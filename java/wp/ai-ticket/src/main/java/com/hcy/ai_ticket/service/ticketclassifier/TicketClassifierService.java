package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.dto.DashboardStatsDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection.BarChart;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection.PieChart;
import com.hcy.ai_ticket.util.DebugTrace;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

@Service
public class TicketClassifierService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	@Value("${endpoint}")
	private String endpoint;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AsyncTicketService asyncTicketService;

	@Autowired
	private TicketRepository ticketRepository;

	public PredictionResult predict(String ticketText) throws Exception {

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("text", ticketText);

		@SuppressWarnings("unchecked")
		Map<String, Object> result = restTemplate.postForObject(endpoint, requestBody, Map.class);

		if (result == null) {
			throw new RuntimeException("AI model returned empty response");
		}

		return new PredictionResult(
				(String) result.get("input"), 
				(String) result.get("predicted_label"),
				Double.valueOf((String) result.get("confidence"))
				);
	}

	public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
		List<PredictionResult> results = new ArrayList<>();
		for (String t : texts) {
			results.add(predict(t));
		}
		return results;
	}

	public String predictAndSave(List<String> contents, String traceId) {
		TRACE.message("接收批次預測請求，總數: {}", contents.size());

		for (int i = 0; i < contents.size(); i++) {

			try {
				asyncTicketService.runAiPredictionTask(contents.get(i), traceId, i + 1, contents.size());
			} catch (Exception e) {
				LOGGER.error("提交第 {} 筆任務時發生錯誤: {}", i, e.getMessage());
			}

		}

		return traceId;
	}

	@Async("dispatcherExecutor")
	public void processFile(byte[] content, String traceId) throws IOException, CsvException {
		
		TRACE.message("執行CSVReader");
		List<String> texts = new ArrayList<>();
		try (InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
				CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {

			String[] line;
			while ((line = reader.readNext()) != null) {
				texts.add(line[0]);
			}
		}

		predictAndSave(texts, traceId);

	}

	public List<String> getTraceIds() {
		return ticketRepository.findRecentTraceIds();
	}

	public DashboardStatsDTO getDashboardStats(String traceId) {
		DashboardStatsDTO dto = new DashboardStatsDTO();

		List<PieChart> categoryResults = ticketRepository.countByCategory(traceId);
		List<DashboardStatsDTO.CategoryCount> categoryStats = categoryResults.stream()
				.map(p -> new DashboardStatsDTO.CategoryCount(p.getCategory(), p.getValue()))
				.collect(Collectors.toList());
		dto.setCategoryStats(categoryStats);

		BarChart barData = ticketRepository.countByConfidenceRanges(traceId);
		if (barData != null) {
			dto.setConfidenceStats(List.of(barData.getRange1(), barData.getRange2(), barData.getRange3(),
					barData.getRange4(), barData.getRange5()));
		} else {
			dto.setConfidenceStats(List.of(0L, 0L, 0L, 0L, 0L));
		}

		return dto;
	}

}
