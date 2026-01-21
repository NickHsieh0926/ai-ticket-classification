package com.hcy.ai_ticket.service.webSocket.dto;

public class ProgressMessage {

	private String traceId;
	private String current;
	private String total;
	private String percentage;
	private String category;
	private String status;

	public ProgressMessage() {
	}

	public ProgressMessage(String traceId, String current, String total, String percentage, String category,
			String status) {
		this.traceId = traceId;
		this.current = current;
		this.total = total;
		this.percentage = percentage;
		this.category = category;
		this.status = status;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
