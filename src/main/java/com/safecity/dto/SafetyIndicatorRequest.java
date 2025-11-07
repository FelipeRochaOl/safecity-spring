package com.safecity.dto;

public class SafetyIndicatorRequest {
    private Double latitude;
    private Double longitude;
    private Double radiusKm = 1.0;

    public SafetyIndicatorRequest() {
    }

    public SafetyIndicatorRequest(Double latitude, Double longitude, Double radiusKm) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusKm = radiusKm;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadiusKm() {
        return radiusKm;
    }

    public void setRadiusKm(Double radiusKm) {
        this.radiusKm = radiusKm;
    }
}
