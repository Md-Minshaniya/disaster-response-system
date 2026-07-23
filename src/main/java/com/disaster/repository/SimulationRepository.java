package com.disaster.repository;

import com.disaster.model.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    List<Simulation> findByRegion(String region);
    
    List<Simulation> findByRiskLevel(String riskLevel);
    
    @Query("SELECT s FROM Simulation s WHERE s.floodRisk > :threshold ORDER BY s.floodRisk DESC")
    List<Simulation> findHighRiskSimulations(@Param("threshold") Double threshold);
    
    @Query("SELECT AVG(s.responseTimeMs) FROM Simulation s")
    Double getAverageResponseTime();
    
    @Query("SELECT COUNT(s) FROM Simulation s WHERE s.riskLevel = 'CRITICAL'")
    Long countCriticalSimulations();
    
    @Query("SELECT COUNT(s) FROM Simulation s")
    Long getTotalSimulations();
    
    @Query("SELECT AVG(CASE WHEN s.floodRisk > 0.7 THEN 1 ELSE 0 END) FROM Simulation s")
    Double getSuccessRate();
}
