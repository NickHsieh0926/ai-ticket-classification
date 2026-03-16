package com.hcy.ai_ticket.service.ticketclassifier;

public interface IProgressNotifier {

	void notify(String traceId, int current, int total, String label);

}
