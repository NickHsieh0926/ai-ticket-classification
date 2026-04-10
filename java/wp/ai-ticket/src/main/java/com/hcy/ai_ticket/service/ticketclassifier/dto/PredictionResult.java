package com.hcy.ai_ticket.service.ticketclassifier.dto;

public class PredictionResult {
	private String input;
	private String predictedLabel;
	private double confidence;
	private String reasoning;
	private String model;
	private Boolean ragUsed;

	public PredictionResult() {
		super();
	}

	public PredictionResult(String input, String predictedLabel, double confidence) {
		this.input = input;
		this.predictedLabel = predictedLabel;
		this.confidence = confidence;
	}

	public PredictionResult(String input, String predictedLabel, double confidence, String reasoning, String model,
			Boolean ragUsed) {
		super();
		this.input = input;
		this.predictedLabel = predictedLabel;
		this.confidence = confidence;
		this.reasoning = reasoning;
		this.model = model;
		this.ragUsed = ragUsed;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getPredictedLabel() {
		return predictedLabel;
	}

	public void setPredictedLabel(String predictedLabel) {
		this.predictedLabel = predictedLabel;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
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

	public Boolean getRagUsed() {
		return ragUsed;
	}

	public void setRagUsed(Boolean ragUsed) {
		this.ragUsed = ragUsed;
	}

}
