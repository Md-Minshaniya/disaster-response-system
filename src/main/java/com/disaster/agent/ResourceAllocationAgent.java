package com.disaster.agent;

import com.disaster.model.DisasterContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@Getter
@Setter
public class ResourceAllocationAgent extends BaseAgent {
    public static final String AGENT_ID = "🚁 Resource Allocator";
    public static final String AGENT_NAME = "Resource Allocation Agent";

    public ResourceAllocationAgent() {
        super(AGENT_ID, AGENT_NAME);
    }

    @Override
    public double computePriorityScore(DisasterContext context) {
        return (context.getFloodRisk() != null ? context.getFloodRisk() * 0.6 : 0) +
                ((context.getPopulation() / 10000.0) * 0.4);
    }

    @Override
    public Object execute(DisasterContext context) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();

        String riskLevel = context.getRiskLevel();
        double resourceMultiplier = getResourceMultiplier(riskLevel);

        int rescueTeams = Math.min((int) (context.getPopulation() / 100000.0 * resourceMultiplier), 20);
        int boats = Math.min((int) (context.getPopulation() / 500000.0 * resourceMultiplier), 15);
        int foodKits = Math.min((int) (context.getPopulation() / 1000.0 * resourceMultiplier), 10000);

        double allocationEfficiency = calculateEfficiency(rescueTeams, boats, foodKits);

        result.put("riskLevel", riskLevel);
        result.put("resourceMultiplier", String.format("%.2f", resourceMultiplier));
        result.put("allocations", Map.of(
                "rescueTeams", rescueTeams,
                "boats", boats,
                "foodKits", foodKits));
        result.put("allocationEfficiency", String.format("%.2f%%", allocationEfficiency * 100));
        result.put("totalResourcesNeeded",
                rescueTeams + boats + foodKits);
        result.put("confidence", calculateConfidence());
        result.put("executionTime", System.currentTimeMillis() - startTime);

        log.info("ResourceAllocationAgent executed: Teams={}, Boats={}, Food={}", rescueTeams, boats, foodKits);
        return result;
    }

    private double getResourceMultiplier(String riskLevel) {
        if ("CRITICAL".equals(riskLevel)) {
            return 1.0;
        } else if ("HIGH".equals(riskLevel)) {
            return 0.66;
        } else if ("MEDIUM".equals(riskLevel)) {
            return 0.33;
        }
        return 0.1;
    }

    private double calculateEfficiency(int teams, int boats, int kits) {
        return (teams / 20.0 + boats / 15.0 + Math.min(kits / 10000.0, 1.0)) / 3.0;
    }
}
