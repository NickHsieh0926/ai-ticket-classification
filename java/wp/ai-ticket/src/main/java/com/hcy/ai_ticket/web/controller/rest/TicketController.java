package com.hcy.ai_ticket.web.controller.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcy.ai_ticket.service.ticketclassifier.TicketClassifierService;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {
	
	@Autowired
	private TicketClassifierService ticketClassifierService;

    @PostMapping("/predict")
    public String predictBatch(@RequestBody Map<String, List<String>> payload) throws Exception {
        List<String> texts = payload.get("texts"); 
        return ticketClassifierService.predictAndSave(texts);
    }
    
}
