package com.hcy.ai_ticket.service.ticketclassifier;

import java.io.IOException;
import java.util.List;

import com.opencsv.exceptions.CsvException;

public interface IFileParser {

	List<String> parse(byte[] content) throws IOException, CsvException;

}
