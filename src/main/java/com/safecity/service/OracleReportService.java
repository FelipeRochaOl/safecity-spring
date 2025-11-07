package com.safecity.service;

import com.safecity.model.Incident;
import com.safecity.repository.IncidentRepository;
import com.safecity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OracleReportService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Calcula o indicador de segurança para uma região específica
     * Simula a função Oracle: calculate_safety_indicator
     */
    public Double calculateSafetyIndicator(Double latitude, Double longitude, Double radiusKm) {
        // Converter raio em graus aproximadamente (1 grau ≈ 111 km)
        Double radiusDegrees = radiusKm / 111.0;
        Double radiusSquared = radiusDegrees * radiusDegrees;

        List<Incident> incidents = incidentRepository.findIncidentsNearLocation(latitude, longitude, radiusSquared);

        if (incidents.isEmpty()) {
            return 100.0;
        }

        // Calcular severidade total
        Double totalSeverity = incidents.stream()
                .mapToDouble(this::getIncidentSeverity)
                .sum();

        // Fórmula: 100 - ((incidentes × severidade_total) / (raio_km × 5))
        Double score = 100.0 - ((incidents.size() * totalSeverity) / (radiusKm * 5));

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Retorna a severidade de um incidente baseado no tipo
     */
    private Double getIncidentSeverity(Incident incident) {
        if (incident.getIncidentType() == null) {
            return 2.0;
        }
        return switch (incident.getIncidentType()) {
            case ASSAULT -> 10.0;
            case DRUG_ACTIVITY -> 8.0;
            case THEFT -> 7.0;
            case VANDALISM -> 5.0;
            case SUSPICIOUS_ACTIVITY -> 3.0;
            default -> 2.0;
        };
    }

    /**
     * Gera resumo de incidentes de um usuário
     * Simula a função Oracle: get_user_incidents_summary
     */
    public String getUserIncidentsSummary(Long userId) {
        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return "Usuário não encontrado";
        }

        var user = userOpt.get();
        var incidents = incidentRepository.findByUserOrderByCreatedAtDesc(user);

        if (incidents.isEmpty()) {
            return "=== RESUMO DE INCIDENTES ===\n\nNenhum incidente reportado ainda.";
        }

        // Contagem por status
        Map<Incident.Status, Long> statusCounts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));

        // Tipo mais comum
        Map.Entry<Incident.IncidentType, Long> mostCommon = incidents.stream()
                .filter(i -> i.getIncidentType() != null)
                .collect(Collectors.groupingBy(Incident::getIncidentType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        // Último incidente
        Incident lastIncident = incidents.get(0);

        StringBuilder summary = new StringBuilder();
        summary.append("=== RESUMO DE INCIDENTES ===\n\n");
        summary.append("Total de incidentes: ").append(incidents.size()).append("\n");
        summary.append("• Pendentes: ").append(statusCounts.getOrDefault(Incident.Status.PENDING, 0L)).append("\n");
        summary.append("• Em investigação: ").append(statusCounts.getOrDefault(Incident.Status.INVESTIGATING, 0L)).append("\n");
        summary.append("• Resolvidos: ").append(statusCounts.getOrDefault(Incident.Status.RESOLVED, 0L)).append("\n");
        summary.append("• Descartados: ").append(statusCounts.getOrDefault(Incident.Status.DISMISSED, 0L)).append("\n\n");

        if (mostCommon != null) {
            summary.append("Tipo mais reportado: ").append(mostCommon.getKey()).append("\n");
        }
        summary.append("Último incidente: ").append(lastIncident.getCreatedAt()).append("\n");

        summary.append("\n=== ÚLTIMOS 3 INCIDENTES ===\n");
        incidents.stream().limit(3).forEach(incident -> {
            summary.append("• ").append(incident.getIncidentType())
                    .append(" (").append(incident.getStatus()).append(") - ")
                    .append(incident.getCreatedAt().toLocalDate()).append("\n");
        });

        return summary.toString();
    }

    /**
     * Gera alertas automáticos
     * Simula a procedure Oracle: generate_automatic_alerts
     */
    public Map<String, Object> generateAutomaticAlerts() {
        Map<String, Object> result = new HashMap<>();
        int alertsCreated = 0;

        // 1. Alertas de região de alto risco (3+ incidentes nas últimas 24h)
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        List<Incident> recentIncidents = incidentRepository.findAll().stream()
                .filter(i -> i.getCreatedAt().isAfter(last24Hours))
                .filter(i -> i.getStatus() == Incident.Status.PENDING || i.getStatus() == Incident.Status.INVESTIGATING)
                .collect(Collectors.toList());

        Map<String, Long> locationCounts = recentIncidents.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getLatitude() + "," + i.getLongitude(),
                        Collectors.counting()
                ));

        List<String> highRiskAreas = locationCounts.entrySet().stream()
                .filter(e -> e.getValue() >= 3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        alertsCreated += highRiskAreas.size();

        result.put("highRiskAreas", highRiskAreas);
        result.put("totalAlerts", alertsCreated);
        result.put("message", "Geração de alertas concluída. Total de alertas criados: " + alertsCreated);

        return result;
    }

    /**
     * Gera relatório de atividade do usuário
     * Simula a procedure Oracle: generate_user_activity_report
     */
    public Map<String, Object> generateUserActivityReport(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();

        var userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            report.put("error", "Usuário não encontrado");
            return report;
        }

        var user = userOpt.get();
        var allIncidents = incidentRepository.findByUserOrderByCreatedAtDesc(user);

        // Filtrar por período
        List<Incident> incidents = allIncidents.stream()
                .filter(i -> i.getCreatedAt().isAfter(startDate) && i.getCreatedAt().isBefore(endDate))
                .collect(Collectors.toList());

        // Estatísticas gerais
        report.put("userId", userId);
        report.put("userName", user.getName());
        report.put("period", Map.of(
                "start", startDate,
                "end", endDate
        ));
        report.put("totalIncidents", incidents.size());

        // Por status
        Map<Incident.Status, Long> statusCounts = incidents.stream()
                .collect(Collectors.groupingBy(Incident::getStatus, Collectors.counting()));
        report.put("byStatus", statusCounts);

        // Por tipo
        Map<Incident.IncidentType, Long> typeCounts = incidents.stream()
                .filter(i -> i.getIncidentType() != null)
                .collect(Collectors.groupingBy(Incident::getIncidentType, Collectors.counting()));
        report.put("byType", typeCounts);

        // Última atividade
        if (!incidents.isEmpty()) {
            report.put("lastActivity", incidents.get(0).getCreatedAt());
        }

        return report;
    }
}
