package com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection;

public interface AbComparison {
	String getTraceId();

	String getContent();

	String getMlCategory();

	String getLlmCategory();

	Double getMlConfidence();

	Double getLlmConfidence();

	Integer getIsMatch();
}
