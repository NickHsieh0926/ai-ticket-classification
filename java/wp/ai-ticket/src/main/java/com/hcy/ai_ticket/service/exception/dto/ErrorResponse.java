package com.hcy.ai_ticket.service.exception.dto;

import java.time.LocalDateTime;

public class ErrorResponse {
	private int status;
	private String message;
	private String traceId;
	private LocalDateTime timestamp;

	public ErrorResponse() {
	}

	public ErrorResponse(int status, String message, String traceId, LocalDateTime timestamp) {
		this.status = status;
		this.message = message;
		this.traceId = traceId;
		this.timestamp = timestamp;
	}

	public ErrorResponse(int status, String message, String traceId) {
		this.status = status;
		this.message = message;
		this.traceId = traceId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

}
