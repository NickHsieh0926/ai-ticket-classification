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
	private TicketClassifierService classifierService;

    @PostMapping("/predict")
    public PredictionResult predict(@RequestBody Map<String, String> payload) throws Exception {
        String text = payload.get("text");
        return classifierService.predictAndSave(text);
    }
    
    @PostMapping("/predict/batch")
    public List<PredictionResult> predictBatch(@RequestBody Map<String, List<String>> payload) throws Exception {
        List<String> texts = payload.get("texts"); 
        return classifierService.predictBatchAndSave(texts);
    }
    
}
