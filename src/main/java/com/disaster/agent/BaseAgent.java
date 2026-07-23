package com.disaster.agent;

import com.disaster.model.DisasterContext;
import lombok.Getter;
import lombok.Setter;
import java.util.Random;

@Getter
@Setter
public abstract class BaseAgent {
    protected String agentId;
    protected String agentName;
    protected Random random = new Random();

    public BaseAgent(String agentId, String agentName) {
        this.agentId = agentId;
        this.agentName = agentName;
    }

    /**
     * Compute priority score for this agent based on disaster context
     * 
     * @param context Current disaster context
     * @return Priority score (0-1)
     */
    public abstract double computePriorityScore(DisasterContext context);

    /**
     * Execute agent's primary function
     * 
     * @param context Current disaster context
     * @return Agent output/result
     */
    public abstract Object execute(DisasterContext context);

    /**
     * Calculate agent's confidence level (0.5-1.0)
     * 
     * @return Confidence score
     */
    public double calculateConfidence() {
        return 0.5 + (random.nextDouble() * 0.5);
    }

    /**
     * Get agent description
     * 
     * @return String representation
     */
    @Override
    public String toString() {
        return String.format("Agent[id=%s, name=%s]", agentId, agentName);
    }
}
