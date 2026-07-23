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
public class WeatherAgent extends BaseAgent {
    public static final String AGENT_ID = "🌤️ Weather Monitor";
    public static final String AGENT_NAME = "Weather Agent";

    public WeatherAgent() {
        super(AGENT_ID, AGENT_NAME);
    }

    @Override
    public double computePriorityScore(DisasterContext context) {
        double rainfallScore = Math.min(context.getRainfall() / 150.0, 1.0);
        return rainfallScore;
    }

    @Override
    public Object execute(DisasterContext context) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();
        String severity;
        String alert;

        if (context.getRainfall() > 100) {
            severity = "EXTREME";
            alert = "🚨 EXTREME rainfall detected! " + String.format("%.2f", context.getRainfall())
                    + "mm | Immediate evacuation required";
        } else if (context.getRainfall() > 50) {
            severity = "SEVERE";
            alert = "⚠️ SEVERE rainfall detected! " + String.format("%.2f", context.getRainfall())
                    + "mm | High risk alert issued";
        } else if (context.getRainfall() > 25) {
            severity = "MODERATE";
            alert = "⚡ MODERATE rainfall detected " + String.format("%.2f", context.getRainfall())
                    + "mm | Monitor situation closely";
        } else {
            severity = "LOW";
            alert = "☀️ Normal weather conditions. Rainfall: " + String.format("%.2f", context.getRainfall()) + "mm";
        }

        result.put("rainfall", context.getRainfall());
        result.put("prediction",
                "Next 24 hours: " + (context.getRainfall() > 50 ? "Continuous heavy rain" : "Variable conditions"));
        result.put("severity", severity);
        result.put("alert", alert);
        result.put("confidence", calculateConfidence());
        result.put("executionTime", System.currentTimeMillis() - startTime);

        log.info("WeatherAgent executed: {}", result);
        return result;
    }
}
