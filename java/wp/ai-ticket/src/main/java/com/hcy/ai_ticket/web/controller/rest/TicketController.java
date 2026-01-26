package com.hcy.ai_ticket.web.controller.rest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hcy.ai_ticket.service.ticketclassifier.TicketClassifierService;
import com.hcy.ai_ticket.util.DebugTrace;
import com.opencsv.exceptions.CsvException;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketController.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());
	
	@Autowired
	private TicketClassifierService ticketClassifierService;

    @PostMapping("/predict")
    public String predictBatch(@RequestBody Map<String, List<String>> payload) throws Exception {
    	String traceId = MDC.get("traceId");
        List<String> texts = payload.get("texts"); 
        return ticketClassifierService.predictAndSave(texts,traceId);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadTickets(@RequestParam("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "請選擇檔案"));
        }
        
        LOGGER.info(">>> 接收到檔案: " + file.getOriginalFilename());
        String traceId = MDC.get("traceId");
        LOGGER.info(">>> 生成的 TraceID : " + traceId);
        
        byte[] fileBytes = file.getBytes();
        ticketClassifierService.processFile(fileBytes, traceId);
        
        return ResponseEntity.ok(Map.of("traceId", traceId));
    }
    
}
