package com.safecity.dto;

import java.time.LocalDateTime;

public record IncidentResponse(
        String id,
        String title,
        String description,
        Double latitude,
        Double longitude,
        String address,
        String incidentType,
        String status,
        String userId,
        String userName,
        String userEmail,
        String userPhone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}