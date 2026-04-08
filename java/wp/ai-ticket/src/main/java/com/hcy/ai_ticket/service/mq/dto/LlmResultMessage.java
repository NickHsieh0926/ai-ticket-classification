package com.hcy.ai_ticket.service.mq.dto;

public class LlmResultMessage {

	private String traceId;
	private String spanId;
	private String cacheKey;
	private String text;
	private String predictedLabel;
	private String confidence;
	private String reasoning;
	private String model;
	private boolean ragUsed;

	public LlmResultMessage() {
		super();
	}

	public LlmResultMessage(String traceId, String spanId, String cacheKey, String text, String predictedLabel,
			String confidence, String reasoning, String model, boolean ragUsed) {
		super();
		this.traceId = traceId;
		this.spanId = spanId;
		this.cacheKey = cacheKey;
		this.text = text;
		this.predictedLabel = predictedLabel;
		this.confidence = confidence;
		this.reasoning = reasoning;
		this.model = model;
		this.ragUsed = ragUsed;
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

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getPredictedLabel() {
		return predictedLabel;
	}

	public void setPredictedLabel(String predictedLabel) {
		this.predictedLabel = predictedLabel;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	public String getReasoning() {
		return reasoning;
	}

	public void setReasoning(String reasoning) {
		this.reasoning = reasoning;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public boolean isRagUsed() {
		return ragUsed;
	}

	public void setRagUsed(boolean ragUsed) {
		this.ragUsed = ragUsed;
	}

}
