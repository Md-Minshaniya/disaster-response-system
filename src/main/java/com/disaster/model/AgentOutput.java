package com.disaster.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentOutput {
    private String agentId;
    private String agentName;
    private Long executionTimeMs;
    private Double priorityScore;
    private Double confidence;
    private String status;
    private String alert;
    private Object data;
    private String severity;
    private String timestamp;

    public static AgentOutput success(String agentId, String agentName, Object data, Long executionTime, Double priority) {
        return AgentOutput.builder()
                .agentId(agentId)
                .agentName(agentName)
                .executionTimeMs(executionTime)
                .priorityScore(priority)
                .status("SUCCESS")
                .data(data)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    public static AgentOutput error(String agentId, String agentName, String errorMessage, Long executionTime) {
        return AgentOutput.builder()
                .agentId(agentId)
                .agentName(agentName)
                .executionTimeMs(executionTime)
                .status("ERROR")
                .alert(errorMessage)
                .severity("LOW")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
