package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.IOException;
import java.util.List;

import com.hcy.ai_ticket.service.ticketclassifier.dto.DashboardStatsDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.opencsv.exceptions.CsvException;

public interface ITicketAppService {

	PredictionResult predict(String text) throws Exception;

	List<PredictionResult> predictBatch(List<String> texts) throws Exception;

	void processFile(byte[] content, String traceId) throws IOException, CsvException;

	List<String> getTraceIds();

	DashboardStatsDTO getDashboardStats(String traceId);

}
