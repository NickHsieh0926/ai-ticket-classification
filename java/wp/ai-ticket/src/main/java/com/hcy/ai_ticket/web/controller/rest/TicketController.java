package com.hcy.ai_ticket.web.controller.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcy.ai_ticket.service.ticketclassifier.ITicketAppService;
import com.hcy.ai_ticket.service.ticketclassifier.dto.AbComparisonDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.DashboardStatsDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.util.DebugTrace;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

	private final ITicketAppService ticketAppService;

	public TicketController(ITicketAppService ticketAppService) {
		this.ticketAppService = ticketAppService;
	}

	@PostMapping("/predict")
	public PredictionResult predict(@RequestBody Map<String, String> payload,
			@RequestHeader(value = "X-Model-Type", defaultValue = "ml") String modelType) throws Exception {
		String text = payload.get("text");
		return ticketAppService.predict(text, modelType);
	}

	@PostMapping("/predict/batch")
	public List<PredictionResult> predictBatch(@RequestBody Map<String, List<String>> payload,
			@RequestHeader(value = "X-Model-Type", defaultValue = "ml") String modelType) throws Exception {
		List<String> texts = payload.get("texts");
		return ticketAppService.predictBatch(texts, modelType);
	}

	@PostMapping("/upload")
	public ResponseEntity<Map<String, String>> uploadTickets(@RequestParam("file") MultipartFile file,
			@RequestHeader(value = "X-Model-Type", defaultValue = "ml") String modelType) throws Exception {
		if (file.isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("message", "請選擇檔案"));
		}

		String traceId = MDC.get("traceId");

		byte[] fileBytes = file.getBytes();
		ticketAppService.processFile(fileBytes, traceId, modelType);

		LOGGER.info("分析檔案 TraceID : " + traceId);

		return ResponseEntity.ok(Map.of("traceId", traceId));
	}

	@GetMapping("/trace-ids")
	public List<String> getTraceIds() throws Exception {
		return ticketAppService.getTraceIds();
	}

	@GetMapping("/stats")
	public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam("traceId") String traceId) {
		return ResponseEntity.ok(ticketAppService.getDashboardStats(traceId));
	}

	@GetMapping("/ab-comparison")
	public ResponseEntity<List<AbComparisonDTO>> getAbComparison(@RequestParam("traceId") String traceId) {
		return ResponseEntity.ok(ticketAppService.getAbComparison(traceId));
	}
	
	@PostMapping("/upload/ab")
	public ResponseEntity<Map<String, String>> uploadForAbComparison(
	        @RequestParam("file") MultipartFile file) throws Exception {
	    if (file.isEmpty()) {
	        return ResponseEntity.badRequest().body(Map.of("message", "請選擇檔案"));
	    }
	    String traceId = MDC.get("traceId");
	    byte[] fileBytes = file.getBytes();
	    ticketAppService.processFileForAb(fileBytes, traceId);
	    LOGGER.info("AB 比較任務啟動 TraceID: {}", traceId);
	    return ResponseEntity.ok(Map.of("traceId", traceId));
	}

}
