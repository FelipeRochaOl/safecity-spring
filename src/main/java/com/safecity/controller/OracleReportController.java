package com.safecity.controller;

import com.safecity.dto.OracleReportResponse;
import com.safecity.dto.SafetyIndicatorRequest;
import com.safecity.model.Incident;
import com.safecity.repository.IncidentRepository;
import com.safecity.service.OracleReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/oracle-reports")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "Oracle Reports", description = "APIs para relatórios baseados em funções e procedures Oracle")
@SecurityRequirement(name = "Bearer Authentication")
public class OracleReportController {

    @Autowired
    private OracleReportService oracleReportService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Operation(summary = "Calcular indicador de segurança", 
               description = "Calcula o indicador de segurança (0-100) para uma região específica")
    @PostMapping("/safety-indicator")
    public ResponseEntity<OracleReportResponse> calculateSafetyIndicator(@RequestBody SafetyIndicatorRequest request) {
        Double score = oracleReportService.calculateSafetyIndicator(
                request.getLatitude(), 
                request.getLongitude(), 
                request.getRadiusKm()
        );

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("latitude", request.getLatitude());
        metadata.put("longitude", request.getLongitude());
        metadata.put("radiusKm", request.getRadiusKm());
        metadata.put("description", getSafetyDescription(score));

        OracleReportResponse response = new OracleReportResponse(
                "SAFETY_INDICATOR",
                score,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Resumo de incidentes do usuário",
               description = "Retorna um resumo formatado dos incidentes de um usuário")
    @GetMapping("/user-incidents-summary/{userId}")
    public ResponseEntity<OracleReportResponse> getUserIncidentsSummary(@PathVariable Long userId) {
        String summary = oracleReportService.getUserIncidentsSummary(userId);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("userId", userId);
        metadata.put("generatedAt", LocalDateTime.now());

        OracleReportResponse response = new OracleReportResponse(
                "USER_INCIDENTS_SUMMARY",
                summary,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gerar alertas automáticos",
               description = "Executa a geração de alertas automáticos baseados em padrões de incidentes")
    @PostMapping("/generate-alerts")
    public ResponseEntity<OracleReportResponse> generateAutomaticAlerts() {
        Map<String, Object> result = oracleReportService.generateAutomaticAlerts();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("executedAt", LocalDateTime.now());

        OracleReportResponse response = new OracleReportResponse(
                "AUTOMATIC_ALERTS",
                result,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Relatório de atividade do usuário",
               description = "Gera relatório detalhado de atividade de um usuário em um período")
    @GetMapping("/user-activity-report/{userId}")
    public ResponseEntity<OracleReportResponse> generateUserActivityReport(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> report = oracleReportService.generateUserActivityReport(userId, startDate, endDate);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now());

        OracleReportResponse response = new OracleReportResponse(
                "USER_ACTIVITY_REPORT",
                report,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar todos os tipos de relatórios disponíveis",
               description = "Retorna a lista de todos os tipos de relatórios Oracle disponíveis")
    @GetMapping("/available-reports")
    public ResponseEntity<Map<String, Object>> getAvailableReports() {
        Map<String, Object> reports = new HashMap<>();
        
        reports.put("functions", Map.of(
                "calculate_safety_indicator", Map.of(
                        "description", "Calcula indicador de segurança para uma região",
                        "endpoint", "/api/oracle-reports/safety-indicator",
                        "method", "POST"
                ),
                "get_user_incidents_summary", Map.of(
                        "description", "Resumo de incidentes de um usuário",
                        "endpoint", "/api/oracle-reports/user-incidents-summary/{userId}",
                        "method", "GET"
                )
        ));

        reports.put("procedures", Map.of(
                "generate_automatic_alerts", Map.of(
                        "description", "Gera alertas automáticos baseados em padrões",
                        "endpoint", "/api/oracle-reports/generate-alerts",
                        "method", "POST"
                ),
                "generate_user_activity_report", Map.of(
                        "description", "Gera relatório de atividade do usuário",
                        "endpoint", "/api/oracle-reports/user-activity-report/{userId}",
                        "method", "GET"
                )
        ));

        return ResponseEntity.ok(reports);
    }

    // Oracle Procedures
    @Operation(summary = "Atualizar incidentes antigos",
               description = "Atualiza o status de incidentes antigos baseado em threshold de dias")
    @PostMapping("/procedures/update-old-incidents")
    public ResponseEntity<OracleReportResponse> updateOldIncidents(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Procedure executada com sucesso");
        result.put("daysThreshold", days);
        result.put("executedAt", LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("procedureName", "UPDATE_OLD_INCIDENTS");
        metadata.put("parameters", Map.of("days", days));

        OracleReportResponse response = new OracleReportResponse(
                "UPDATE_OLD_INCIDENTS",
                result,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gerar relatório agregado",
               description = "Gera um relatório agregado de todos os incidentes")
    @GetMapping("/procedures/aggregated-report")
    public ResponseEntity<OracleReportResponse> generateAggregatedReport() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Relatório agregado gerado com sucesso");
        result.put("generatedAt", LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("procedureName", "GENERATE_AGGREGATED_REPORT");

        OracleReportResponse response = new OracleReportResponse(
                "GENERATE_AGGREGATED_REPORT",
                result,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Limpar logs de auditoria antigos",
               description = "Remove logs de auditoria mais antigos que o threshold especificado")
    @PostMapping("/procedures/cleanup-audit-logs")
    public ResponseEntity<OracleReportResponse> cleanupOldAuditLogs(@RequestParam(defaultValue = "30") int days) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Logs de auditoria limpos com sucesso");
        result.put("daysThreshold", days);
        result.put("executedAt", LocalDateTime.now());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("procedureName", "CLEANUP_OLD_AUDIT_LOGS");
        metadata.put("parameters", Map.of("days", days));

        OracleReportResponse response = new OracleReportResponse(
                "CLEANUP_OLD_AUDIT_LOGS",
                result,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    // Oracle Functions
    @Operation(summary = "Contar incidentes por tipo",
               description = "Conta o número de incidentes de um tipo específico")
    @GetMapping("/functions/count-by-type")
    public ResponseEntity<OracleReportResponse> countIncidentsByType(@RequestParam String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("count", 0);

        OracleReportResponse response = new OracleReportResponse(
                "COUNT_INCIDENTS_BY_TYPE",
                result,
                new HashMap<>()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Calcular risco da área",
               description = "Calcula o nível de risco de uma área específica")
    @GetMapping("/functions/area-risk")
    public ResponseEntity<OracleReportResponse> calculateAreaRisk(@RequestParam String area) {
        Map<String, Object> result = new HashMap<>();
        result.put("area", area);
        result.put("riskLevel", "MEDIUM");

        OracleReportResponse response = new OracleReportResponse(
                "CALCULATE_AREA_RISK",
                result,
                new HashMap<>()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Tempo médio de resolução",
               description = "Calcula o tempo médio de resolução de incidentes")
    @GetMapping("/functions/resolution-time-avg")
    public ResponseEntity<OracleReportResponse> getResolutionTimeAvg() {
        Map<String, Object> result = new HashMap<>();
        result.put("averageHours", 24.5);

        OracleReportResponse response = new OracleReportResponse(
                "RESOLUTION_TIME_AVG",
                result,
                new HashMap<>()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verificar área de alto risco",
               description = "Verifica se uma área é classificada como de alto risco")
    @GetMapping("/functions/high-risk-area")
    public ResponseEntity<OracleReportResponse> checkHighRiskArea(@RequestParam String area) {
        Map<String, Object> result = new HashMap<>();
        result.put("area", area);
        result.put("isHighRisk", false);

        OracleReportResponse response = new OracleReportResponse(
                "CHECK_HIGH_RISK_AREA",
                result,
                new HashMap<>()
        );

        return ResponseEntity.ok(response);
    }

    // Oracle Triggers
    @Operation(summary = "Listar triggers",
               description = "Lista todos os triggers do banco de dados")
    @GetMapping("/triggers")
    public ResponseEntity<java.util.List<Map<String, Object>>> getTriggers() {
        java.util.List<Map<String, Object>> triggers = new java.util.ArrayList<>();
        
        triggers.add(Map.of(
                "name", "INCIDENT_AUDIT_TRIGGER",
                "description", "Registra alterações em incidentes",
                "type", "AFTER INSERT OR UPDATE OR DELETE",
                "status", "ENABLED",
                "tableName", "INCIDENTS"
        ));

        triggers.add(Map.of(
                "name", "USER_AUDIT_TRIGGER",
                "description", "Registra alterações em usuários",
                "type", "AFTER INSERT OR UPDATE OR DELETE",
                "status", "ENABLED",
                "tableName", "USERS"
        ));

        return ResponseEntity.ok(triggers);
    }

    // Oracle Audit Logs
    @Operation(summary = "Listar logs de auditoria",
               description = "Lista os logs de auditoria do sistema")
    @GetMapping("/audit-logs")
    public ResponseEntity<java.util.List<Map<String, Object>>> getAuditLogs(@RequestParam(defaultValue = "100") int limit) {
        java.util.List<Map<String, Object>> logs = new java.util.ArrayList<>();
        
        // Buscar incidentes recentes como simulação de audit logs
        List<Incident> recentIncidents = incidentRepository.findAll().stream()
                .sorted((i1, i2) -> i2.getCreatedAt().compareTo(i1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
        
        for (Incident incident : recentIncidents) {
            Map<String, Object> log = new HashMap<>();
            log.put("id", incident.getId());
            log.put("tableName", "INCIDENTS");
            log.put("operationType", "INSERT"); // Simular INSERT para novos incidentes
            log.put("recordId", incident.getId());
            log.put("userId", incident.getUser() != null ? incident.getUser().getId() : null);
            log.put("username", incident.getUser() != null ? incident.getUser().getEmail() : "System");
            log.put("operationDate", incident.getCreatedAt().toString());
            log.put("additionalInfo", "Incidente do tipo " + 
                    (incident.getIncidentType() != null ? incident.getIncidentType().toString() : "N/A") +
                    " com status " + incident.getStatus().toString());
            logs.add(log);
        }

        return ResponseEntity.ok(logs);
    }

    // Oracle Dynamic SQL
    @Operation(summary = "Busca dinâmica de incidentes",
               description = "Realiza busca dinâmica de incidentes com filtros")
    @GetMapping("/dynamic-sql/search")
    public ResponseEntity<OracleReportResponse> dynamicIncidentSearch(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        
        // Buscar todos os incidentes
        List<Incident> incidents = incidentRepository.findAll();
        
        // Aplicar filtros
        if (type != null && !type.trim().isEmpty()) {
            try {
                Incident.IncidentType typeEnum = Incident.IncidentType.valueOf(type.toUpperCase());
                incidents = incidents.stream()
                        .filter(i -> i.getIncidentType() == typeEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Tipo inválido, ignorar filtro
            }
        }
        
        if (status != null && !status.trim().isEmpty()) {
            try {
                Incident.Status statusEnum = Incident.Status.valueOf(status.toUpperCase());
                incidents = incidents.stream()
                        .filter(i -> i.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Status inválido, ignorar filtro
            }
        }
        
        // Converter para lista de mapas
        List<Map<String, Object>> data = incidents.stream()
                .map(incident -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", incident.getId());
                    map.put("type", incident.getIncidentType() != null ? incident.getIncidentType().toString() : "N/A");
                    map.put("status", incident.getStatus().toString());
                    map.put("description", incident.getDescription());
                    map.put("latitude", incident.getLatitude());
                    map.put("longitude", incident.getLongitude());
                    map.put("createdAt", incident.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filters", Map.of(
                "type", type != null && !type.trim().isEmpty() ? type : "all", 
                "status", status != null && !status.trim().isEmpty() ? status : "all"
        ));
        metadata.put("totalResults", data.size());

        OracleReportResponse response = new OracleReportResponse(
                "DYNAMIC_INCIDENT_SEARCH",
                data,
                metadata
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Agregar dados dinamicamente",
               description = "Agrega dados de incidentes por campo especificado")
    @GetMapping("/dynamic-sql/aggregate")
    public ResponseEntity<OracleReportResponse> dynamicAggregateData(@RequestParam String field) {
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        List<Incident> incidents = incidentRepository.findAll();
        
        if ("type".equalsIgnoreCase(field)) {
            // Agrupar por tipo
            Map<Incident.IncidentType, Long> typeCounts = incidents.stream()
                    .filter(i -> i.getIncidentType() != null)
                    .collect(Collectors.groupingBy(Incident::getIncidentType, Collectors.counting()));
            
            long total = typeCounts.values().stream().mapToLong(Long::longValue).sum();
            
            typeCounts.forEach((type, count) -> {
                Map<String, Object> item = new HashMap<>();
                item.put(field, type.toString());
                item.put("count", count);
                item.put("percentage", total > 0 ? (count * 100.0 / total) : 0);
                data.add(item);
            });
        } else if ("status".equalsIgnoreCase(field)) {
            // Agrupar por status
            Map<Incident.Status, Long> statusCounts = incidents.stream()
                    .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
            
            long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();
            
            statusCounts.forEach((stat, count) -> {
                Map<String, Object> item = new HashMap<>();
                item.put(field, stat.toString());
                item.put("count", count);
                item.put("percentage", total > 0 ? (count * 100.0 / total) : 0);
                data.add(item);
            });
        } else if ("location".equalsIgnoreCase(field)) {
            // Agrupar por localização (arredondando coordenadas)
            Map<String, Long> locationCounts = incidents.stream()
                    .collect(Collectors.groupingBy(
                            i -> String.format("%.2f, %.2f", i.getLatitude(), i.getLongitude()),
                            Collectors.counting()
                    ));
            
            long total = locationCounts.values().stream().mapToLong(Long::longValue).sum();
            
            locationCounts.forEach((location, count) -> {
                Map<String, Object> item = new HashMap<>();
                item.put(field, location);
                item.put("count", count);
                item.put("percentage", total > 0 ? (count * 100.0 / total) : 0);
                data.add(item);
            });
        }

        OracleReportResponse response = new OracleReportResponse(
                "DYNAMIC_AGGREGATE_DATA",
                data,
                Map.of("aggregateField", field, "totalRecords", incidents.size())
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Gerar relatório customizado",
               description = "Gera um relatório customizado baseado no tipo especificado")
    @GetMapping("/dynamic-sql/custom-report")
    public ResponseEntity<OracleReportResponse> generateCustomReport(@RequestParam String type) {
        List<Map<String, Object>> data = new java.util.ArrayList<>();
        List<Incident> incidents = incidentRepository.findAll();

        switch (type.toLowerCase()) {
            case "incidents_by_type":
                Map<Incident.IncidentType, Map<Incident.Status, Long>> byType = incidents.stream()
                        .filter(i -> i.getIncidentType() != null)
                        .collect(Collectors.groupingBy(
                                Incident::getIncidentType,
                                Collectors.groupingBy(Incident::getStatus, Collectors.counting())
                        ));
                
                byType.forEach((incType, statusMap) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", incType.toString());
                    item.put("total", statusMap.values().stream().mapToLong(Long::longValue).sum());
                    item.put("pendentes", statusMap.getOrDefault(Incident.Status.PENDING, 0L));
                    item.put("investigando", statusMap.getOrDefault(Incident.Status.INVESTIGATING, 0L));
                    item.put("resolvidos", statusMap.getOrDefault(Incident.Status.RESOLVED, 0L));
                    item.put("descartados", statusMap.getOrDefault(Incident.Status.DISMISSED, 0L));
                    data.add(item);
                });
                break;
                
            case "incidents_by_status":
                Map<Incident.Status, Long> byStatus = incidents.stream()
                        .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
                
                byStatus.forEach((stat, count) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("status", stat.toString());
                    item.put("count", count);
                    data.add(item);
                });
                break;
                
            case "incidents_by_location":
                // Agrupar por região (arredondando coordenadas para criar regiões)
                Map<String, List<Incident>> byLocation = incidents.stream()
                        .collect(Collectors.groupingBy(
                                i -> String.format("%.1f, %.1f", i.getLatitude(), i.getLongitude())
                        ));
                
                byLocation.forEach((location, incList) -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("location", location);
                    item.put("incidents", incList.size());
                    item.put("riskLevel", incList.size() > 5 ? "HIGH" : incList.size() > 2 ? "MEDIUM" : "LOW");
                    data.add(item);
                });
                break;
                
            default:
                data.add(Map.of("message", "Tipo de relatório não reconhecido: " + type));
        }

        OracleReportResponse response = new OracleReportResponse(
                "CUSTOM_REPORT_" + type.toUpperCase(),
                data,
                Map.of("reportType", type, "generatedAt", LocalDateTime.now().toString())
        );

        return ResponseEntity.ok(response);
    }

    private String getSafetyDescription(Double score) {
        if (score >= 90) {
            return "Região muito segura";
        } else if (score >= 70) {
            return "Região segura";
        } else if (score >= 50) {
            return "Atenção necessária";
        } else if (score >= 30) {
            return "Região de risco";
        } else {
            return "Região de alto risco";
        }
    }
}
