package com.disaster.service;

import com.disaster.model.Simulation;
import com.disaster.model.AgentExecution;
import com.disaster.repository.SimulationRepository;
import com.disaster.repository.AgentExecutionRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ReportService {
    @Autowired
    private SimulationRepository simulationRepository;

    @Autowired
    private AgentExecutionRepository agentExecutionRepository;

    /**
     * Generate PDF report for a simulation using PDFBox
     */
    public byte[] generatePdfReport(Long simulationId) throws IOException {
        Simulation simulation = simulationRepository.findById(simulationId)
                .orElseThrow(() -> new RuntimeException("Simulation not found"));

        List<AgentExecution> executions = agentExecutionRepository.findBySimulationId(simulationId);

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float yPosition = 750;
            contentStream.setFont(PDType1Font.HELVETICA, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("DISASTER RESPONSE SIMULATION REPORT");
            contentStream.endText();

            yPosition -= 30;
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(
                    "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Simulation ID: " + simulationId);
            contentStream.endText();

            // Simulation Details Section
            yPosition -= 30;
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("SIMULATION DETAILS");
            contentStream.endText();

            yPosition -= 15;
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            String[][] details = {
                    { "Region", simulation.getRegion() },
                    { "Risk Level", simulation.getRiskLevel() },
                    { "Rainfall (mm)", String.format("%.2f", simulation.getRainfall()) },
                    { "River Level (m)", String.format("%.2f", simulation.getRiverLevel()) },
                    { "Population", String.format("%,d", simulation.getPopulation()) },
                    { "Flood Risk", String.format("%.2f", simulation.getFloodRisk()) },
                    { "Response Time (ms)", simulation.getResponseTimeMs().toString() },
                    { "Adaptive Priority", simulation.getAdaptivePriorityUsed() ? "Yes" : "No" }
            };

            for (String[] detail : details) {
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(detail[0] + ": " + detail[1]);
                contentStream.endText();
                yPosition -= 12;
            }

            // Agent Executions Section
            if (!executions.isEmpty()) {
                yPosition -= 15;
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("AGENT EXECUTIONS");
                contentStream.endText();

                yPosition -= 15;
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                for (AgentExecution exec : executions) {
                    String resultText = exec.getResult() != null
                            ? (exec.getResult().length() > 50 ? exec.getResult().substring(0, 50) + "..."
                                    : exec.getResult())
                            : "N/A";

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(exec.getAgentName() + " | Priority: " +
                            String.format("%.4f", exec.getPriorityScore()) + " | Time: " + exec.getExecutionTimeMs()
                            + "ms");
                    contentStream.endText();
                    yPosition -= 10;

                    if (yPosition < 50) {
                        contentStream.endText();
                        PDPage newPage = new PDPage();
                        document.addPage(newPage);
                        contentStream.close();
                        try (PDPageContentStream newStream = new PDPageContentStream(document, newPage)) {
                            yPosition = 750;
                        }
                    }
                }
            }

            // Footer
            yPosition -= 20;
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("---");
            contentStream.endText();

            yPosition -= 10;
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("This is an automated report generated by the Disaster Response System");
            contentStream.endText();
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        log.info("PDF report generated for simulation {}", simulationId);
        return baos.toByteArray();
    }

    /**
     * Generate Excel report for all simulations
     */
    public byte[] generateExcelReport(List<Simulation> simulations) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        // Create Simulations sheet
        Sheet simSheet = workbook.createSheet("Simulations");
        Row header = simSheet.createRow(0);
        String[] headers = { "ID", "Region", "Rainfall (mm)", "River Level (m)", "Population",
                "Flood Risk", "Risk Level", "Response Time (ms)", "Adaptive Priority", "Created At" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (Simulation sim : simulations) {
            Row row = simSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sim.getId());
            row.createCell(1).setCellValue(sim.getRegion());
            row.createCell(2).setCellValue(sim.getRainfall());
            row.createCell(3).setCellValue(sim.getRiverLevel());
            row.createCell(4).setCellValue(sim.getPopulation());
            row.createCell(5).setCellValue(sim.getFloodRisk());
            row.createCell(6).setCellValue(sim.getRiskLevel());
            row.createCell(7).setCellValue(sim.getResponseTimeMs());
            row.createCell(8).setCellValue(sim.getAdaptivePriorityUsed() ? "Yes" : "No");
            row.createCell(9).setCellValue(sim.getCreatedAt().toString());
        }

        // Create Summary sheet
        Sheet summarySheet = workbook.createSheet("Summary");
        Long total = simulationRepository.getTotalSimulations();
        Double avgTime = simulationRepository.getAverageResponseTime();
        Double successRate = simulationRepository.getSuccessRate();
        Long critical = simulationRepository.countCriticalSimulations();

        summarySheet.createRow(0).createCell(0).setCellValue("SUMMARY STATISTICS");
        summarySheet.createRow(1).createCell(0).setCellValue("Total Simulations");
        summarySheet.createRow(1).createCell(1).setCellValue(total != null ? total : 0);
        summarySheet.createRow(2).createCell(0).setCellValue("Avg Response Time (ms)");
        summarySheet.createRow(2).createCell(1).setCellValue(avgTime != null ? avgTime : 0);
        summarySheet.createRow(3).createCell(0).setCellValue("Success Rate");
        summarySheet.createRow(3).createCell(1).setCellValue(successRate != null ? successRate * 100 : 0);
        summarySheet.createRow(4).createCell(0).setCellValue("Critical Incidents");
        summarySheet.createRow(4).createCell(1).setCellValue(critical != null ? critical : 0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        log.info("Excel report generated with {} simulations", simulations.size());
        return baos.toByteArray();
    }

    /**
     * Export agent executions to Excel for detailed analysis
     */
    public byte[] exportAgentExecutionsToExcel(Long simulationId) throws IOException {
        List<AgentExecution> executions = agentExecutionRepository.findBySimulationId(simulationId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Agent Executions");

        Row header = sheet.createRow(0);
        String[] headers = { "Agent Name", "Priority Score", "Execution Time (ms)", "Result" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (AgentExecution exec : executions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(exec.getAgentName());
            row.createCell(1).setCellValue(exec.getPriorityScore());
            row.createCell(2).setCellValue(exec.getExecutionTimeMs());
            row.createCell(3)
                    .setCellValue(exec.getResult() != null
                            ? (exec.getResult().length() > 100 ? exec.getResult().substring(0, 100) : exec.getResult())
                            : "");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        log.info("Agent executions exported for simulation {}", simulationId);
        return baos.toByteArray();
    }
}
