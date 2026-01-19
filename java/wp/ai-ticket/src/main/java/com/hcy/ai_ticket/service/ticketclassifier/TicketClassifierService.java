package com.hcy.ai_ticket.service.ticketclassifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;
import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.TicketRepository;
import com.hcy.ai_ticket.util.DebugTrace;

@Service
public class TicketClassifierService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TicketClassifierService.class);
	private static final DebugTrace TRACE = new DebugTrace(LOGGER, LOGGER.isDebugEnabled());

    @Autowired
    private AsyncTicketService asyncTicketService;

    public String predictAndSave(List<String> contents) {
        String traceId = MDC.get("traceId"); 
        LOGGER.info("接收批次預測請求，總數: {}", contents.size());

        for (int i = 0; i < contents.size(); i++) {
            asyncTicketService.runAiPredictionTask(contents.get(i), traceId, i + 1);
        }

        return traceId; 
    }
    
}
