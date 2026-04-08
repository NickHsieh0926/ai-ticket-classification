package com.hcy.ai_ticket.service.ticketclassifier;

import java.nio.file.Files;
import java.nio.file.Paths;
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

@SpringBootTest
public class TicketStressTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketStressTest.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	private static final String TRACE_ID = "traceId";
	private static final String modelType = "ml";

	private static final String csvFilePath = "D:\\NK_WP\\LLM\\ai-ticket-classification\\java\\wp\\ai-ticket\\src\\test\\resources\\async_ticket_test_data.csv";

	@Autowired
	private ITicketAppService ticketAppService;

//	@Test
	public void startStressTest() throws Exception {
		byte[] fileBytes = Files.readAllBytes(Paths.get(csvFilePath));

		String traceId = UUID.randomUUID().toString().replace("-", "");
		MDC.put(TRACE_ID, traceId);

		ticketAppService.processFile(fileBytes, traceId, modelType);
		LOGGER.info("壓力測試已啟動，Global TraceID: " + traceId);

		Thread.sleep(30 * 1000);
	}

	@Test
	public void simulateMultiUserConcurrentRequests() throws Exception {
	    int numberOfUsers = 5;
	    ExecutorService executor = Executors.newFixedThreadPool(numberOfUsers);
	    CountDownLatch latch = new CountDownLatch(numberOfUsers);

	    byte[] fileBytes = Files.readAllBytes(Paths.get(csvFilePath));

	    for (int i = 0; i < numberOfUsers; i++) {
	        final int userIndex = i;
	        executor.execute(() -> {
	            String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "-User" + userIndex;

	            try {
	                MDC.put("traceId", traceId);
	                LOGGER.info("用戶 {} 開始提交工單", userIndex);

	                ticketAppService.processFile(fileBytes, traceId, modelType);

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
