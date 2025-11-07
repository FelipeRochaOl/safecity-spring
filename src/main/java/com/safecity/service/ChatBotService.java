package com.safecity.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safecity.dto.ChatMessage;
import com.safecity.dto.ChatRequest;
import com.safecity.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatBotService {

    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openaiApiUrl;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public ChatBotService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Processa a mensagem do usuário e retorna resposta do chatbot
     */
    public ChatResponse processMessage(ChatRequest request) {
        try {
            // Obter contexto do banco de dados
            String dbContext = getDatabaseContext();

            // Construir prompt para a IA
            String systemPrompt = buildSystemPrompt(dbContext);

            // Chamar OpenAI API
            String aiResponse = callOpenAI(systemPrompt, request.getMessage(), request.getConversationHistory());

            // Processar resposta e executar SQL se necessário
            return processAIResponse(aiResponse);

        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setMessage("Desculpe, ocorreu um erro ao processar sua solicitação.");
            errorResponse.setError(e.getMessage());
            errorResponse.setTimestamp(System.currentTimeMillis());
            return errorResponse;
        }
    }

    /**
     * Constrói o prompt do sistema com contexto do banco de dados
     */
    private String buildSystemPrompt(String dbContext) {
        return """
                Você é um assistente especializado em segurança urbana e análise de dados do sistema SafeCity.
                
                Você tem acesso a um banco de dados Oracle com as seguintes tabelas:
                
                """ + dbContext + """
                
                Seu objetivo é ajudar os usuários a:
                1. Consultar informações sobre incidentes de segurança
                2. Gerar relatórios e estatísticas
                3. Identificar áreas de risco
                4. Analisar tendências de segurança
                5. Obter insights sobre a segurança urbana
                
                Quando o usuário fizer uma pergunta sobre os dados:
                - Analise a pergunta cuidadosamente
                - Se necessário, sugira uma consulta SQL
                - Explique os resultados de forma clara e objetiva
                - Forneça insights e recomendações quando apropriado
                
                IMPORTANTE - Sintaxe SQL Oracle:
                - NÃO use LIMIT. Use FETCH FIRST n ROWS ONLY
                - Exemplo: SELECT * FROM incidents ORDER BY created_at DESC FETCH FIRST 10 ROWS ONLY
                - Para paginação use: OFFSET n ROWS FETCH NEXT m ROWS ONLY
                - Use TO_DATE para conversão de datas
                - Strings devem usar aspas simples, não duplas
                
                Sempre responda em português do Brasil de forma profissional e útil.
                """;
    }

    /**
     * Obtém contexto do banco de dados (schema, estatísticas básicas)
     */
    private String getDatabaseContext() {
        StringBuilder context = new StringBuilder();

        try {
            // Informações sobre tabela INCIDENTS
            context.append("TABELA: incidents\n");
            context.append("Descrição: Armazena incidentes de segurança reportados\n");
            context.append("Colunas:\n");
            context.append("  - id: Identificador único\n");
            context.append("  - title: Título do incidente\n");
            context.append("  - description: Descrição detalhada\n");
            context.append("  - incident_type: Tipo (ASSAULT, THEFT, VANDALISM, DRUG_ACTIVITY, SUSPICIOUS_ACTIVITY, OTHER)\n");
            context.append("  - status: Status (PENDING, INVESTIGATING, RESOLVED, CLOSED)\n");
            context.append("  - latitude, longitude: Localização geográfica\n");
            context.append("  - address: Endereço\n");
            context.append("  - user_id: ID do usuário que reportou\n");
            context.append("  - created_at, updated_at: Timestamps\n");

            // Estatísticas básicas
            Long totalIncidents = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM incidents", Long.class);
            context.append("\nEstatísticas: Total de incidentes = ").append(totalIncidents).append("\n");

            // Informações sobre tabela USERS
            context.append("\nTABELA: users\n");
            context.append("Descrição: Usuários do sistema\n");
            context.append("Colunas: id, name, email, role, enabled, created_at\n");

            // Informações sobre tabela NOTIFICATIONS
            context.append("\nTABELA: notifications\n");
            context.append("Descrição: Notificações enviadas aos usuários\n");
            context.append("Colunas: id, title, message, type, user_id, is_read, created_at\n");

            // Informações sobre Functions e Procedures Oracle
            context.append("\nFUNÇÕES ORACLE DISPONÍVEIS:\n");
            context.append("  - calculate_safety_indicator(lat, long, radius): Calcula indicador de segurança\n");
            context.append("  - get_incident_count_by_type(lat, long, radius, type): Conta incidentes por tipo\n");
            context.append("  - is_high_risk_area(lat, long, radius): Verifica se é área de risco\n");

        } catch (Exception e) {
            context.append("Erro ao obter contexto do banco de dados: ").append(e.getMessage());
        }

        return context.toString();
    }

    /**
     * Chama a API do OpenAI
     */
    private String callOpenAI(String systemPrompt, String userMessage, List<ChatMessage> history) {
        try {
            // Verificar se a chave da API está configurada
            if (openaiApiKey == null || openaiApiKey.isEmpty()) {
                return "Desculpe, o serviço de IA não está configurado. Por favor, configure a chave da API OpenAI.";
            }

            // Construir mensagens
            List<Map<String, String>> messages = new ArrayList<>();
            
            // Adicionar prompt do sistema
            messages.add(Map.of("role", "system", "content", systemPrompt));

            // Adicionar histórico de conversa
            if (history != null && !history.isEmpty()) {
                for (ChatMessage msg : history) {
                    messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
                }
            }

            // Adicionar mensagem atual do usuário
            messages.add(Map.of("role", "user", "content", userMessage));

            // Construir request body
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messages,
                    "max_tokens", 1000,
                    "temperature", 0.7
            );

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            // Fazer requisição
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(openaiApiUrl, entity, String.class);

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            return "Erro ao se comunicar com a IA: " + e.getMessage();
        }
    }

    /**
     * Processa a resposta da IA e executa SQL se necessário
     */
    private ChatResponse processAIResponse(String aiResponse) {
        ChatResponse response = new ChatResponse();
        response.setMessage(aiResponse);
        response.setTimestamp(System.currentTimeMillis());

        // Verificar se a resposta contém uma consulta SQL
        if (aiResponse.contains("SELECT") || aiResponse.contains("select")) {
            try {
                // Extrair SQL da resposta
                String sqlQuery = extractSQLQuery(aiResponse);
                if (sqlQuery != null && !sqlQuery.isEmpty()) {
                    // Converter SQL para sintaxe Oracle
                    sqlQuery = convertToOracleSQL(sqlQuery);
                    response.setSqlQuery(sqlQuery);
                    
                    // Executar query (apenas SELECTs por segurança)
                    if (sqlQuery.trim().toUpperCase().startsWith("SELECT")) {
                        try {
                            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
                            response.setData(results);
                        } catch (Exception sqlException) {
                            // Log detalhado do erro SQL
                            String errorMsg = "Erro ao executar consulta SQL: " + sqlException.getMessage();
                            System.err.println(errorMsg);
                            System.err.println("SQL Query: " + sqlQuery);
                            response.setError(errorMsg);
                        }
                    }
                }
            } catch (Exception e) {
                response.setError("Erro ao processar consulta SQL: " + e.getMessage());
            }
        }

        return response;
    }

    /**
     * Converte SQL para sintaxe Oracle
     */
    private String convertToOracleSQL(String sql) {
        if (sql == null || sql.isEmpty()) {
            return sql;
        }

        // Converter LIMIT para FETCH FIRST
        // Padrão: LIMIT n
        String converted = sql.replaceAll(
            "(?i)LIMIT\\s+(\\d+)\\s*$",
            "FETCH FIRST $1 ROWS ONLY"
        );

        // Converter LIMIT com OFFSET
        // Padrão: LIMIT n OFFSET m
        converted = converted.replaceAll(
            "(?i)LIMIT\\s+(\\d+)\\s+OFFSET\\s+(\\d+)",
            "OFFSET $2 ROWS FETCH NEXT $1 ROWS ONLY"
        );

        // Converter OFFSET antes de LIMIT (ordem invertida)
        // Padrão: OFFSET m LIMIT n
        converted = converted.replaceAll(
            "(?i)OFFSET\\s+(\\d+)\\s+LIMIT\\s+(\\d+)",
            "OFFSET $1 ROWS FETCH NEXT $2 ROWS ONLY"
        );

        return converted;
    }

    /**
     * Extrai consulta SQL da resposta da IA
     */
    private String extractSQLQuery(String text) {
        // Procurar por blocos de código SQL
        String[] patterns = {"```sql", "```SQL", "```"};
        
        for (String pattern : patterns) {
            int start = text.indexOf(pattern);
            if (start != -1) {
                start += pattern.length();
                int end = text.indexOf("```", start);
                if (end != -1) {
                    String query = text.substring(start, end).trim();
                    // Remover ponto-e-vírgula final (Oracle JDBC não aceita)
                    if (query.endsWith(";")) {
                        query = query.substring(0, query.length() - 1).trim();
                    }
                    return query;
                }
            }
        }
        
        // Se não encontrou em blocos de código, procurar SELECT direto
        int selectIndex = text.toUpperCase().indexOf("SELECT");
        if (selectIndex != -1) {
            int endIndex = text.indexOf(";", selectIndex);
            if (endIndex == -1) {
                endIndex = text.length();
            }
            String query = text.substring(selectIndex, endIndex).trim();
            // Remover ponto-e-vírgula final (Oracle JDBC não aceita)
            if (query.endsWith(";")) {
                query = query.substring(0, query.length() - 1).trim();
            }
            return query;
        }
        
        return null;
    }

    /**
     * Obtém estatísticas rápidas para o chatbot
     */
    public Map<String, Object> getQuickStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalIncidents", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM incidents", Long.class));
            
            stats.put("pendingIncidents", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM incidents WHERE status = 'PENDING'", Long.class));
            
            stats.put("totalUsers", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM users", Long.class));
            
            stats.put("incidentsByType", jdbcTemplate.queryForList(
                    "SELECT incident_type, COUNT(*) as count FROM incidents GROUP BY incident_type"));

        } catch (Exception e) {
            stats.put("error", e.getMessage());
        }

        return stats;
    }
}
