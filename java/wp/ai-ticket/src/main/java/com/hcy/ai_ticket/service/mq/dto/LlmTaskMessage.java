package com.hcy.ai_ticket.service.mq.dto;

public class LlmTaskMessage {

	private String traceId;
	private String spanId;
	private String text;
	private String cacheKey;

	public LlmTaskMessage() {
		super();
	}

	public LlmTaskMessage(String traceId, String spanId, String text, String cacheKey) {
		super();
		this.traceId = traceId;
		this.spanId = spanId;
		this.text = text;
		this.cacheKey = cacheKey;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getSpanId() {
		return spanId;
	}

	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

}
