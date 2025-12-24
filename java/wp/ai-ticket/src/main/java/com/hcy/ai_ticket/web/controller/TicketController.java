package com.hcy.ai_ticket.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hcy.ai_ticket.service.ticketclassifier.TicketClassifierService;
import com.hcy.ai_ticket.service.ticketclassifier.dto.PredictionResult;

@RestController
@RequestMapping("/tickets")
public class TicketController {
	
	private final TicketClassifierService classifierService = new TicketClassifierService();

    @PostMapping("/predict")
    public PredictionResult predict(@RequestBody Map<String, String> payload) throws Exception {
        String text = payload.get("text");
        return classifierService.predict(text);
    }
    
    // 批次預測
    @PostMapping("/predict/batch")
    public List<PredictionResult> predictBatch(@RequestBody Map<String, List<String>> payload) throws Exception {
        List<String> texts = payload.get("texts"); 
        return classifierService.predictBatch(texts);
    }
    
}
