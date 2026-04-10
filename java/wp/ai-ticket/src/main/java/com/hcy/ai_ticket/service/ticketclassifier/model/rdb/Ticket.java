package com.hcy.ai_ticket.service.ticketclassifier.model.rdb;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String content;

	private String category;

	private String confidence;

	private String status;

	@Column(name = "trace_id")
	private String traceId;

	@Column(name = "span_id")
	private String spanId;

	@Column(name = "model_type")
	private String modelType;

	@CreationTimestamp
	@Column(name = "created_Timestamp", updatable = false)
	private LocalDateTime createdTimestamp;

	public Ticket() {
		super();
	}

	public Ticket(Long id, String content, String category, String confidence, String status, String traceId,
			String spanId, String modelType, LocalDateTime createdTimestamp) {
		super();
		this.id = id;
		this.content = content;
		this.category = category;
		this.confidence = confidence;
		this.status = status;
		this.traceId = traceId;
		this.spanId = spanId;
		this.modelType = modelType;
		this.createdTimestamp = createdTimestamp;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getConfidence() {
		return confidence;
	}

	public void setConfidence(String confidence) {
		this.confidence = confidence;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getModelType() {
		return modelType;
	}

	public void setModelType(String modelType) {
		this.modelType = modelType;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

}
