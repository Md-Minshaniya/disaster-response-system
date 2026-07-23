package com.disaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Entity
@Table(name = "simulations")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Simulation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private Double rainfall;

    @Column(name = "river_level", nullable = false)
    private Double riverLevel;

    @Column(nullable = false)
    private Integer population;

    @Column(name = "flood_risk")
    private Double floodRisk;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "adaptive_priority_used")
    @Builder.Default
    private Boolean adaptivePriorityUsed = true;

    @Column(name = "available_teams")
    private Integer availableTeams;

    @Column(name = "available_boats")
    private Integer availableBoats;

    @Column(name = "food_kits")
    private Integer foodKits;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String toString() {
        return String.format(
                "Simulation[id=%d, region=%s, rainfall=%.2f, riverLevel=%.2f, population=%d, riskLevel=%s]",
                id, region, rainfall, riverLevel, population, riskLevel);
    }
}
