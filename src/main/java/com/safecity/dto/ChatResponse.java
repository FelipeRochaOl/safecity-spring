package com.safecity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private String sqlQuery;
    private Object data;
    private Long timestamp;
    private String error;
    
    public ChatResponse(String message, Object data) {
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatResponse(String message) {
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
    
    // Explicit setters for compatibility
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setError(String error) {
        this.error = error;
    }
}
