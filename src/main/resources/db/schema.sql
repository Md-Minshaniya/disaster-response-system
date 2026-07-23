-- Disaster Response System Database Schema
-- MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS disaster_db;
USE disaster_db;

-- Simulations Table
CREATE TABLE IF NOT EXISTS simulations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    region VARCHAR(100) NOT NULL,
    rainfall DOUBLE NOT NULL DEFAULT 0,
    river_level DOUBLE NOT NULL DEFAULT 0,
    population INT NOT NULL DEFAULT 0,
    flood_risk DOUBLE,
    risk_level VARCHAR(20),
    response_time_ms BIGINT,
    adaptive_priority_used BOOLEAN DEFAULT TRUE,
    available_teams INT DEFAULT 0,
    available_boats INT DEFAULT 0,
    food_kits INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_region (region),
    INDEX idx_risk_level (risk_level),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Agent Executions Table
CREATE TABLE IF NOT EXISTS agent_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    simulation_id BIGINT,
    agent_name VARCHAR(100),
    execution_time_ms BIGINT,
    priority_score DOUBLE,
    result LONGTEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (simulation_id) REFERENCES simulations(id) ON DELETE CASCADE,
    INDEX idx_simulation_id (simulation_id),
    INDEX idx_agent_name (agent_name),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create views for analytics
CREATE OR REPLACE VIEW simulation_summary AS
SELECT 
    r.region,
    COUNT(*) as total_simulations,
    AVG(r.rainfall) as avg_rainfall,
    AVG(r.river_level) as avg_river_level,
    AVG(r.flood_risk) as avg_flood_risk,
    AVG(r.response_time_ms) as avg_response_time,
    MIN(r.created_at) as first_simulation,
    MAX(r.created_at) as last_simulation
FROM simulations r
GROUP BY r.region;

CREATE OR REPLACE VIEW agent_performance AS
SELECT 
    ae.agent_name,
    COUNT(*) as execution_count,
    AVG(ae.execution_time_ms) as avg_execution_time,
    MIN(ae.execution_time_ms) as min_execution_time,
    MAX(ae.execution_time_ms) as max_execution_time,
    AVG(ae.priority_score) as avg_priority_score
FROM agent_executions ae
GROUP BY ae.agent_name;

CREATE OR REPLACE VIEW risk_distribution AS
SELECT 
    risk_level,
    COUNT(*) as count,
    ROUND(COUNT(*) * 100 / (SELECT COUNT(*) FROM simulations), 2) as percentage
FROM simulations
WHERE risk_level IS NOT NULL
GROUP BY risk_level
ORDER BY FIELD(risk_level, 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW');

-- Insert sample data
INSERT INTO simulations (region, rainfall, river_level, population, flood_risk, risk_level, response_time_ms, adaptive_priority_used, available_teams, available_boats, food_kits)
VALUES 
    ('Hyderabad', 87.5, 6.5, 8500000, 0.52, 'MEDIUM', 2150, TRUE, 10, 8, 5000),
    ('Mumbai', 120.0, 8.2, 12400000, 0.74, 'CRITICAL', 1890, TRUE, 18, 14, 9500),
    ('Chennai', 45.2, 4.1, 4700000, 0.28, 'LOW', 1250, TRUE, 5, 3, 2000);
