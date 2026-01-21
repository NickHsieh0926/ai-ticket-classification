package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.hcy.ai_ticket.util.DebugTrace;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

@SpringBootTest
public class TicketStressTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketStressTest.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	private static final String TRACE_ID = "traceId";

	private static final String csvFilePath = "D:\\NK_WP\\LLM\\ai-ticket-classification\\java\\wp\\ai-ticket\\src\\test\\resources\\async_ticket_test_data.csv";

	@Autowired
	private TicketClassifierService ticketClassifierService;

//	@Test
	public void startStressTest() throws Exception {
		List<String> texts = new ArrayList<>();
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFilePath)).withSkipLines(1).build()) {

			String[] line;
			while ((line = reader.readNext()) != null) {
				texts.add(line[0]);
			}
		}
		LOGGER.info("成功讀取 " + texts.size() + " 筆測試資料，準備發送壓力測試...");

		String traceId = UUID.randomUUID().toString().replace("-", "");
		MDC.put(TRACE_ID, traceId);

		String globalTraceId = ticketClassifierService.predictAndSave(texts);
		LOGGER.info("壓力測試已啟動，Global TraceID: " + globalTraceId);

		Thread.sleep(30 * 1000);
	}

	@Test
	public void simulateMultiUserConcurrentRequests() throws Exception {
	    int numberOfUsers = 5; 
	    ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
	    CountDownLatch latch = new CountDownLatch(numberOfUsers);
	    
		List<String> texts = new ArrayList<>();
		try (CSVReader reader = new CSVReaderBuilder(new FileReader(csvFilePath)).withSkipLines(1).build()) {

			String[] line;
			while ((line = reader.readNext()) != null) {
				texts.add(line[0]);
			}
		}

	    for (int i = 0; i < numberOfUsers; i++) {
	        final int userIndex = i;
//	        LOGGER.info("用戶 User-{}", i);
//	        if (i > 0) {
//	            Thread.sleep(10 * 1000); 
//	        }
	        executor.execute(() -> {

	            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "-User" + userIndex;
	            
	            try {
	                MDC.put("traceId", traceId);
	                LOGGER.info("用戶 {} 開始提交 {} 筆工單", userIndex, texts.size());
	                
	                ticketClassifierService.predictAndSave(texts);
	                
	            } catch (Exception e) {
	            	LOGGER.error("用戶 {} 提交失敗: {}", userIndex, e.getMessage());
	            } finally {
	                MDC.clear();
	                latch.countDown();
	            }
	        });
	    }

	    latch.await(); 
	    executor.shutdown();
	    LOGGER.info("所有用戶請求提交完成，等待背景任務處理...");
	    Thread.sleep(30 * 1000); 
	}

}
