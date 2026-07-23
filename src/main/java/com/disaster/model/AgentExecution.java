package com.disaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Entity
@Table(name = "agent_executions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simulation_id")
    private Long simulationId;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "priority_score")
    private Double priorityScore;

    @Column(columnDefinition = "LONGTEXT")
    private String result;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
