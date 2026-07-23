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
public class CommunicationAgent extends BaseAgent {
    public static final String AGENT_ID = "📢 Emergency Comms";
    public static final String AGENT_NAME = "Communication Agent";

    public CommunicationAgent() {
        super(AGENT_ID, AGENT_NAME);
    }

    @Override
    public double computePriorityScore(DisasterContext context) {
        return (context.getFloodRisk() != null ? context.getFloodRisk() : 0) * 0.8;
    }

    @Override
    public Object execute(DisasterContext context) {
        long startTime = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();

        String riskLevel = context.getRiskLevel();
        String urgency = getUrgencyLevel(riskLevel);
        String alert = generateAlert(context, riskLevel);
        List<String> evacuationInstructions = generateEvacuationInstructions(context, riskLevel);
        List<String> emergencyContacts = getEmergencyContacts(context.getRegion());

        result.put("riskLevel", riskLevel);
        result.put("urgencyLevel", urgency);
        result.put("alert", alert);
        result.put("evacuationInstructions", evacuationInstructions);
        result.put("emergencyContacts", emergencyContacts);
        result.put("messageChannels", Arrays.asList("SMS", "Email", "WhatsApp", "Mobile App"));
        result.put("confidence", calculateConfidence());
        result.put("executionTime", System.currentTimeMillis() - startTime);

        log.info("CommunicationAgent executed: Urgency={}, RiskLevel={}", urgency, riskLevel);
        return result;
    }

    private String getUrgencyLevel(String riskLevel) {
        switch (riskLevel) {
            case "CRITICAL":
                return "🚨 CRITICAL - IMMEDIATE ACTION";
            case "HIGH":
                return "⚠️ HIGH - PREPARE FOR EVACUATION";
            case "MEDIUM":
                return "⚡ MEDIUM - STAY ALERT";
            default:
                return "ℹ️ LOW - MONITOR SITUATION";
        }
    }

    private String generateAlert(DisasterContext context, String riskLevel) {
        String riskEmoji = switch (riskLevel) {
            case "CRITICAL" -> "🔴";
            case "HIGH" -> "🟠";
            case "MEDIUM" -> "🟡";
            default -> "🟢";
        };

        return String.format("%s DISASTER ALERT for %s | Rainfall: %.2fmm | River: %.2fm | " +
                "Population at Risk: %d | Risk Level: %s",
                riskEmoji, context.getRegion(), context.getRainfall(),
                context.getRiverLevel(), context.getPopulation(), riskLevel);
    }

    private List<String> generateEvacuationInstructions(DisasterContext context, String riskLevel) {
        if ("CRITICAL".equals(riskLevel)) {
            return Arrays.asList(
                    "1. Evacuate immediately to designated safe zones",
                    "2. Take essential documents and valuables",
                    "3. Help elderly and disabled persons",
                    "4. Avoid flooded routes and stay on elevated paths",
                    "5. Keep emergency contacts ready");
        } else if ("HIGH".equals(riskLevel)) {
            return Arrays.asList(
                    "1. Prepare evacuation materials",
                    "2. Keep vehicle fueled and ready",
                    "3. Monitor weather updates regularly",
                    "4. Identify safest evacuation route");
        } else {
            return Arrays.asList(
                    "1. Stay informed about weather updates",
                    "2. Keep emergency kit handy",
                    "3. Avoid unnecessary travel in flood-prone areas");
        }
    }

    private List<String> getEmergencyContacts(String region) {
        return Arrays.asList(
                "🚨 Emergency: 108 (All India)",
                "📞 Police: 100",
                "🚒 Fire: 101",
                "🚑 Ambulance: 102",
                "🌐 Disaster Helpline: 1070",
                "📱 Local Control Room: " + getLocalNumber(region));
    }

    private String getLocalNumber(String region) {
        return switch (region.toLowerCase()) {
            case "hyderabad" -> "+91-40-23451234 (NDMA Hyderabad)";
            case "mumbai" -> "+91-22-23051000 (BMC Mumbai)";
            case "chennai" -> "+91-44-24313333 (NDMA Chennai)";
            default -> "+91-XXXX-XXXXXX (Local Control Room)";
        };
    }
}
