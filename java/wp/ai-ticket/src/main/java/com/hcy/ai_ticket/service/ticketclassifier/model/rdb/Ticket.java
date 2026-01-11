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

	@Column(name = "trace_id")
	private String traceId;

	@CreationTimestamp
	@Column(name = "created_Timestamp", updatable = false)
	private LocalDateTime createdTimestamp;

	public Ticket() {
	}

	public Ticket(Long id, String content, String category, String confidence, String traceId,
			LocalDateTime createdTimestamp) {
		super();
		this.id = id;
		this.content = content;
		this.category = category;
		this.confidence = confidence;
		this.traceId = traceId;
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

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public LocalDateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(LocalDateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

}
