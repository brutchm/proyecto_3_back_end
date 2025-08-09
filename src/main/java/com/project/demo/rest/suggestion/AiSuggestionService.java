package com.project.demo.rest.suggestion;

import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class AiSuggestionService {

    @Value("${together.api.key}")
    private String togetherApiKey;

    @Value("${together.api.model}")
    private String togetherModel;

    @Value("${app.base.url}") //http://localhost:8080
    private String appBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String TOGETHER_API_URL = "https://api.together.xyz/v1/chat/completions";

    private FarmRepository farmRepository;




    public String generateSuggestion(String userInput, Long farmId) {
        // 1. Obtener contexto de la finca desde repositorio (sin llamada HTTP)
        String farmContext = getFarmContext(farmId);

        // 2. Armar prompt final
        String systemPrompt = "Eres un asistente agrícola experto en dar sugerencias personalizadas. " +
                "Usa el contexto de la finca que te proporciono para adaptar tu respuesta.\n\n" +
                "=== CONTEXTO DE LA FINCA ===\n" + farmContext + "\n===========================\n";

        // 3. Armar el cuerpo del request
        Map<String, Object> body = new HashMap<>();
        body.put("model", togetherModel);
        body.put("max_tokens", 300);
        body.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userInput));
        body.put("messages", messages);

        // 4. Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(togetherApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 5. Llamar a Together AI
        ResponseEntity<Map> response = restTemplate.exchange(TOGETHER_API_URL, HttpMethod.POST, entity, Map.class);

        // 6. Parsear resultado
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return message.get("content").toString().trim();
            }
        }

        return "No se pudo generar una sugerencia. Intenta nuevamente.";
    }







    //Este me funciona
    public String generateSuggestion(String userInput) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println("API KEY: " + togetherApiKey);
        System.out.println("Model: " + togetherModel);

        // Modelo recomendado
        // String model = "mistralai/Mixtral-8x7B-Instruct-v0.1";

        // Armar el cuerpo del request
        Map<String, Object> body = new HashMap<>();
        body.put("model", togetherModel);
        body.put("max_tokens", 300);
        body.put("temperature", 0.7);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", "Eres un asistente agrícola experto en dar sugerencias personalizadas para el manejo de fincas."));
        messages.add(Map.of("role", "user", "content", userInput));
        body.put("messages", messages);

        // Armar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(togetherApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // Enviar request
        ResponseEntity<Map> response = restTemplate.exchange(TOGETHER_API_URL, HttpMethod.POST, entity, Map.class);

        // Parsear resultado
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return message.get("content").toString().trim();
            }
        }

        return "No se pudo generar una sugerencia. Intenta nuevamente.";
    }


    /**
     *endpoint /farms/{id} para obtener el contexto.
     */

    private String getFarmContext(Long farmId) {
        String url = appBaseUrl + "/farms/" + farmId;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("data")) {
            return "No se encontró información de la finca.";
        }

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        Map<String, Object> farm = (Map<String, Object>) data.get("farm");

        StringBuilder sb = new StringBuilder();
        sb.append("Nombre: ").append(farm.get("farmName")).append("\n");
        sb.append("Ubicación: ").append(farm.get("farmCountry")).append(", ")
                .append(farm.get("farmStateProvince")).append("\n");
        sb.append("Tamaño: ").append(farm.get("farmSize")).append(" ").append(farm.get("farmMeasureUnit")).append("\n");

        // Animales
        List<Map<String, Object>> animalGroups = (List<Map<String, Object>>) farm.get("animalGroups");
        sb.append("\nAnimales:\n");
        if (animalGroups != null) {
            for (Map<String, Object> group : animalGroups) {
                sb.append("- Grupo: ").append(group.get("groupName"))
                        .append(" (Producción: ").append(group.get("productionType")).append(")\n");
                List<Map<String, Object>> animals = (List<Map<String, Object>>) group.get("animals");
                if (animals != null) {
                    for (Map<String, Object> animal : animals) {
                        sb.append("  * ").append(animal.get("species"))
                                .append(" - cantidad: ").append(animal.get("count")).append("\n");
                    }
                }
            }
        }

        // Info técnica
        Map<String, Object> technicalInfo = (Map<String, Object>) data.get("technicalInfo");
        sb.append("\nInformación técnica:\n");
        sb.append("- pH del suelo: ").append(technicalInfo.get("soilPh")).append("\n");
        sb.append("- Nutrientes del suelo: ").append(technicalInfo.get("soilNutrients")).append("\n");
        sb.append("- Sistema de riego: ").append(technicalInfo.get("irrigationSystemType")).append("\n");

        return sb.toString();
    }

}