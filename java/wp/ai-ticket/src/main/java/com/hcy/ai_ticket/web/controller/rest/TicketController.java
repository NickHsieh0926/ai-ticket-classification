package com.hcy.ai_ticket.web.controller.rest;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcy.ai_ticket.service.ticketclassifier.TicketClassifierService;
import com.hcy.ai_ticket.service.ticketclassifier.dto.DashboardStatsDTO;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.util.DebugTrace;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
	@Autowired
	private TicketClassifierService ticketClassifierService;

    @PostMapping("/predict")
    public PredictionResult predict(@RequestBody Map<String, String> payload) throws Exception {
        String text = payload.get("text");
        return ticketClassifierService.predict(text);
    }
    
    @PostMapping("/predict/batch")
    public List<PredictionResult> predictBatch(@RequestBody Map<String, List<String>> payload) throws Exception {
        List<String> texts = payload.get("texts"); 
        return ticketClassifierService.predictBatch(texts);
    }
    
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadTickets(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "請選擇檔案"));
        }
        
        String traceId = MDC.get("traceId");
        
        byte[] fileBytes = file.getBytes();
        ticketClassifierService.processFile(fileBytes, traceId);
        
        LOGGER.info("分析檔案 TraceID : " + traceId);
        
        return ResponseEntity.ok(Map.of("traceId", traceId));
    }
    
    @GetMapping("/trace-ids")
    public List<String> getTraceIds() throws Exception {
        return ticketClassifierService.getTraceIds();
    }
    
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats(@RequestParam("traceId") String traceId) {
        return ResponseEntity.ok(ticketClassifierService.getDashboardStats(traceId));
    }
    
}
