package com.disaster.agent;

import com.disaster.model.DisasterContext;
import com.disaster.model.AgentOutput;
import com.disaster.model.Simulation;
import com.disaster.model.AgentExecution;
import com.disaster.repository.SimulationRepository;
import com.disaster.repository.AgentExecutionRepository;
import com.disaster.engine.AdaptivePriorityEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.*;
import java.time.LocalDateTime;

@Slf4j
@Component
public class SupervisorAgent {
    @Autowired
    private AdaptivePriorityEngine adaptivePriorityEngine;

    @Autowired
    private WeatherAgent weatherAgent;

    @Autowired
    private FloodPredictionAgent floodPredictionAgent;

    @Autowired
    private ResourceAllocationAgent resourceAllocationAgent;

    @Autowired
    private CommunicationAgent communicationAgent;

    @Autowired
    private SimulationRepository simulationRepository;

    @Autowired
    private AgentExecutionRepository agentExecutionRepository;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    public Map<String, Object> orchestrateResponse(DisasterContext context) {
        long orchestrationStart = System.currentTimeMillis();
        log.info("Starting orchestration for region: {}", context.getRegion());

        Map<String, Object> orchestrationResult = new LinkedHashMap<>();
        List<AgentOutput> agentOutputs = new ArrayList<>();
        List<BaseAgent> agents = Arrays.asList(
            weatherAgent,
            floodPredictionAgent,
            resourceAllocationAgent,
            communicationAgent
        );

        try {
            List<BaseAgent> prioritizedAgents = context.getAdaptivePriorityUsed() ? 
                adaptivePriorityEngine.getPrioritizedAgents(agents, context) :
                getFixedPriorityAgents(agents);

            for (BaseAgent agent : prioritizedAgents) {
                long agentStart = System.currentTimeMillis();
                double priority = agent.computePriorityScore(context);

                try {
                    Object agentResult = agent.execute(context);
                    long executionTime = System.currentTimeMillis() - agentStart;

                    AgentOutput output = AgentOutput.success(
                        agent.getAgentId(),
                        agent.getAgentName(),
                        agentResult,
                        executionTime,
                        priority
                    );
                    agentOutputs.add(output);

                    saveAgentExecution(context, agent.getAgentName(), executionTime, priority, 
                        toJson(agentResult));

                    sendWebSocketUpdate(agent.getAgentName(), output);
                    log.info("Agent {} executed in {}ms with priority {}", 
                        agent.getAgentName(), executionTime, priority);

                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - agentStart;
                    AgentOutput output = AgentOutput.error(
                        agent.getAgentId(),
                        agent.getAgentName(),
                        e.getMessage(),
                        executionTime
                    );
                    agentOutputs.add(output);
                    log.error("Error executing agent {}: {}", agent.getAgentName(), e.getMessage());
                }
            }

            long totalTime = System.currentTimeMillis() - orchestrationStart;

            orchestrationResult.put("status", "SUCCESS");
            orchestrationResult.put("region", context.getRegion());
            orchestrationResult.put("riskLevel", context.getRiskLevel());
            orchestrationResult.put("floodRisk", context.getFloodRisk());
            orchestrationResult.put("agentOutputs", agentOutputs);
            orchestrationResult.put("totalExecutionTime", totalTime);
            orchestrationResult.put("adaptivePriorityUsed", context.getAdaptivePriorityUsed());
            orchestrationResult.put("agentCount", agentOutputs.size());
            orchestrationResult.put("timestamp", LocalDateTime.now().toString());

            saveSimulation(context, totalTime);

        } catch (Exception e) {
            log.error("Orchestration failed: {}", e.getMessage());
            orchestrationResult.put("status", "ERROR");
            orchestrationResult.put("error", e.getMessage());
        }

        return orchestrationResult;
    }

    private void saveAgentExecution(DisasterContext context, String agentName, 
                                   Long executionTime, Double priority, String result) {
        try {
            AgentExecution execution = AgentExecution.builder()
                .simulationId(context.getSimulationId())
                .agentName(agentName)
                .executionTimeMs(executionTime)
                .priorityScore(priority)
                .result(result)
                .createdAt(LocalDateTime.now())
                .build();
            agentExecutionRepository.save(execution);
        } catch (Exception e) {
            log.error("Failed to save agent execution: {}", e.getMessage());
        }
    }

    private void saveSimulation(DisasterContext context, Long totalTime) {
        try {
            Simulation simulation = Simulation.builder()
                .region(context.getRegion())
                .rainfall(context.getRainfall())
                .riverLevel(context.getRiverLevel())
                .population(context.getPopulation())
                .floodRisk(context.getFloodRisk())
                .riskLevel(context.getRiskLevel())
                .responseTimeMs(totalTime)
                .adaptivePriorityUsed(context.getAdaptivePriorityUsed())
                .availableTeams(context.getAvailableTeams())
                .availableBoats(context.getAvailableBoats())
                .foodKits(context.getFoodKits())
                .createdAt(LocalDateTime.now())
                .build();
            Simulation saved = simulationRepository.save(simulation);
            context.setSimulationId(saved.getId());
            log.info("Simulation saved with ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Failed to save simulation: {}", e.getMessage());
        }
    }

    private void sendWebSocketUpdate(String agentName, AgentOutput output) {
        try {
            if (messagingTemplate != null) {
                Map<String, Object> update = new LinkedHashMap<>();
                update.put("agentName", agentName);
                update.put("status", output.getStatus());
                update.put("executionTime", output.getExecutionTimeMs());
                update.put("timestamp", output.getTimestamp());
                messagingTemplate.convertAndSend("/topic/progress", update);
            }
        } catch (Exception e) {
            log.debug("WebSocket update failed (may not be subscribed): {}", e.getMessage());
        }
    }

    private List<BaseAgent> getFixedPriorityAgents(List<BaseAgent> agents) {
        return Arrays.asList(weatherAgent, floodPredictionAgent, resourceAllocationAgent, communicationAgent);
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}
