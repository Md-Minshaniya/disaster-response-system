# 📡 API Testing Guide

## Using Postman Collection

Import the following requests in Postman:

### 1. Run Simulation - Hyderabad (Medium Risk)

**Request:**
```
POST http://localhost:8080/api/disaster/simulate
Content-Type: application/json

{
  "region": "Hyderabad",
  "rainfall": 87.5,
  "riverLevel": 6.5,
  "population": 8500000,
  "availableTeams": 10,
  "availableBoats": 8,
  "foodKits": 5000,
  "adaptivePriorityUsed": true
}
```

**Expected Response:** 2000-2500ms total execution time

---

### 2. Run Simulation - Mumbai (Critical Risk)

**Request:**
```
POST http://localhost:8080/api/disaster/simulate
Content-Type: application/json

{
  "region": "Mumbai",
  "rainfall": 150.0,
  "riverLevel": 10.5,
  "population": 12400000,
  "availableTeams": 18,
  "availableBoats": 14,
  "foodKits": 9500,
  "adaptivePriorityUsed": true
}
```

**Expected Response:** 🚨 CRITICAL - Voice alert triggered

---

### 3. Run Simulation - Chennai (Low Risk)

**Request:**
```
POST http://localhost:8080/api/disaster/simulate
Content-Type: application/json

{
  "region": "Chennai",
  "rainfall": 30.0,
  "riverLevel": 3.2,
  "population": 4700000,
  "availableTeams": 5,
  "availableBoats": 3,
  "foodKits": 2000,
  "adaptivePriorityUsed": true
}
```

---

### 4. Run Simulation - Fixed Priority (No Adaptive)

**Request:**
```
POST http://localhost:8080/api/disaster/simulate
Content-Type: application/json

{
  "region": "Hyderabad",
  "rainfall": 87.5,
  "riverLevel": 6.5,
  "population": 8500000,
  "availableTeams": 10,
  "availableBoats": 8,
  "foodKits": 5000,
  "adaptivePriorityUsed": false
}
```

**Expected Response:** Slower execution (fixed order: Weather→Flood→Resource→Comms)

---

### 5. Get All Simulations

**Request:**
```
GET http://localhost:8080/api/disaster/history
```

**Response:**
```json
{
  "status": "SUCCESS",
  "totalRecords": 3,
  "data": [
    {
      "id": 1,
      "region": "Hyderabad",
      "rainfall": 87.5,
      "riverLevel": 6.5,
      "population": 8500000,
      "floodRisk": 0.52,
      "riskLevel": "MEDIUM",
      "responseTimeMs": 2150,
      "adaptivePriorityUsed": true,
      "createdAt": "2026-06-12T10:30:45"
    },
    {
      "id": 2,
      "region": "Mumbai",
      "rainfall": 150.0,
      "riverLevel": 10.5,
      "population": 12400000,
      "floodRisk": 0.82,
      "riskLevel": "CRITICAL",
      "responseTimeMs": 1890,
      "adaptivePriorityUsed": true,
      "createdAt": "2026-06-12T10:35:20"
    },
    {
      "id": 3,
      "region": "Chennai",
      "rainfall": 30.0,
      "riverLevel": 3.2,
      "population": 4700000,
      "floodRisk": 0.22,
      "riskLevel": "LOW",
      "responseTimeMs": 1750,
      "adaptivePriorityUsed": true,
      "createdAt": "2026-06-12T10:40:15"
    }
  ],
  "timestamp": "2026-06-12T10:45:30"
}
```

---

### 6. Get Single Simulation Details

**Request:**
```
GET http://localhost:8080/api/disaster/history/1
```

**Response:**
```json
{
  "status": "SUCCESS",
  "simulation": {
    "id": 1,
    "region": "Hyderabad",
    "rainfall": 87.5,
    "riverLevel": 6.5,
    "population": 8500000,
    "floodRisk": 0.52,
    "riskLevel": "MEDIUM",
    "responseTimeMs": 2150,
    "adaptivePriorityUsed": true,
    "availableTeams": 10,
    "availableBoats": 8,
    "foodKits": 5000,
    "createdAt": "2026-06-12T10:30:45"
  },
  "agentExecutions": [
    {
      "id": 1,
      "simulationId": 1,
      "agentName": "Weather Agent",
      "executionTimeMs": 45,
      "priorityScore": 0.5833,
      "result": "{\"rainfall\":87.5,\"prediction\":\"Next 24 hours: Continuous heavy rain\",\"severity\":\"EXTREME\",\"alert\":\"🚨 EXTREME rainfall detected! 87.50mm | Immediate evacuation required\",\"confidence\":0.87}",
      "createdAt": "2026-06-12T10:30:45"
    },
    {
      "id": 2,
      "simulationId": 1,
      "agentName": "Flood Prediction Agent",
      "executionTimeMs": 52,
      "priorityScore": 0.52,
      "result": "{\"floodProbability\":\"52.00%\",\"riskLevel\":\"MEDIUM\",\"riverLevel\":6.5,\"affectedZones\":[\"Old City\",\"Secunderabad\",\"L.B. Nagar\",\"Charminar Area\"],\"evacuationRecommendations\":[\"Stay alert\",\"Avoid flood-prone areas\",\"Keep emergency kit ready\"]}",
      "createdAt": "2026-06-12T10:30:46"
    },
    {
      "id": 3,
      "simulationId": 1,
      "agentName": "Resource Allocation Agent",
      "executionTimeMs": 38,
      "priorityScore": 0.62,
      "result": "{\"riskLevel\":\"MEDIUM\",\"resourceMultiplier\":0.33,\"allocations\":{\"rescueTeams\":6,\"boats\":5,\"foodKits\":3333},\"allocationEfficiency\":\"37.33%\",\"totalResourcesNeeded\":8338}",
      "createdAt": "2026-06-12T10:30:47"
    },
    {
      "id": 4,
      "simulationId": 1,
      "agentName": "Communication Agent",
      "executionTimeMs": 35,
      "priorityScore": 0.42,
      "result": "{\"riskLevel\":\"MEDIUM\",\"urgencyLevel\":\"⚡ MEDIUM - STAY ALERT\",\"alert\":\"🟡 DISASTER ALERT for Hyderabad | Rainfall: 87.50mm | River: 6.50m | Population at Risk: 8500000 | Risk Level: MEDIUM\",\"evacuationInstructions\":[\"Stay informed about weather updates\",\"Keep emergency kit handy\",\"Avoid unnecessary travel in flood-prone areas\"],\"messageChannels\":[\"SMS\",\"Email\",\"WhatsApp\",\"Mobile App\"],\"emergencyContacts\":[\"🚨 Emergency: 108 (All India)\",\"📞 Police: 100\",\"🚒 Fire: 101\",\"🚑 Ambulance: 102\",\"🌐 Disaster Helpline: 1070\",\"📱 Local Control Room: +91-40-23451234 (NDMA Hyderabad)\"]}",
      "createdAt": "2026-06-12T10:30:47"
    }
  ],
  "executionCount": 4,
  "timestamp": "2026-06-12T10:45:30"
}
```

---

### 7. Get System Metrics

**Request:**
```
GET http://localhost:8080/api/disaster/metrics
```

**Response:**
```json
{
  "totalSimulations": 3,
  "averageResponseTime": 1930.0,
  "successRate": "100.00%",
  "criticalIncidents": 1,
  "systemStatus": "OPERATIONAL",
  "lastUpdated": "2026-06-12T10:45:30"
}
```

---

### 8. Run Benchmark (Adaptive vs Fixed)

**Request:**
```
GET http://localhost:8080/api/disaster/benchmark?iterations=5
```

**Response:**
```json
{
  "adaptive": {
    "benchmarkName": "Adaptive Priority",
    "iterations": 5,
    "successCount": 5,
    "failureCount": 0,
    "totalExecutionTime": "9450ms",
    "averageExecutionTime": 1890,
    "minExecutionTime": 1750,
    "maxExecutionTime": 2150,
    "successRate": "100.00%"
  },
  "fixed": {
    "benchmarkName": "Fixed Priority",
    "iterations": 5,
    "successCount": 5,
    "failureCount": 0,
    "totalExecutionTime": "10750ms",
    "averageExecutionTime": 2150,
    "minExecutionTime": 2000,
    "maxExecutionTime": 2450,
    "successRate": "100.00%"
  },
  "improvement": {
    "responseTimeReduction": "12.09%",
    "adaptiveAvgTime": 1890,
    "fixedAvgTime": 2150,
    "timeSaved": "260ms"
  },
  "winner": "ADAPTIVE PRIORITY"
}
```

---

### 9. Get Map Data (GeoJSON)

**Request:**
```
GET http://localhost:8080/api/disaster/map-data
```

**Response:**
```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [78.4867, 17.3850]
      },
      "properties": {
        "name": "Hyderabad",
        "riskLevel": "MEDIUM",
        "color": "yellow"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [72.8777, 19.0760]
      },
      "properties": {
        "name": "Mumbai",
        "riskLevel": "HIGH",
        "color": "orange"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [80.2707, 13.0827]
      },
      "properties": {
        "name": "Chennai",
        "riskLevel": "LOW",
        "color": "green"
      }
    }
  ]
}
```

---

### 10. Get Agent Statistics

**Request:**
```
GET http://localhost:8080/api/disaster/agent-stats/Weather%20Agent
```

**Response:**
```json
{
  "agentName": "Weather Agent",
  "totalExecutions": 4,
  "averageExecutionTime": 45.5,
  "executions": [
    {
      "id": 1,
      "simulationId": 1,
      "agentName": "Weather Agent",
      "executionTimeMs": 45,
      "priorityScore": 0.5833,
      "result": "{...}",
      "createdAt": "2026-06-12T10:30:45"
    }
  ]
}
```

---

### 11. Export to PDF

**Request:**
```
GET http://localhost:8080/api/disaster/export/pdf/1
```

**Response:** PDF file (application/pdf)

**Download using cURL:**
```bash
curl http://localhost:8080/api/disaster/export/pdf/1 \
  -H "Accept: application/pdf" \
  --output simulation_report_1.pdf
```

---

### 12. Export to Excel

**Request:**
```
GET http://localhost:8080/api/disaster/export/excel
```

**Response:** Excel file (application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)

**Download using cURL:**
```bash
curl http://localhost:8080/api/disaster/export/excel \
  --output simulations_export.xlsx
```

---

### 13. Health Check

**Request:**
```
GET http://localhost:8080/api/disaster/health
```

**Response:**
```json
{
  "status": "UP",
  "database": "CONNECTED",
  "totalSimulations": 3,
  "timestamp": "2026-06-12T10:45:30",
  "version": "1.0.0"
}
```

---

## cURL Command Examples

### Run Simulation (One-liner)
```bash
curl -X POST http://localhost:8080/api/disaster/simulate \
  -H "Content-Type: application/json" \
  -d '{"region":"Hyderabad","rainfall":87.5,"riverLevel":6.5,"population":8500000,"availableTeams":10,"availableBoats":8,"foodKits":5000,"adaptivePriorityUsed":true}' | jq .
```

### Get History with jq Formatting
```bash
curl -s http://localhost:8080/api/disaster/history | jq '.data[] | {id, region, riskLevel, responseTimeMs}'
```

### Run Benchmark with 20 Iterations
```bash
curl "http://localhost:8080/api/disaster/benchmark?iterations=20" | jq '.improvement'
```

### Download PDF Report
```bash
curl http://localhost:8080/api/disaster/export/pdf/1 -o report.pdf && ls -lh report.pdf
```

### Continuous Testing (Every 5 seconds)
```bash
while true; do
  curl -s http://localhost:8080/api/disaster/health | jq '.status'
  sleep 5
done
```

---

## Performance Test Scenarios

### Light Load (10 simulations)
```bash
for i in {1..10}; do
  curl -s -X POST http://localhost:8080/api/disaster/simulate \
    -H "Content-Type: application/json" \
    -d '{"region":"Hyderabad","rainfall":'$((RANDOM % 200))',"riverLevel":'$((RANDOM % 12 + 1))'.5,"population":8500000,"availableTeams":10,"availableBoats":8,"foodKits":5000,"adaptivePriorityUsed":true}' | jq '.totalExecutionTime'
done
```

### Stress Test (100 concurrent requests)
```bash
ab -n 100 -c 50 http://localhost:8080/api/disaster/history
```

### Load Test Pattern (Gradual increase)
```bash
for concurrency in 5 10 20 50 100; do
  echo "Testing with $concurrency concurrent requests..."
  ab -n 200 -c $concurrency -q http://localhost:8080/api/disaster/metrics
  sleep 5
done
```

---

## Expected Response Times

| Endpoint | Method | Avg Time | Max Time |
|----------|--------|----------|----------|
| /api/disaster/simulate | POST | 1800-2200ms | 3000ms |
| /api/disaster/history | GET | 50-150ms | 500ms |
| /api/disaster/metrics | GET | 30-100ms | 300ms |
| /api/disaster/health | GET | 10-50ms | 100ms |
| /api/disaster/benchmark?iterations=10 | GET | 20000-30000ms | 40000ms |
| /api/disaster/export/excel | GET | 500-2000ms | 5000ms |

---

## Common Issues & Solutions

| Issue | Cause | Solution |
|-------|-------|----------|
| 404 Not Found | Wrong endpoint | Check API documentation |
| 500 Internal Error | Database error | Check MySQL connection |
| Timeout | Long processing | Increase request timeout to 30s |
| CORS Error | Browser restriction | Access via same origin or whitelist in config |
| Out of Memory | Large export | Increase JVM heap: `-Xmx1024m` |

---

**API Testing Version**: 1.0.0  
**Last Updated**: 2026-06-12
