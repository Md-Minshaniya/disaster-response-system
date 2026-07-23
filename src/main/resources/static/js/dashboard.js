// Global Variables
let map;
let alertLog = [];
let currentSimulationId = null;

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
});

// Form submission
document.getElementById('simulationForm').addEventListener('submit', function(e) {
    e.preventDefault();
    runSimulation();
});

// Input change handlers for range displays
document.getElementById('rainfall').addEventListener('input', function() {
    document.getElementById('rainfallDisplay').textContent = this.value + ' mm';
});

document.getElementById('riverLevel').addEventListener('input', function() {
    document.getElementById('riverLevelDisplay').textContent = this.value + ' m';
});

document.getElementById('population').addEventListener('input', function() {
    const millions = (this.value / 1000000).toFixed(1);
    document.getElementById('populationDisplay').textContent = millions + 'M';
});

/**
 * Initialize the page
 */
function initializePage() {
    console.log('🚀 Initializing Disaster Response System...');
    
    initializeMap();
    loadMetrics();
    loadHistory();
    connectWebSocket();
    checkDarkMode();
    
    console.log('✅ Dashboard initialized');
}

/**
 * Initialize Leaflet map with flood zones
 */
function initializeMap() {
    if (map) {
        map.remove();
    }

    map = L.map('map').setView([20, 78], 5);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
    }).addTo(map);

    // Fetch and add flood zones
    fetch('/api/disaster/map-data')
        .then(response => response.json())
        .then(data => {
            data.features.forEach(feature => {
                const coords = feature.geometry.coordinates;
                const props = feature.properties;
                const color = props.color === 'red' ? '#f56565' : 
                             props.color === 'orange' ? '#ed8936' : 
                             props.color === 'yellow' ? '#ecc94b' : '#48bb78';

                const marker = L.circleMarker([coords[1], coords[0]], {
                    radius: 15,
                    fillColor: color,
                    color: color,
                    weight: 2,
                    opacity: 0.8,
                    fillOpacity: 0.6
                }).addTo(map);

                marker.bindPopup(`<strong>${props.name}</strong><br/>Risk: ${props.riskLevel}`);
            });
        })
        .catch(error => console.error('❌ Error loading map data:', error));
}

/**
 * Load system metrics
 */
function loadMetrics() {
    fetch('/api/disaster/metrics')
        .then(response => response.json())
        .then(data => {
            document.getElementById('totalSimulations').textContent = data.totalSimulations || 0;
            document.getElementById('avgResponseTime').textContent = 
                (data.averageResponseTime || 0).toFixed(0) + 'ms';
            document.getElementById('successRate').textContent = data.successRate || '0%';
            document.getElementById('criticalCases').textContent = data.criticalIncidents || 0;
        })
        .catch(error => console.error('❌ Error loading metrics:', error));
}

/**
 * Load simulation history
 */
function loadHistory() {
    fetch('/api/disaster/history')
        .then(response => response.json())
        .then(data => {
            const tableHtml = `
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Region</th>
                            <th>Rainfall (mm)</th>
                            <th>Risk Level</th>
                            <th>Response Time (ms)</th>
                            <th>Created</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${data.data.slice(0, 20).map(sim => `
                            <tr onclick="viewSimulationDetails(${sim.id})">
                                <td>${sim.id}</td>
                                <td>${sim.region}</td>
                                <td>${sim.rainfall.toFixed(2)}</td>
                                <td><span class="risk-badge ${sim.riskLevel?.toLowerCase() || 'low'}">${sim.riskLevel || 'N/A'}</span></td>
                                <td>${sim.responseTimeMs || 'N/A'}</td>
                                <td>${new Date(sim.createdAt).toLocaleString()}</td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            `;
            document.getElementById('historyTable').innerHTML = tableHtml;
        })
        .catch(error => console.error('❌ Error loading history:', error));
}

/**
 * Run simulation
 */
function runSimulation() {
    const formData = {
        region: document.getElementById('region').value,
        rainfall: parseFloat(document.getElementById('rainfall').value),
        riverLevel: parseFloat(document.getElementById('riverLevel').value),
        population: parseInt(document.getElementById('population').value),
        availableTeams: parseInt(document.getElementById('availableTeams').value),
        availableBoats: parseInt(document.getElementById('availableBoats').value),
        foodKits: parseInt(document.getElementById('foodKits').value),
        adaptivePriorityUsed: document.getElementById('adaptivePriority').checked
    };

    showLoading(true);

    fetch('/api/disaster/simulate', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
    .then(response => response.json())
    .then(data => {
        showLoading(false);
        
        if (data.status === 'SUCCESS') {
            currentSimulationId = data.simulationId;
            displayResults(data);
            showToast('✅ Simulation completed successfully!', 'success');
            
            // Auto refresh metrics
            setTimeout(() => {
                loadMetrics();
                loadHistory();
            }, 1000);
        } else {
            showToast('❌ Simulation failed: ' + data.error, 'error');
        }
    })
    .catch(error => {
        console.error('❌ Error:', error);
        showLoading(false);
        showToast('❌ Error running simulation', 'error');
    });
}

/**
 * Display simulation results
 */
function displayResults(data) {
    const resultsDiv = document.getElementById('results');
    const resultsContent = document.getElementById('resultsContent');

    let html = `
        <div class="result-item">
            <h3>📊 Simulation Summary</h3>
            <p><strong>Region:</strong> ${data.region}</p>
            <p><strong>Risk Level:</strong> <span class="risk-badge ${data.riskLevel?.toLowerCase() || 'low'}">${data.riskLevel || 'N/A'}</span></p>
            <p><strong>Flood Risk:</strong> ${(data.floodRisk * 100).toFixed(2)}%</p>
            <p><strong>Total Execution Time:</strong> ${data.totalExecutionTime}ms</p>
            <p><strong>Algorithm:</strong> ${data.adaptivePriorityUsed ? '🧠 Adaptive Priority' : '📋 Fixed Priority'}</p>
        </div>
    `;

    if (data.agentOutputs && data.agentOutputs.length > 0) {
        html += '<h3>🤖 Agent Executions</h3>';
        data.agentOutputs.forEach(agent => {
            const status = agent.status === 'SUCCESS' ? '✅' : '❌';
            html += `
                <div class="result-item">
                    <h3>${agent.agentName}</h3>
                    <p><strong>Status:</strong> ${status} ${agent.status}</p>
                    <p><strong>Priority Score:</strong> ${agent.priorityScore?.toFixed(4) || 'N/A'}</p>
                    <p><strong>Execution Time:</strong> ${agent.executionTimeMs}ms</p>
                    <div class="result-data">${JSON.stringify(agent.data, null, 2)}</div>
                </div>
            `;
        });
    }

    resultsContent.innerHTML = html;
    resultsDiv.classList.remove('hidden');
    resultsDiv.scrollIntoView({ behavior: 'smooth' });

    // Try to speak alert if critical
    if (data.floodRisk > 0.7) {
        const alertMsg = `CRITICAL ALERT: Flood risk ${(data.floodRisk * 100).toFixed(0)} percent in ${data.region}. Evacuation recommended.`;
        speakAlert(alertMsg);
    }
}

/**
 * View simulation details
 */
function viewSimulationDetails(id) {
    fetch(`/api/disaster/history/${id}`)
        .then(response => response.json())
        .then(data => {
            console.log('📋 Simulation Details:', data);
            showToast('ℹ️ Details loaded (check console)', 'success');
        })
        .catch(error => console.error('❌ Error:', error));
}

/**
 * Export to PDF
 */
function exportPDF() {
    if (!currentSimulationId) {
        const sim = prompt('Enter Simulation ID to export:');
        if (!sim) return;
        currentSimulationId = sim;
    }

    const url = `/api/disaster/export/pdf/${currentSimulationId}`;
    window.open(url, '_blank');
    showToast('📄 PDF export started', 'success');
}

/**
 * Export to Excel
 */
function exportToExcel() {
    const url = '/api/disaster/export/excel';
    window.open(url, '_blank');
    showToast('📊 Excel export started', 'success');
}

/**
 * Run benchmark
 */
function runBenchmark() {
    showLoading(true);
    
    fetch('/api/disaster/benchmark?iterations=10')
        .then(response => response.json())
        .then(data => {
            showLoading(false);
            
            const resultsDiv = document.getElementById('results');
            const resultsContent = document.getElementById('resultsContent');

            let html = `
                <div class="result-item">
                    <h3>🏆 Benchmark Results</h3>
                    <p><strong>Winner:</strong> ${data.winner}</p>
                </div>
                <div class="result-item">
                    <h3>🧠 Adaptive Priority</h3>
                    <p><strong>Avg Time:</strong> ${data.adaptive.averageExecutionTime}ms</p>
                    <p><strong>Success Rate:</strong> ${data.adaptive.successRate}</p>
                </div>
                <div class="result-item">
                    <h3>📋 Fixed Priority</h3>
                    <p><strong>Avg Time:</strong> ${data.fixed.averageExecutionTime}ms</p>
                    <p><strong>Success Rate:</strong> ${data.fixed.successRate}</p>
                </div>
                <div class="result-item">
                    <h3>📈 Improvement</h3>
                    <p><strong>Response Time Reduction:</strong> ${data.improvement.responseTimeReduction}</p>
                    <p><strong>Time Saved:</strong> ${data.improvement.timeSaved}</p>
                </div>
            `;

            resultsContent.innerHTML = html;
            resultsDiv.classList.remove('hidden');
            resultsDiv.scrollIntoView({ behavior: 'smooth' });
            showToast('✅ Benchmark completed!', 'success');
        })
        .catch(error => {
            showLoading(false);
            console.error('❌ Error:', error);
            showToast('❌ Benchmark failed', 'error');
        });
}

/**
 * Send alert
 */
function sendAlert() {
    const phones = document.getElementById('alertRecipients').value
        .split(',')
        .map(p => p.trim())
        .filter(p => p);
    const emails = document.getElementById('alertEmails').value
        .split(',')
        .map(e => e.trim())
        .filter(e => e);
    const message = document.getElementById('alertMessage').value;

    if (!message) {
        showToast('⚠️ Please enter a message', 'warning');
        return;
    }

    // Log the alert
    const timestamp = new Date().toLocaleString();
    alertLog.unshift({
        time: timestamp,
        phones: phones.length,
        emails: emails.length,
        message: message.substring(0, 50) + '...'
    });

    // Update log
    updateAlertLog();

    // Simulate sending
    showToast(`✅ Alert sent to ${phones.length} phones and ${emails.length} emails`, 'success');
    
    // Console output
    console.log('📢 Alert Details:');
    console.log('  Phones:', phones);
    console.log('  Emails:', emails);
    console.log('  Message:', message);

    // Clear form
    document.getElementById('alertMessage').value = '';
}

/**
 * Update alert log
 */
function updateAlertLog() {
    const logHtml = alertLog.map((alert, index) => `
        <div class="alert-log-item">
            <div class="alert-timestamp">${alert.time}</div>
            <p>📱 ${alert.phones} phones | 📧 ${alert.emails} emails</p>
            <p>${alert.message}</p>
        </div>
    `).join('');
    
    document.getElementById('alertLogContent').innerHTML = logHtml || '<p>No alerts sent yet</p>';
}

/**
 * Show/hide loading spinner
 */
function showLoading(show) {
    document.getElementById('loading').classList.toggle('hidden', !show);
}

/**
 * Show toast notification
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

/**
 * Navigate between tabs
 */
function navigateTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName).classList.add('active');
}

/**
 * Toggle dark mode
 */
function toggleDarkMode() {
    document.body.classList.toggle('dark-mode');
    localStorage.setItem('darkMode', document.body.classList.contains('dark-mode'));
}

/**
 * Check dark mode on load
 */
function checkDarkMode() {
    if (localStorage.getItem('darkMode') === 'true') {
        document.body.classList.add('dark-mode');
    }
}

/**
 * Connect to WebSocket for real-time updates
 */
function connectWebSocket() {
    try {
        const ws = new WebSocket('ws://localhost:8080/ws');
        
        ws.onopen = function() {
            console.log('✅ WebSocket connected');
        };
        
        ws.onmessage = function(event) {
            const data = JSON.parse(event.data);
            console.log('📨 WebSocket message:', data);
            
            // Update agent status
            updateAgentStatus(data.agentName, data.status, data.executionTime);
        };
        
        ws.onerror = function(error) {
            console.warn('⚠️ WebSocket error:', error);
        };
        
        ws.onclose = function() {
            console.warn('⚠️ WebSocket disconnected');
        };
    } catch (error) {
        console.warn('⚠️ WebSocket not available:', error);
    }
}

/**
 * Update agent status on UI
 */
function updateAgentStatus(agentName, status, executionTime) {
    const statusMap = {
        'Weather Agent': 'weather',
        'Flood Prediction Agent': 'flood',
        'Resource Allocation Agent': 'resource',
        'Communication Agent': 'comm'
    };

    const prefix = statusMap[agentName];
    if (prefix) {
        const statusEl = document.getElementById(`${prefix}-status`);
        const timeEl = document.getElementById(`${prefix}-time`);
        
        if (statusEl) {
            statusEl.textContent = status;
            statusEl.classList.toggle('error', status === 'ERROR');
        }
        if (timeEl && executionTime) {
            timeEl.textContent = executionTime + 'ms';
        }
    }
}

/**
 * Speak alert using Web Speech API
 */
function speakAlert(message) {
    if ('speechSynthesis' in window) {
        const utterance = new SpeechSynthesisUtterance(message);
        utterance.rate = 0.9;
        utterance.pitch = 1;
        utterance.volume = 1;
        speechSynthesis.speak(utterance);
        console.log('🔊 Voice alert:', message);
    }
}

// Service Worker registration for PWA
if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('/sw.js').catch(err => {
        console.warn('⚠️ Service Worker registration failed:', err);
    });
}

console.log('✅ Dashboard JS loaded');
