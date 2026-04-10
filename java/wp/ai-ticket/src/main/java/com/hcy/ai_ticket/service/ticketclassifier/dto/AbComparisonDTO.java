package com.hcy.ai_ticket.service.ticketclassifier.dto;

public record AbComparisonDTO(
	    String traceId,
	    String content,
	    String mlCategory,
	    String llmCategory,
	    Double mlConfidence,
	    Double llmConfidence,
	    Integer isMatch
) {}
