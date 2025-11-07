package com.safecity.dto;

import java.util.Map;

public class OracleReportResponse {
    private String reportType;
    private Object data;
    private Map<String, Object> metadata;

    public OracleReportResponse() {
    }

    public OracleReportResponse(String reportType, Object data, Map<String, Object> metadata) {
        this.reportType = reportType;
        this.data = data;
        this.metadata = metadata;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
