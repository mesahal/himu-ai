package com.himu.ai.controller;

import com.himu.ai.dto.ChatRequest;
import com.himu.ai.dto.ChatResponse;
import com.himu.ai.service.VertexAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

	@Autowired
	private VertexAiService vertexAiService;

	@PostMapping("/chat")
	public Mono<ChatResponse> chatWithHimu(@RequestBody ChatRequest chatRequest) {
		return vertexAiService.getHimuResponse(chatRequest.getMessage())
				.map(ChatResponse::new);
	}
}

