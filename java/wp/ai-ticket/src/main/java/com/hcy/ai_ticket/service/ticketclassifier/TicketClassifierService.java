package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.util.DebugTrace;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

@Service
public class TicketClassifierService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	@Autowired
	private AsyncTicketService asyncTicketService;

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
		
		LOGGER.info("執行readCSV");
		List<String> texts = new ArrayList<>();
		try (InputStreamReader isr = new InputStreamReader(new ByteArrayInputStream(content), StandardCharsets.UTF_8);
				CSVReader reader = new CSVReaderBuilder(isr).withSkipLines(1).build()) {

			String[] line;
			while ((line = reader.readNext()) != null) {
				texts.add(line[0]);
			}
		}

		predictAndSave(texts,traceId);

	}

}
