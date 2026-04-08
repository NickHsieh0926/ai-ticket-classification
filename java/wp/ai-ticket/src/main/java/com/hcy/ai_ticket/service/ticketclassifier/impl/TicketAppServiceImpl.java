package com.hcy.ai_ticket.service.ticketclassifier.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.service.ticketclassifier.IAiInferenceClient;
import com.hcy.ai_ticket.service.ticketclassifier.ITicketAppService;
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
public class TicketAppServiceImpl implements ITicketAppService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketAppServiceImpl.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	@Value("${ai.mode:ml}")
	private String aiMode;

	private final IAiInferenceClient aiClient;
	private final AsyncTicketService asyncTicketService;
	private final TicketRepository ticketRepository;
	private final LlmBatchService llmBatchService;

	public TicketAppServiceImpl(IAiInferenceClient aiClient, AsyncTicketService asyncTicketService,
			TicketRepository ticketRepository, LlmBatchService llmBatchService) {
		this.aiClient = aiClient;
		this.asyncTicketService = asyncTicketService;
		this.ticketRepository = ticketRepository;
		this.llmBatchService = llmBatchService;
	}

	@Override
	public PredictionResult predict(String text) throws Exception {
		return aiClient.predict(text);
	}

	@Override
	public List<PredictionResult> predictBatch(List<String> texts) throws Exception {
		return aiClient.predictBatch(texts);
	}

	@Override
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
		
		if ("llm".equals(aiMode)) {
			llmBatchService.dispatchBatch(texts, traceId);
		} else {
			predictAndSave(texts, traceId);
		}
	}

	private void predictAndSave(List<String> contents, String traceId) {
		TRACE.message("接收批次預測請求，總數: {}", contents.size());
		for (int i = 0; i < contents.size(); i++) {
			try {
				asyncTicketService.runAiPredictionTask(contents.get(i), traceId, i + 1, contents.size());
			} catch (Exception e) {
				LOGGER.error("提交第 {} 筆任務時發生錯誤: {}", i, e.getMessage());
			}
		}
	}

	@Override
	public List<String> getTraceIds() {
		return ticketRepository.findRecentTraceIds();
	}

	@Override
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
