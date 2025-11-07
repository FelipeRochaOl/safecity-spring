package com.safecity.controller;

import com.safecity.dto.ChatRequest;
import com.safecity.dto.ChatResponse;
import com.safecity.service.ChatBotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/chatbot")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@Tag(name = "ChatBot", description = "APIs para chatbot com IA Generativa")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatBotController {

    @Autowired
    private ChatBotService chatBotService;

    @Operation(summary = "Enviar mensagem para o chatbot",
            description = "Processa mensagem do usuário usando IA Generativa e retorna resposta com dados do banco")
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = chatBotService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obter estatísticas rápidas",
            description = "Retorna estatísticas básicas do sistema para o chatbot")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = chatBotService.getQuickStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Health check do chatbot",
            description = "Verifica se o serviço de chatbot está funcionando")
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "ChatBot with AI",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
