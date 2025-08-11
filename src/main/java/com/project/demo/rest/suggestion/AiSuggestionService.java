package com.project.demo.rest.suggestion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.demo.logic.entity.farm.Farm;
import com.project.demo.logic.entity.farm.FarmRepository;
import com.project.demo.logic.entity.farm.FarmsTechnicalInformation;
import com.project.demo.logic.entity.farm.FarmsTechnicalInformationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.type.TypeReference;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class AiSuggestionService {

    @Value("${together.api.key}")
    private String togetherApiKey;

    @Value("${together.api.model}")
    private String togetherModel;

    @Value("${together.api.max_tokens}")
    private int togetherMaxTokens;

    private final RestTemplate restTemplate = new RestTemplate();

    private final String TOGETHER_API_URL = "https://api.together.xyz/v1/chat/completions";
    @Autowired
    private FarmRepository farmRepository;



    @Autowired
    private FarmsTechnicalInformationRepository farmsTechnicalInformationRepository;

    public AiSuggestionService(FarmRepository farmRepository,
                               FarmsTechnicalInformationRepository farmsTechnicalInformationRepository) {
        this.farmRepository = farmRepository;
        this.farmsTechnicalInformationRepository = farmsTechnicalInformationRepository;
    }


    /**
     * Genera una sugerencia de IA personalizada para una finca específica
     * basándose en la información general y técnica de la finca junto con el
     * input del usuario.
     *
     * @param userInput Texto con la pregunta o solicitud del usuario.
     * @param farmId ID de la finca para la cual se genera la sugerencia.
     * @return Sugerencia generada por IA en formato String.
     * @throws ResponseStatusException Si la finca no existe, hay error en la API o en la generación.
     */
    public String generateSuggestion(String userInput, Long farmId) {
        try {
            //para obtener data de la finca
            Farm farm = farmRepository.findById(farmId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la finca con ID " + farmId));

            //info tecnica de finca
            Optional<FarmsTechnicalInformation> techInfoOpt = farmsTechnicalInformationRepository.findByFarmId(farmId);

            // 3. Convertir a Map
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> farmMap = mapper.convertValue(farm, typeRef);
            Map<String, Object> techInfoMap = techInfoOpt.map(info -> mapper.convertValue(info, typeRef)).orElse(null);

           // contexto para la ia
            Map<String, Object> farmDataMap = new HashMap<>();
            farmDataMap.put("farm", farmMap);
            farmDataMap.put("technicalInfo", techInfoMap);
            String contextoCompleto = buildContextJson(Map.of("data", farmDataMap));

            //rcortar inteligentemente si excede el limite
            int maxTokensDisponibles = 4000;
            String contextoOptimizado = cutContext(contextoCompleto, maxTokensDisponibles - contarTokens(userInput) - togetherMaxTokens);

            //request
            Map<String, Object> body = new HashMap<>();
            body.put("model", togetherModel);
            body.put("max_tokens", togetherMaxTokens);
            body.put("temperature", 0.7);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "Eres un asistente agrícola experto en dar sugerencias personalizadas para el manejo de fincas. " +
                            "Usa la siguiente información de la finca como contexto para tus respuestas:\n" +
                            contextoOptimizado +
                            "\nResponde SIEMPRE en español, en máximo 4 párrafos completos y asegurándote de terminar las ideas sin cortar frases."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", userInput + "\nRecuerda: responde siempre en español."
            ));
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(togetherApiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    TOGETHER_API_URL,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("error")) {
                Map<String, Object> errorData = (Map<String, Object>) response.getBody().get("error");
                String errorMsg = errorData.getOrDefault("message", "Error desconocido").toString();
                String errorType = errorData.getOrDefault("type", "").toString();

                if ("credit_limit".equalsIgnoreCase(errorType)) {
                    throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, errorMsg);
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMsg);
                }
            }

            if (response.getStatusCode().is2xxSuccessful()) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.containsKey("content")) {
                        return message.get("content").toString().trim();
                    }
                }
            }

            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar una sugerencia");

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Ocurrió un error generando la sugerencia: " + e.getMessage());
        }
    }

    /**
     * Recorta el contexto textual de manera inteligente para no superar
     * un límite máximo de tokens, priorizando secciones más relevantes.
     *
     * @param contexto Texto completo del contexto.
     * @param maxTokens Límite máximo de tokens permitidos.
     * @return Texto recortado que mantiene las secciones más importantes.
     */
    private String cutContext(String contexto, int maxTokens) {
        List<String> secciones = divideIntoSections(contexto);
        List<String> palabrasClave = Arrays.asList("críticos", "ID", "Fecha", "cultivo", "riego", "agua", "fertilizante");

        List<SeccionConScore> lista = new ArrayList<>();
        for (String sec : secciones) {
            int score = calculateRelevanceSection(sec, palabrasClave);
            lista.add(new SeccionConScore(sec, score));
        }

        lista.sort((a, b) -> Integer.compare(b.score, a.score));

        StringBuilder nuevoContexto = new StringBuilder();
        int tokensUsados = 0;
        for (SeccionConScore sc : lista) {
            int tokensSeccion = contarTokens(sc.texto);
            if (tokensUsados + tokensSeccion <= maxTokens) {
                nuevoContexto.append(sc.texto).append("\n");
                tokensUsados += tokensSeccion;
            } else {
                break;
            }
        }

        return nuevoContexto.toString().trim();
    }
    /**
     * Divide el contexto completo en secciones separadas para evaluación
     * de relevancia.
     *
     * @param contexto Texto completo del contexto.
     * @return Lista de secciones extraídas del texto.
     */
    private List<String> divideIntoSections(String contexto) {
        return Arrays.stream(contexto.split("(?m)^===|\\n\\n")) // separa por dobles saltos o separadores
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }


    /**
     * Calcula la relevancia de una sección específica basado en la presencia
     * de palabras clave y la longitud de la sección.
     *
     * @param seccion Texto de la sección.
     * @param palabrasClave Lista de palabras clave para evaluar relevancia.
     * @return Puntaje de relevancia numérico para la sección.
     */
    private int calculateRelevanceSection(String seccion, List<String> palabrasClave) {
        int score = 0;
        for (String palabra : palabrasClave) {
            if (seccion.toLowerCase().contains(palabra.toLowerCase())) {
                score += 3;
            }
        }
        score += Math.min(seccion.length() / 100, 5);
        return score;
    }
    // Clase interna usada para manejar secciones con puntuación
    private static class SeccionConScore {
        String texto;
        int score;
        SeccionConScore(String texto, int score) {
            this.texto = texto;
            this.score = score;
        }
    }

    /**
     * Cuenta una aproximación del número de tokens en un texto dado,
     * usado para gestionar límites en las solicitudes a la IA.
     *
     * @param texto Texto para contar tokens.
     * @return Número aproximado de tokens.
     */
    private int contarTokens(String texto) {
        if (texto == null || texto.isEmpty()) return 0;
        return texto.length() / 4; // aprox
    }

    /**
     * Construye un contexto textual a partir de un JSON que contiene información
     * de la finca y su información técnica para ser usada como contexto en la IA.
     *
     * @param fullResponseJson Mapa con la información completa (finca y técnica).
     * @return Texto formateado que representa el contexto para la IA.
     */
    @SuppressWarnings("unchecked")
    public String buildContextJson(Map<String, Object> fullResponseJson) {
        StringBuilder contexto = new StringBuilder();

        if (fullResponseJson == null) return "";

        // Obtener data
        Map<String, Object> data = (Map<String, Object>) fullResponseJson.get("data");
        if (data == null) return "";

        // inf tecnica
        Map<String, Object> techInfo = (Map<String, Object>) data.get("technicalInfo");
        if (techInfo != null) {
            contexto.append("Información técnica de la finca:\n");
            contexto.append("- pH del suelo: ").append(orEmpty(techInfo.get("soilPh"))).append("\n");
            contexto.append("- Nutrientes del suelo: ").append(orEmpty(techInfo.get("soilNutrients"))).append("\n");
            contexto.append("- Tipo de sistema de riego: ").append(orEmpty(techInfo.get("irrigationSystemType"))).append("\n");
            contexto.append("- Disponibilidad de agua: ").append(orEmpty(techInfo.get("waterAvailable"))).append("\n");
            contexto.append("- Uso del agua: ").append(orEmpty(techInfo.get("waterUsageType"))).append("\n");
            contexto.append("- Uso de fertilizantes y pesticidas: ").append(orEmpty(techInfo.get("fertilizerPesticideUse"))).append("\n\n");
        }

        // info finca
        Map<String, Object> farm = (Map<String, Object>) data.get("farm");
        if (farm != null) {
            contexto.append("Datos generales de la finca:\n");
            contexto.append("- Nombre: ").append(orEmpty(farm.get("farmName"))).append("\n");
            contexto.append("- País: ").append(orEmpty(farm.get("farmCountry"))).append("\n");
            contexto.append("- Provincia/Estado: ").append(orEmpty(farm.get("farmStateProvince"))).append("\n");
            contexto.append("- Otras direcciones: ").append(orEmpty(farm.get("farmOtherDirections"))).append("\n");
            contexto.append("- Ubicación (lat,lng): ").append(orEmpty(farm.get("farmLocation"))).append("\n");
            contexto.append("- Tamaño: ").append(orEmpty(farm.get("farmSize"))).append(" ").append(orEmpty(farm.get("farmMeasureUnit"))).append("\n\n");

            // grupos animales
            List<Map<String, Object>> animalGroups = (List<Map<String, Object>>) farm.get("animalGroups");
            if (animalGroups != null && !animalGroups.isEmpty()) {
                contexto.append("Grupos animales:\n");
                for (Map<String, Object> grupo : animalGroups) {
                    contexto.append("- Grupo: ").append(orEmpty(grupo.get("groupName"))).append("\n");
                    contexto.append("  - Tipo de producción: ").append(orEmpty(grupo.get("productionType"))).append("\n");
                    contexto.append("  - Medida: ").append(orEmpty(grupo.get("measure"))).append("\n");

                    List<Map<String, Object>> animals = (List<Map<String, Object>>) grupo.get("animals");
                    if (animals != null && !animals.isEmpty()) {
                        contexto.append("  - Animales:\n");
                        for (Map<String, Object> animal : animals) {
                            contexto.append("    * Especie: ").append(orEmpty(animal.get("species"))).append(", ");
                            contexto.append("Raza: ").append(orEmpty(animal.get("breed"))).append(", ");
                            contexto.append("Cantidad: ").append(orEmpty(animal.get("count"))).append("\n");
                        }
                    }
                }
                contexto.append("\n");
            }

            // Parcelas (plots)
            List<Map<String, Object>> plots = (List<Map<String, Object>>) farm.get("plots");
            if (plots != null && !plots.isEmpty()) {
                contexto.append("Parcelas de la finca:\n");
                for (Map<String, Object> parcela : plots) {
                    contexto.append("- Parcela: ").append(orEmpty(parcela.get("plotName"))).append("\n");
                    contexto.append("  - Descripción: ").append(orEmpty(parcela.get("plotDescription"))).append("\n");
                    contexto.append("  - Tipo: ").append(orEmpty(parcela.get("plotType"))).append("\n");
                    contexto.append("  - Uso actual: ").append(orEmpty(parcela.get("currentUsage"))).append("\n");

                    List<Map<String, Object>> cropsManagements = (List<Map<String, Object>>) parcela.get("cropsManagements");
                    if (cropsManagements != null && !cropsManagements.isEmpty()) {
                        contexto.append("  - Manejo de cultivos:\n");
                        for (Map<String, Object> manejo : cropsManagements) {
                            contexto.append("    * Acción: ").append(orEmpty(manejo.get("actionName"))).append("\n");
                            contexto.append("      Fecha acción: ").append(orEmpty(manejo.get("actionDate"))).append("\n");
                            contexto.append("      Valor gastado: ").append(orEmpty(manejo.get("valueSpent"))).append("\n");

                            // Info del cultivo
                            Map<String, Object> crop = (Map<String, Object>) manejo.get("crop");
                            if (crop != null) {
                                contexto.append("      Cultivo: ").append(orEmpty(crop.get("cropName"))).append("\n");
                                contexto.append("      Tipo: ").append(orEmpty(crop.get("cropType"))).append("\n");
                                contexto.append("      Variedad: ").append(orEmpty(crop.get("cropVariety"))).append("\n");
                            }
                        }
                    }
                }
                contexto.append("\n");
            }
        }

        return contexto.toString();
    }

    /**
     * Método auxiliar para devolver un String no nulo, reemplazando null con
     * texto por defecto "Sin Datos".
     *
     * @param obj Objeto a convertir a String.
     * @return String del objeto o "Sin Datos" si es null.
     */
    private String orEmpty(Object obj) {
        return (obj == null) ? "Sin Datos" : obj.toString();
    }


}