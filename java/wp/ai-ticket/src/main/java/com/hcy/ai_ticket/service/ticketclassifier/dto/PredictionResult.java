package com.hcy.ai_ticket.service.ticketclassifier.dto;

public class PredictionResult {
	private String input;
	private String predictedLabel;
	private double confidence;

	public PredictionResult() {
	}

	public PredictionResult(String input, String predictedLabel, double confidence) {
		super();
		this.input = input;
		this.predictedLabel = predictedLabel;
		this.confidence = confidence;
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

}
