package com.safecity.dto;

import com.safecity.model.Incident;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de um novo incidente")
public class IncidentRequest {
    
    @Schema(description = "Título do incidente", example = "Assalto na praça central")
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    private String title;
    
    @Schema(description = "Descrição detalhada do incidente", example = "Ocorreu um assalto durante a tarde na praça central...")
    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String description;
    
    @Schema(description = "Latitude da localização do incidente", example = "-23.5505")
    @NotNull(message = "Latitude é obrigatória")
    private Double latitude;
    
    @Schema(description = "Longitude da localização do incidente", example = "-46.6333")
    @NotNull(message = "Longitude é obrigatória")
    private Double longitude;
    
    @Schema(description = "Endereço do incidente (opcional)", example = "Praça da Sé, São Paulo - SP")
    @Size(max = 500, message = "Endereço deve ter no máximo 500 caracteres")
    private String address;
    
    @Schema(description = "Tipo do incidente", example = "THEFT", allowableValues = {"ASSAULT", "THEFT", "VANDALISM", "DRUG_ACTIVITY", "SUSPICIOUS_ACTIVITY", "OTHER"})
    private Incident.IncidentType incidentType;
    
    // Constructors
    public IncidentRequest() {}
    
    public IncidentRequest(String title, String description, Double latitude, Double longitude) {
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Incident.IncidentType getIncidentType() { return incidentType; }
    public void setIncidentType(Incident.IncidentType incidentType) { this.incidentType = incidentType; }
}

