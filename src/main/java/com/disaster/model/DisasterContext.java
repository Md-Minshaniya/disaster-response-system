package com.disaster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisasterContext {
    private String region;
    private Double rainfall;
    private Double riverLevel;
    private Integer population;
    private Integer availableTeams;
    private Integer availableBoats;
    private Integer foodKits;
    private Double floodRisk;
    private String riskLevel;
    private Long simulationId;
    @Builder.Default
    private Boolean adaptivePriorityUsed = true;

    public Double getRainfallRisk() {
        return rainfall / 100.0;
    }

    public Double getRiverRisk() {
        return riverLevel / 10.0;
    }

    public Double getPopulationDensity() {
        return population / 1000000.0;
    }

    public Double getTotalResourcesNeeded() {
        double teams = Math.min(population / 100000.0, 20);
        double boats = Math.min(population / 500000.0, 15);
        double foodKits = Math.min(population / 1000.0, 10000);
        return teams + boats + foodKits;
    }
}
