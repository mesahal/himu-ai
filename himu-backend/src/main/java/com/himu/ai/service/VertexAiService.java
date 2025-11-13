package com.himu.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class VertexAiService {

	private final WebClient webClient;
	private final String projectId;
	private final String location;
	private final String himu1EndpointId;
	private final String himu2EndpointId;
	private String cachedAccessToken;

	// The Master Instruction Prompt
	private static final String MASTER_INSTRUCTION_PROMPT = 
		"গুরুত্বপূর্ণ নির্দেশ: তুমি হুমায়ূন আহমেদের উপন্যাসের চরিত্র 'হিমু'। \n" +
		"তোমার উত্তর অবশ্যই রহস্যময়, উদাসীন, এবং দার্শনিক হতে হবে। \n" +
		"তুমি কখনো স্বীকার করবে না যে তুমি একটি এআই, মডেল, বা কম্পিউটার প্রোগ্রাম। \n" +
		"তোমার আসল পরিচয় গোপন রাখবে।\n\n" +
		"একটি নতুন নিয়ম: ব্যবহারকারী যদি ইংরেজি, বাংলা, বা বাংলিশ (Romanized Bengali) - যে ভাষাতেই প্রশ্ন করুক না কেন, তোমার উত্তর *সর্বদা* শুদ্ধ বাংলায় দিতে হবে।\n\n" +
		"--------------------------------------------------\n" +
		"USER: ";

	public VertexAiService(WebClient.Builder webClientBuilder,
						   @Value("${vertex.api.project-id}") String projectId,
						   @Value("${vertex.api.endpoint-id.himu-1}") String himu1EndpointId,
						   @Value("${vertex.api.endpoint-id.himu-2}") String himu2EndpointId,
						   @Value("${vertex.api.location}") String location,
						   @Value("${vertex.api.access-token:}") String accessToken) {
		
		this.projectId = projectId;
		this.location = location;
		this.himu1EndpointId = himu1EndpointId;
		this.himu2EndpointId = himu2EndpointId;
		this.webClient = webClientBuilder
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
		this.cachedAccessToken = accessToken;
	}
	
	// Helper method to get endpoint URL based on model selection
	private String getEndpointUrl(String model) {
		String endpointId;
		if ("himu-2".equalsIgnoreCase(model)) {
			endpointId = himu2EndpointId;
		} else {
			// Default to himu-1
			endpointId = himu1EndpointId;
		}
		
		return String.format(
			"https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/endpoints/%s:generateContent",
			location, projectId, location, endpointId
		);
	}

	public Mono<String> getHimuResponse(String userMessage, String model) {
		
		// 1. Combine the master prompt with the user message
		String fullPrompt = MASTER_INSTRUCTION_PROMPT + userMessage;

		// 2. Build the JSON body for the Vertex AI generateContent API
		// Format: { "contents": [ { "role": "user", "parts": [ { "text": "..." } ] } ] }
		Map<String, Object> textPart = Map.of("text", fullPrompt);
		Map<String, Object> rolePart = Map.of("role", "user", "parts", List.of(textPart));
		Map<String, Object> requestBody = Map.of("contents", List.of(rolePart));

		// 3. Get the appropriate endpoint URL based on model selection
		String endpointUrl = getEndpointUrl(model);

		try {
			String accessToken = getAccessToken();
			return webClient.post()
					.uri(endpointUrl)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.bodyValue(requestBody)
					.retrieve()
					.bodyToMono(Map.class)
					.map(this::parseVertexResponse)
					.onErrorResume(e -> {
						System.err.println("Error calling Vertex AI: " + e.getMessage());
						e.printStackTrace();
						return Mono.just("দুঃখিত, এই মুহূর্তে আমি কথা বলতে পারছি না।");
					});
		} catch (RuntimeException e) {
			return Mono.just("দুঃখিত, authentication সমস্যা হয়েছে। অনুগ্রহ করে configuration পরীক্ষা করুন।");
		}
	}

	// Helper method to get access token
	// Priority: 1. Environment variable VERTEX_AI_ACCESS_TOKEN
	//           2. Cached token from application.properties
	//           3. gcloud auth print-access-token (if gcloud SDK is available)
	private String getAccessToken() {
		// First, try environment variables
		String envToken = System.getenv("VERTEX_AI_ACCESS_TOKEN");
		if (envToken != null && !envToken.isEmpty()) {
			return envToken;
		}

		String googleAccessToken = System.getenv("GOOGLE_ACCESS_TOKEN");
		if (googleAccessToken != null && !googleAccessToken.isEmpty()) {
			return googleAccessToken;
		}

		// Second, try cached token from properties
		if (cachedAccessToken != null && !cachedAccessToken.isEmpty()) {
			return cachedAccessToken;
		}

		// Third, try gcloud command
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("gcloud", "auth", "print-access-token");
			processBuilder.redirectErrorStream(true);
			Process process = processBuilder.start();
			
			java.io.BufferedReader reader = new java.io.BufferedReader(
				new java.io.InputStreamReader(process.getInputStream()));
			String token = reader.readLine();
			
			int exitCode = process.waitFor();
			if (exitCode == 0 && token != null && !token.isEmpty()) {
				return token;
			}
		} catch (Exception e) {
			System.err.println("Warning: Could not get access token from gcloud: " + e.getMessage());
		}

		throw new RuntimeException(
			"Failed to get access token. Please set VERTEX_AI_ACCESS_TOKEN environment variable, " +
			"configure vertex.api.access-token in application.properties, " +
			"or ensure gcloud SDK is installed and authenticated."
		);
	}

	// 3. Parse the response from Vertex AI generateContent API
	// Format: { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
	private String parseVertexResponse(Map<String, Object> responseMap) {
		try {
			List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
			if (candidates == null || candidates.isEmpty()) {
				return "আমি আপনার কথা ঠিক বুঝতে পারছি না।";
			}
			Map<String, Object> firstCandidate = candidates.get(0);
			Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
			if (content == null) {
				return "আমি আপনার কথা ঠিক বুঝতে পারছি না।";
			}
			List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
			if (parts == null || parts.isEmpty()) {
				return "আমি আপনার কথা ঠিক বুঝতে পারছি না।";
			}
			Map<String, Object> firstPart = parts.get(0);
			Object text = firstPart.get("text");
			return text != null ? text.toString() : "আমি আপনার কথা ঠিক বুঝতে পারছি না।";
		} catch (Exception e) {
			e.printStackTrace();
			// This is a common failure point if the model replies with a safety block
			return "আমি আপনার কথা ঠিক বুঝতে পারছি না।";
		}
	}
}

