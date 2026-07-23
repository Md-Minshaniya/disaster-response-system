package com.disaster.service;

import com.disaster.agent.*;
import com.disaster.engine.AdaptivePriorityEngine;
import com.disaster.model.DisasterContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Slf4j
@Service
public class BenchmarkService {
    @Autowired
    private WeatherAgent weatherAgent;

    @Autowired
    private FloodPredictionAgent floodPredictionAgent;

    @Autowired
    private ResourceAllocationAgent resourceAllocationAgent;

    @Autowired
    private CommunicationAgent communicationAgent;

    @Autowired
    private AdaptivePriorityEngine adaptivePriorityEngine;

    /**
     * Run benchmark with adaptive priority
     */
    public Map<String, Object> runAdaptivePriorityTest(int iterations) {
        log.info("Starting Adaptive Priority Benchmark - {} iterations", iterations);

        long totalTime = 0;
        int successCount = 0;
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            DisasterContext context = generateRandomContext();
            long startTime = System.currentTimeMillis();

            try {
                List<BaseAgent> agents = Arrays.asList(weatherAgent, floodPredictionAgent, 
                                                       resourceAllocationAgent, communicationAgent);
                List<BaseAgent> prioritized = adaptivePriorityEngine.getPrioritizedAgents(agents, context);

                for (BaseAgent agent : prioritized) {
                    agent.execute(context);
                }

                long executionTime = System.currentTimeMillis() - startTime;
                executionTimes.add(executionTime);
                totalTime += executionTime;
                successCount++;
            } catch (Exception e) {
                log.error("Iteration {} failed: {}", i, e.getMessage());
            }
        }

        return buildBenchmarkResult("Adaptive Priority", iterations, totalTime, successCount, executionTimes);
    }

    /**
     * Run benchmark with fixed priority
     */
    public Map<String, Object> runFixedPriorityTest(int iterations) {
        log.info("Starting Fixed Priority Benchmark - {} iterations", iterations);

        long totalTime = 0;
        int successCount = 0;
        List<Long> executionTimes = new ArrayList<>();

        for (int i = 0; i < iterations; i++) {
            DisasterContext context = generateRandomContext();
            long startTime = System.currentTimeMillis();

            try {
                // Fixed order: Weather -> Flood -> Resource -> Communication
                weatherAgent.execute(context);
                floodPredictionAgent.execute(context);
                resourceAllocationAgent.execute(context);
                communicationAgent.execute(context);

                long executionTime = System.currentTimeMillis() - startTime;
                executionTimes.add(executionTime);
                totalTime += executionTime;
                successCount++;
            } catch (Exception e) {
                log.error("Iteration {} failed: {}", i, e.getMessage());
            }
        }

        return buildBenchmarkResult("Fixed Priority", iterations, totalTime, successCount, executionTimes);
    }

    /**
     * Compare adaptive vs fixed priority
     */
    public Map<String, Object> compareResults(int iterations) {
        log.info("Starting Benchmark Comparison - {} iterations", iterations);

        Map<String, Object> adaptiveResult = runAdaptivePriorityTest(iterations);
        Map<String, Object> fixedResult = runFixedPriorityTest(iterations);

        Map<String, Object> comparison = new LinkedHashMap<>();
        
        long adaptiveAvgTime = (Long) adaptiveResult.get("averageExecutionTime");
        long fixedAvgTime = (Long) fixedResult.get("averageExecutionTime");
        double improvement = ((double)(fixedAvgTime - adaptiveAvgTime) / fixedAvgTime) * 100;

        comparison.put("adaptive", adaptiveResult);
        comparison.put("fixed", fixedResult);
        comparison.put("improvement", Map.of(
            "responseTimeReduction", String.format("%.2f%%", improvement),
            "adaptiveAvgTime", adaptiveAvgTime,
            "fixedAvgTime", fixedAvgTime,
            "timeSaved", (fixedAvgTime - adaptiveAvgTime) + "ms"
        ));
        comparison.put("winner", improvement > 0 ? "ADAPTIVE PRIORITY" : "FIXED PRIORITY");

        log.info("Benchmark comparison complete. Improvement: {}", String.format("%.2f%%", improvement));
        return comparison;
    }

    /**
     * Run comprehensive benchmark with multiple metrics
     */
    public Map<String, Object> runComprehensiveBenchmark(int iterations) {
        Map<String, Object> results = new LinkedHashMap<>();

        // Run both tests
        Map<String, Object> comparison = compareResults(iterations);
        results.putAll(comparison);

        // Add resource utilization metrics
        Map<String, Object> resourceMetrics = calculateResourceMetrics(iterations);
        results.put("resourceMetrics", resourceMetrics);

        // Add task completion rates
        Map<String, Object> completionRates = calculateCompletionRates(iterations);
        results.put("completionRates", completionRates);

        return results;
    }

    private Map<String, Object> calculateResourceMetrics(int iterations) {
        double adaptiveResourceUtil = 0;
        double fixedResourceUtil = 0;

        for (int i = 0; i < iterations; i++) {
            DisasterContext context = generateRandomContext();
            
            List<BaseAgent> agents = Arrays.asList(weatherAgent, floodPredictionAgent, 
                                                   resourceAllocationAgent, communicationAgent);
            List<BaseAgent> adaptiveOrder = adaptivePriorityEngine.getPrioritizedAgents(agents, context);
            
            adaptiveResourceUtil += (1.0 / adaptiveOrder.size()) * 100;
            fixedResourceUtil += (1.0 / 4) * 100;
        }

        return Map.of(
            "adaptiveUtilization", String.format("%.2f%%", adaptiveResourceUtil / iterations),
            "fixedUtilization", String.format("%.2f%%", fixedResourceUtil / iterations)
        );
    }

    private Map<String, Object> calculateCompletionRates(int iterations) {
        int adaptiveSuccess = 0;
        int fixedSuccess = 0;

        for (int i = 0; i < iterations; i++) {
            DisasterContext context = generateRandomContext();
            
            try {
                List<BaseAgent> agents = Arrays.asList(weatherAgent, floodPredictionAgent, 
                                                       resourceAllocationAgent, communicationAgent);
                List<BaseAgent> adaptiveOrder = adaptivePriorityEngine.getPrioritizedAgents(agents, context);
                for (BaseAgent agent : adaptiveOrder) {
                    agent.execute(context);
                }
                adaptiveSuccess++;
            } catch (Exception e) {
                log.debug("Adaptive execution failed: {}", e.getMessage());
            }

            try {
                weatherAgent.execute(context);
                floodPredictionAgent.execute(context);
                resourceAllocationAgent.execute(context);
                communicationAgent.execute(context);
                fixedSuccess++;
            } catch (Exception e) {
                log.debug("Fixed execution failed: {}", e.getMessage());
            }
        }

        return Map.of(
            "adaptiveCompletionRate", String.format("%.2f%%", (adaptiveSuccess / (double)iterations) * 100),
            "fixedCompletionRate", String.format("%.2f%%", (fixedSuccess / (double)iterations) * 100)
        );
    }

    private DisasterContext generateRandomContext() {
        Random random = new Random();
        return DisasterContext.builder()
                .region(getRandomRegion())
                .rainfall(random.nextDouble() * 200)
                .riverLevel(random.nextDouble() * 12)
                .population(1000000 + random.nextInt(11000000))
                .availableTeams(random.nextInt(20))
                .availableBoats(random.nextInt(15))
                .foodKits(random.nextInt(10000))
                .adaptivePriorityUsed(true)
                .build();
    }

    private String getRandomRegion() {
        String[] regions = {"Hyderabad", "Mumbai", "Chennai"};
        return regions[new Random().nextInt(regions.length)];
    }

    private Map<String, Object> buildBenchmarkResult(String name, int iterations, long totalTime, 
                                                     int successCount, List<Long> executionTimes) {
        long avgTime = totalTime / iterations;
        long minTime = executionTimes.stream().mapToLong(Long::longValue).min().orElse(0);
        long maxTime = executionTimes.stream().mapToLong(Long::longValue).max().orElse(0);

        return Map.of(
            "benchmarkName", name,
            "iterations", iterations,
            "successCount", successCount,
            "failureCount", iterations - successCount,
            "totalExecutionTime", totalTime + "ms",
            "averageExecutionTime", avgTime,
            "minExecutionTime", minTime,
            "maxExecutionTime", maxTime,
            "successRate", String.format("%.2f%%", (successCount / (double)iterations) * 100)
        );
    }
}
