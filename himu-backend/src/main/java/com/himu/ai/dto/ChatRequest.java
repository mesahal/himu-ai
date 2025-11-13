package com.himu.ai.dto;

import lombok.Data;

@Data
public class ChatRequest {
	private String message;
	private String model; // "himu-1" or "himu-2"
}

