package com.disaster.agent;

import com.disaster.model.DisasterContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@Getter
@Setter
public class FloodPredictionAgent extends BaseAgent {
    public static final String AGENT_ID = "🌊 Flood Predictor";
    public static final String AGENT_NAME = "Flood Prediction Agent";

    public FloodPredictionAgent() {
        super(AGENT_ID, AGENT_NAME);
    }

    @Override
    public double computePriorityScore(DisasterContext context) {
        double rainfallRisk = context.getRainfallRisk();
        double riverRisk = context.getRiverRisk();
        return (0.6 * rainfallRisk) + (0.4 * riverRisk);
    }

    @Override
    public Object execute(DisasterContext context) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();

        double floodProbability = (context.getRainfall() / 100.0 * 0.6) + (context.getRiverLevel() / 10.0 * 0.4);
        String riskLevel;

        if (floodProbability > 0.7) {
            riskLevel = "CRITICAL";
        } else if (floodProbability > 0.4) {
            riskLevel = "HIGH";
        } else if (floodProbability > 0.2) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }

        List<String> affectedZones = getAffectedZones(context.getRegion(), floodProbability);
        List<String> evacuationRecs = getEvacuationRecommendations(riskLevel);

        result.put("floodProbability", String.format("%.2f", floodProbability * 100) + "%");
        result.put("riskLevel", riskLevel);
        result.put("riverLevel", context.getRiverLevel());
        result.put("affectedZones", affectedZones);
        result.put("evacuationRecommendations", evacuationRecs);
        result.put("confidence", calculateConfidence());
        result.put("executionTime", System.currentTimeMillis() - startTime);

        context.setFloodRisk(floodProbability);
        context.setRiskLevel(riskLevel);

        log.info("FloodPredictionAgent executed: Risk={}, Probability={}", riskLevel, floodProbability);
        return result;
    }

    private List<String> getAffectedZones(String region, double probability) {
        switch (region.toLowerCase()) {
            case "hyderabad":
                return Arrays.asList("Old City", "Secunderabad", "L.B. Nagar", "Charminar Area");
            case "mumbai":
                return Arrays.asList("Dharavi", "Kings Circle", "Bandra", "Marine Drive");
            case "chennai":
                return Arrays.asList("Velachery", "Besant Nagar", "OMR Area", "Guindy");
            default:
                return Arrays.asList("Downtown", "Industrial Area", "Residential Zone");
        }
    }

    private List<String> getEvacuationRecommendations(String riskLevel) {
        switch (riskLevel) {
            case "CRITICAL":
                return Arrays.asList("Immediate evacuation mandatory", "Use elevated routes",
                        "Avoid bridges and tunnels");
            case "HIGH":
                return Arrays.asList("Prepare for evacuation", "Keep valuables safe", "Monitor water levels");
            case "MEDIUM":
                return Arrays.asList("Stay alert", "Avoid flood-prone areas", "Keep emergency kit ready");
            default:
                return Arrays.asList("No immediate action needed", "Continue normal activities");
        }
    }
}
