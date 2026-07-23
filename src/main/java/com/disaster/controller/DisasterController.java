package com.disaster.controller;

import com.disaster.model.DisasterContext;
import com.disaster.model.Simulation;
import com.disaster.model.AgentExecution;
import com.disaster.repository.SimulationRepository;
import com.disaster.repository.AgentExecutionRepository;
import com.disaster.agent.SupervisorAgent;
import com.disaster.service.AlertService;
import com.disaster.service.ReportService;
import com.disaster.service.BenchmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/disaster")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class DisasterController {

    @Autowired
    private SupervisorAgent supervisorAgent;

    @Autowired
    private SimulationRepository simulationRepository;

    @Autowired
    private AgentExecutionRepository agentExecutionRepository;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private BenchmarkService benchmarkService;

    /**
     * POST /api/disaster/simulate - Run disaster response simulation
     */
    @PostMapping("/simulate")
    public ResponseEntity<?> simulateDisaster(@RequestBody DisasterContext context) {
        try {
            log.info("Simulation started for region: {}", context.getRegion());
            
            long startTime = System.currentTimeMillis();
            Map<String, Object> result = supervisorAgent.orchestrateResponse(context);
            long totalTime = System.currentTimeMillis() - startTime;

            // Extract agent outputs for alert
            if (context.getFloodRisk() != null && context.getFloodRisk() > 0.7) {
                sendCriticalAlerts(context);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Simulation failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }

    /**
     * GET /api/disaster/history - Get all simulations
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Simulation> simulations = simulationRepository.findAll();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("totalRecords", simulations.size());
            response.put("data", simulations);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch history: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/history/{id} - Get single simulation with agent executions
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<?> getSimulationDetail(@PathVariable Long id) {
        try {
            Simulation simulation = simulationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Simulation not found"));
            
            List<AgentExecution> executions = agentExecutionRepository.findBySimulationId(id);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("status", "SUCCESS");
            response.put("simulation", simulation);
            response.put("agentExecutions", executions);
            response.put("executionCount", executions.size());
            response.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch simulation details: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/metrics - Get system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        try {
            Long totalSimulations = simulationRepository.getTotalSimulations();
            Double avgResponseTime = simulationRepository.getAverageResponseTime();
            Double successRate = simulationRepository.getSuccessRate();
            Long criticalIncidents = simulationRepository.countCriticalSimulations();

            Map<String, Object> metrics = new LinkedHashMap<>();
            metrics.put("totalSimulations", totalSimulations != null ? totalSimulations : 0);
            metrics.put("averageResponseTime", avgResponseTime != null ? avgResponseTime : 0);
            metrics.put("successRate", successRate != null ? String.format("%.2f%%", successRate * 100) : "0%");
            metrics.put("criticalIncidents", criticalIncidents != null ? criticalIncidents : 0);
            metrics.put("systemStatus", "OPERATIONAL");
            metrics.put("lastUpdated", LocalDateTime.now());

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Failed to fetch metrics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/export/pdf/{id} - Export simulation as PDF
     */
    @GetMapping("/export/pdf/{id}")
    public ResponseEntity<?> exportPdf(@PathVariable Long id) {
        try {
            byte[] pdfContent = reportService.generatePdfReport(id);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disaster_report_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfContent);
        } catch (Exception e) {
            log.error("Failed to generate PDF: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/export/excel - Export all simulations as Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<?> exportExcel() {
        try {
            List<Simulation> simulations = simulationRepository.findAll();
            byte[] excelContent = reportService.generateExcelReport(simulations);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=disaster_simulations.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);
        } catch (Exception e) {
            log.error("Failed to generate Excel: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/benchmark - Run benchmark comparison
     */
    @GetMapping("/benchmark")
    public ResponseEntity<?> runBenchmark(@RequestParam(defaultValue = "10") int iterations) {
        try {
            log.info("Running benchmark with {} iterations", iterations);
            Map<String, Object> results = benchmarkService.compareResults(iterations);
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Benchmark failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/map-data - Get GeoJSON for flood zones
     */
    @GetMapping("/map-data")
    public ResponseEntity<?> getMapData() {
        try {
            Map<String, Object> features = new LinkedHashMap<>();
            
            List<Map<String, Object>> zones = Arrays.asList(
                createFloodZone("Hyderabad", 17.3850, 78.4867, "MEDIUM"),
                createFloodZone("Mumbai", 19.0760, 72.8777, "HIGH"),
                createFloodZone("Chennai", 13.0827, 80.2707, "LOW")
            );

            features.put("type", "FeatureCollection");
            features.put("features", zones);

            return ResponseEntity.ok(features);
        } catch (Exception e) {
            log.error("Failed to fetch map data: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/health - Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Long totalSims = simulationRepository.getTotalSimulations();
            
            Map<String, Object> health = new LinkedHashMap<>();
            health.put("status", "UP");
            health.put("database", "CONNECTED");
            health.put("totalSimulations", totalSims != null ? totalSims : 0);
            health.put("timestamp", LocalDateTime.now());
            health.put("version", "1.0.0");

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed: {}", e.getMessage());
            return ResponseEntity.status(503).body(Map.of(
                "status", "DOWN",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * GET /api/disaster/agent-stats/{agentName} - Get statistics for specific agent
     */
    @GetMapping("/agent-stats/{agentName}")
    public ResponseEntity<?> getAgentStats(@PathVariable String agentName) {
        try {
            List<AgentExecution> executions = agentExecutionRepository.findByAgentName(agentName);
            Double avgTime = agentExecutionRepository.getAverageExecutionTimeByAgent(agentName);

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("agentName", agentName);
            stats.put("totalExecutions", executions.size());
            stats.put("averageExecutionTime", avgTime != null ? avgTime : 0);
            stats.put("executions", executions);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Failed to fetch agent stats: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Send critical alerts for high-risk scenarios
     */
    private void sendCriticalAlerts(DisasterContext context) {
        String[] emergencyNumbers = {"+91-9876543210", "+91-9123456789"};
        String[] emergencyEmails = {"emergency@disaster.gov.in", "alert@ndma.in"};
        
        String alertMessage = String.format(
            "🚨 CRITICAL FLOOD ALERT\n\n" +
            "Region: %s\n" +
            "Rainfall: %.2f mm\n" +
            "River Level: %.2f m\n" +
            "Population at Risk: %,d\n" +
            "Flood Risk: %.2f%%\n\n" +
            "IMMEDIATE EVACUATION RECOMMENDED",
            context.getRegion(), context.getRainfall(), context.getRiverLevel(),
            context.getPopulation(), context.getFloodRisk() * 100
        );

        alertService.sendEmergencyBroadcast(context.getRegion(), alertMessage, emergencyNumbers, emergencyEmails);
    }

    /**
     * Create a flood zone feature for map
     */
    private Map<String, Object> createFloodZone(String name, double lat, double lon, String riskLevel) {
        Map<String, Object> feature = new LinkedHashMap<>();
        feature.put("type", "Feature");
        
        Map<String, Object> geometry = new LinkedHashMap<>();
        geometry.put("type", "Point");
        geometry.put("coordinates", Arrays.asList(lon, lat));
        feature.put("geometry", geometry);

        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("name", name);
        properties.put("riskLevel", riskLevel);
        properties.put("color", getRiskColor(riskLevel));
        feature.put("properties", properties);

        return feature;
    }

    private String getRiskColor(String riskLevel) {
        return switch(riskLevel) {
            case "CRITICAL" -> "red";
            case "HIGH" -> "orange";
            case "MEDIUM" -> "yellow";
            default -> "green";
        };
    }
}
