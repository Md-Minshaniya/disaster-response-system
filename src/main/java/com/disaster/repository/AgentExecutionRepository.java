package com.disaster.repository;

import com.disaster.model.AgentExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AgentExecutionRepository extends JpaRepository<AgentExecution, Long> {
    List<AgentExecution> findBySimulationId(Long simulationId);
    
    List<AgentExecution> findByAgentName(String agentName);
    
    @Query("SELECT AVG(ae.executionTimeMs) FROM AgentExecution ae WHERE ae.agentName = :agentName")
    Double getAverageExecutionTimeByAgent(@Param("agentName") String agentName);
    
    @Query("SELECT ae FROM AgentExecution ae WHERE ae.simulationId = :simulationId ORDER BY ae.priorityScore DESC")
    List<AgentExecution> findBySimulationIdOrderedByPriority(@Param("simulationId") Long simulationId);
    
    @Query("SELECT COUNT(ae) FROM AgentExecution ae WHERE ae.simulationId = :simulationId")
    Long countBySimulationId(@Param("simulationId") Long simulationId);
}
