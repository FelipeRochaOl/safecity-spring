package com.safecity.mapper;

import com.safecity.dto.IncidentResponse;
import com.safecity.model.Incident;

public final class IncidentMapper {
    private IncidentMapper() {}

    public static IncidentResponse toResponse(Incident i) {
        return new IncidentResponse(
                i.getId() != null ? i.getId().toString() : null,
                i.getTitle(),
                i.getDescription(),
                i.getLatitude(),
                i.getLongitude(),
                i.getAddress(),
                i.getIncidentType() != null ? i.getIncidentType().name() : null,
                i.getStatus() != null ? i.getStatus().name() : null,
                (i.getUser() != null && i.getUser().getId() != null) ? i.getUser().getId().toString() : null,
                i.getUser() != null ? i.getUser().getName() : null,
                i.getUser() != null ? i.getUser().getEmail(): null,
                i.getUser() != null ? i.getUser().getPhone(): null,
                i.getCreatedAt(),
                i.getUpdatedAt()
        );
    }
}
