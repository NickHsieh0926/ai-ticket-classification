package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.List;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

public interface IAiInferenceClient {

	PredictionResult predict(String text) throws Exception;

	List<PredictionResult> predictBatch(List<String> texts) throws Exception;

}
