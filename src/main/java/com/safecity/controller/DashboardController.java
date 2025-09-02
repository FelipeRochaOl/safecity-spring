package com.safecity.controller;

import com.safecity.dto.IncidentResponse;
import com.safecity.mapper.IncidentMapper;
import com.safecity.model.Incident;
import com.safecity.model.User;
import com.safecity.repository.IncidentRepository;
import com.safecity.repository.NotificationRepository;
import com.safecity.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {
    
    @Autowired
    IncidentRepository incidentRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    NotificationRepository notificationRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total counts
        long totalUsers = userRepository.count();
        long totalIncidents = incidentRepository.count();
        long totalNotifications = notificationRepository.count();
        
        // Incident status counts
        long pendingIncidents = incidentRepository.countByStatus(Incident.Status.PENDING);
        long investigatingIncidents = incidentRepository.countByStatus(Incident.Status.INVESTIGATING);
        long resolvedIncidents = incidentRepository.countByStatus(Incident.Status.RESOLVED);
        long dismissedIncidents = incidentRepository.countByStatus(Incident.Status.DISMISSED);
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalIncidents", totalIncidents);
        stats.put("totalNotifications", totalNotifications);
        stats.put("pendingIncidents", pendingIncidents);
        stats.put("investigatingIncidents", investigatingIncidents);
        stats.put("resolvedIncidents", resolvedIncidents);
        stats.put("dismissedIncidents", dismissedIncidents);
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/incidents/recent")
    public ResponseEntity<List<IncidentResponse>> getRecentIncidents(@RequestParam(defaultValue = "10") int limit) {
        List<Incident> incidents = incidentRepository.findAll()
                .stream()
                .sorted((i1, i2) -> i2.getCreatedAt().compareTo(i1.getCreatedAt()))
                .limit(limit)
                .toList();
        List<IncidentResponse> response = incidents.stream().map(IncidentMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/incidents/by-status")
    public ResponseEntity<List<IncidentResponse>> getIncidentsByStatus(@RequestParam Incident.Status status) {
        List<Incident> incidents = incidentRepository.findByStatusOrderByCreatedAtDesc(status);
        List<IncidentResponse> response = incidents.stream().map(IncidentMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
}

