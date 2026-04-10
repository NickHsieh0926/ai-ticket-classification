package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.IOException;
import java.util.List;

import com.hcy.ai_ticket.service.ticketclassifier.dto.AbComparisonDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.DashboardStatsDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.opencsv.exceptions.CsvException;

public interface ITicketAppService {

	PredictionResult predict(String text, String modelType) throws Exception;

	List<PredictionResult> predictBatch(List<String> texts, String modelType) throws Exception;

	void processFile(byte[] content, String traceId, String modelType) throws IOException, CsvException;

	List<String> getTraceIds();

	DashboardStatsDTO getDashboardStats(String traceId);

	List<AbComparisonDTO> getAbComparison(String traceId);

	void processFileForAb(byte[] content, String traceId) throws IOException, CsvException;

	List<String> getAbTraceIds();

}
