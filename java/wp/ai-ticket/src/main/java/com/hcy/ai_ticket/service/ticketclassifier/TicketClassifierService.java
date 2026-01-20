package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class TicketClassifierService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Autowired
    private AsyncTicketService asyncTicketService;

    public String predictAndSave(List<String> contents) {
        String traceId = MDC.get("traceId"); 
        TRACE.message("接收批次預測請求，總數: {}", contents.size());

        for (int i = 0; i < contents.size(); i++) {
        	
        	try {
        		asyncTicketService.runAiPredictionTask(contents.get(i), traceId, i + 1);
        	}catch (Exception e) {
        		LOGGER.error("提交第 {} 筆任務時發生錯誤: {}", i, e.getMessage());
            }
        	
        }

        return traceId; 
    }
    
}
