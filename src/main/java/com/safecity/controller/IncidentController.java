package com.safecity.controller;

import com.safecity.dto.IncidentRequest;
import com.safecity.dto.IncidentResponse;
import com.safecity.mapper.IncidentMapper;
import com.safecity.model.Incident;
import com.safecity.model.User;
import com.safecity.repository.IncidentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    
    @Autowired
    IncidentRepository incidentRepository;
    
    @PostMapping
    public ResponseEntity<?> createIncident(@Valid @RequestBody IncidentRequest incidentRequest, 
                                          Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Incident incident = new Incident(
            incidentRequest.getTitle(),
            incidentRequest.getDescription(),
            incidentRequest.getLatitude(),
            incidentRequest.getLongitude(),
            user
        );
        
        incident.setAddress(incidentRequest.getAddress());
        incident.setIncidentType(incidentRequest.getIncidentType());
        
        Incident savedIncident = incidentRepository.save(incident);
        
        return ResponseEntity.ok(savedIncident);
    }
    
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getAllIncidents() {
        List<Incident> incidents = incidentRepository.findAll();
        List<IncidentResponse> response = incidents.stream().map(IncidentMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<IncidentResponse>> getMyIncidents(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Incident> incidents = incidentRepository.findByUserOrderByCreatedAtDesc(user);
        List<IncidentResponse> response = incidents.stream().map(IncidentMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(@PathVariable UUID id) {
        Optional<Incident> incident = incidentRepository.findById(id);
        
        if (incident.isPresent()) {
            IncidentResponse incidentResponse = IncidentMapper.toResponse(incident.get());
            return ResponseEntity.ok(incidentResponse);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Incidente não encontrado");
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/near")
    public ResponseEntity<List<IncidentResponse>> getIncidentsNearLocation(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "0.01") Double radius) {
        
        // Convert radius to squared distance for simple distance calculation
        Double radiusSquared = radius * radius;
        
        List<Incident> incidents = incidentRepository.findIncidentsNearLocation(
            latitude, longitude, radiusSquared);
        List<IncidentResponse> response = incidents.stream().map(IncidentMapper::toResponse).toList();
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateIncidentStatus(@PathVariable UUID id,
                                                @RequestParam Incident.Status status,
                                                Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Optional<Incident> incidentOpt = incidentRepository.findById(id);
        
        if (incidentOpt.isPresent()) {
            Incident incident = incidentOpt.get();
            
            // Only admin or incident owner can update status
            if (user.getRole() == User.Role.ADMIN || incident.getUser().getId().equals(user.getId())) {
                incident.setStatus(status);
                incidentRepository.save(incident);
                IncidentResponse incidentResponse = IncidentMapper.toResponse(incident);
                return ResponseEntity.ok(incidentResponse);
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Não autorizado para atualizar este incidente");
                return ResponseEntity.status(403).body(response);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

