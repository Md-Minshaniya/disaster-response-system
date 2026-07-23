package com.disaster.engine;

import com.disaster.agent.BaseAgent;
import com.disaster.model.DisasterContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AdaptivePriorityEngine {

    /**
     * Calculate composite priority score for an agent
     * Weights:
     * - FloodRisk (40%)
     * - Population Density (30%)
     * - Resource Urgency (20%)
     * - Agent Confidence (10%)
     */
    public double calculatePriorityScore(BaseAgent agent, DisasterContext context) {
        double floodRiskWeight = 0.4;
        double popDensityWeight = 0.3;
        double resourceUrgencyWeight = 0.2;
        double confidenceWeight = 0.1;

        double floodRiskScore = context.getFloodRisk() != null ? context.getFloodRisk() : 0;
        double popDensity = context.getPopulationDensity();
        double resourcesNeeded = context.getTotalResourcesNeeded();
        double resourceUrgency = 1.0 - (Math.min(context.getAvailableTeams() + context.getAvailableBoats(), 
                                                 resourcesNeeded) / resourcesNeeded);
        double agentConfidence = agent.calculateConfidence();

        double compositScore = (floodRiskScore * floodRiskWeight) +
                              (popDensity * popDensityWeight) +
                              (resourceUrgency * resourceUrgencyWeight) +
                              (agentConfidence * confidenceWeight);

        log.debug("Priority calculation for {}: floodRisk={}, popDensity={}, resourceUrgency={}, confidence={}",
                agent.getAgentName(), floodRiskScore, popDensity, resourceUrgency, agentConfidence);

        return Math.min(compositScore, 1.0);
    }

    /**
     * Get agents sorted by priority (highest first)
     */
    public List<BaseAgent> getPrioritizedAgents(List<BaseAgent> agents, DisasterContext context) {
        List<AgentPriority> agentPriorities = agents.stream()
            .map(agent -> new AgentPriority(agent, calculatePriorityScore(agent, context)))
            .sorted((a, b) -> Double.compare(b.score, a.score))
            .collect(Collectors.toList());

        log.info("Prioritized agent order:");
        agentPriorities.forEach(ap -> 
            log.info("  {} - Priority Score: {}", ap.agent.getAgentName(), String.format("%.4f", ap.score))
        );

        return agentPriorities.stream().map(ap -> ap.agent).collect(Collectors.toList());
    }

    /**
     * Compare adaptive priority with fixed priority
     */
    public Map<String, Object> compareWithFixedPriority(List<BaseAgent> agents, DisasterContext context) {
        Map<String, Object> comparison = new LinkedHashMap<>();

        List<BaseAgent> adaptivePriority = getPrioritizedAgents(agents, context);
        List<BaseAgent> fixedPriority = Arrays.asList(agents.get(0), agents.get(1), agents.get(2), agents.get(3));

        List<Map<String, String>> adaptiveOrder = adaptivePriority.stream()
            .map(a -> Map.of("agent", a.getAgentName(), "priority", 
                String.format("%.4f", calculatePriorityScore(a, context))))
            .collect(Collectors.toList());

        List<String> fixedOrder = fixedPriority.stream()
            .map(BaseAgent::getAgentName)
            .collect(Collectors.toList());

        comparison.put("adaptiveOrder", adaptiveOrder);
        comparison.put("fixedOrder", fixedOrder);
        comparison.put("difference", !adaptivePriority.equals(fixedPriority));
        comparison.put("context", Map.of(
            "region", context.getRegion(),
            "rainfall", context.getRainfall(),
            "riverLevel", context.getRiverLevel(),
            "population", context.getPopulation(),
            "floodRisk", context.getFloodRisk()
        ));

        return comparison;
    }

    private static class AgentPriority {
        BaseAgent agent;
        double score;

        AgentPriority(BaseAgent agent, double score) {
            this.agent = agent;
            this.score = score;
        }
    }
}
