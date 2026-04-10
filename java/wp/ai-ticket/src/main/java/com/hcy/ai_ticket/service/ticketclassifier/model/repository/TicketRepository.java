package com.hcy.ai_ticket.service.ticketclassifier.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hcy.ai_ticket.service.ticketclassifier.model.rdb.Ticket;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection.AbComparison;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection.BarChart;
import com.hcy.ai_ticket.service.ticketclassifier.model.repository.projection.PieChart;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

	@Query(value = """
			SELECT trace_id FROM tickets
			GROUP BY trace_id
			ORDER BY MAX(created_timestamp) DESC
			LIMIT 10
			""", nativeQuery = true)
	List<String> findRecentTraceIds();

	@Query(value = """
			SELECT category, COUNT(*) as value FROM tickets
			         WHERE trace_id = :traceId
			         GROUP BY category
			""", nativeQuery = true)
	List<PieChart> countByCategory(@Param("traceId") String traceId);

	@Query(value = """
			SELECT
			 COALESCE(SUM(CASE WHEN confidence < '0.2' THEN 1 ELSE 0 END), 0) as range1,
			 COALESCE(SUM(CASE WHEN confidence >= '0.2' AND confidence < '0.4' THEN 1 ELSE 0 END), 0) as range2,
			 COALESCE(SUM(CASE WHEN confidence >= '0.4' AND confidence < '0.6' THEN 1 ELSE 0 END), 0) as range3,
			 COALESCE(SUM(CASE WHEN confidence >= '0.6' AND confidence < '0.8' THEN 1 ELSE 0 END), 0) as range4,
			 COALESCE(SUM(CASE WHEN confidence >= '0.8' THEN 1 ELSE 0 END), 0) as range5
			FROM tickets WHERE trace_id = :traceId
			   """, nativeQuery = true)
	BarChart countByConfidenceRanges(@Param("traceId") String traceId);

	@Query(value = """
			SELECT trace_id, content,
			       ml_category, llm_category,
			       ml_confidence, llm_confidence,
			       is_match
			FROM ab_comparison
			WHERE trace_id = :traceId
			""", nativeQuery = true)
	List<AbComparison> findAbComparison(@Param("traceId") String traceId);

	@Query(value = """
			SELECT trace_id FROM tickets
			GROUP BY trace_id
			HAVING SUM(CASE WHEN model_type = 'ml'  THEN 1 ELSE 0 END) > 0
			   AND SUM(CASE WHEN model_type = 'llm' THEN 1 ELSE 0 END) > 0
			ORDER BY MAX(created_timestamp) DESC
			LIMIT 10
			""", nativeQuery = true)
	List<String> findAbTraceIds();

}
