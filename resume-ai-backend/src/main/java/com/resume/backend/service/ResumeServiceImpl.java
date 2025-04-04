package com.resume.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final WebClient groqWebClient;
    private final ObjectMapper objectMapper;

    public ResumeServiceImpl(WebClient groqWebClient, ObjectMapper objectMapper) {
        this.groqWebClient = groqWebClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, Object> generateResumeResponse(String userResumeDescription) throws IOException {
        String promptString = this.loadPromptFromFile("resume_prompt.txt");
        String promptContent = this.putValuesToTemplate(promptString, Map.of(
                "userDescription", userResumeDescription
        ));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "mixtral-8x7b-32768");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", promptContent)
        ));
        requestBody.put("temperature", 0.7);

        String response = groqWebClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<?> choices = (List<?>) responseMap.get("choices");
        Map<String, ?> firstChoice = (Map<String, ?>) choices.get(0);
        Map<String, String> message = (Map<String, String>) firstChoice.get("message");
        String content = message.get("content");

        return parseMultipleResponses(content);
    }

    String loadPromptFromFile(String filename) throws IOException {
        Path path = new ClassPathResource(filename).getFile().toPath();
        return Files.readString(path);
    }

    String putValuesToTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {

            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());

        }
        return template;
    }


    public static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();

        // Extract content inside <think> tags
        int thinkStart = response.indexOf("<think>") + 7;
        int thinkEnd = response.indexOf("</think>");
        if (thinkStart != -1 && thinkEnd != -1) {
            String thinkContent = response.substring(thinkStart, thinkEnd).trim();
            jsonResponse.put("think", thinkContent);
        } else {
            jsonResponse.put("think", null); // Handle missing <think> tags
        }

        // Extract content that is in JSON format
        int jsonStart = response.indexOf("```json") + 7; // Start after ```json
        int jsonEnd = response.lastIndexOf("```");       // End before ```
        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
            try {
                // Convert JSON string to Map using Jackson ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataContent = objectMapper.readValue(jsonContent, Map.class);
                jsonResponse.put("data", dataContent);
            } catch (Exception e) {
                jsonResponse.put("data", null); // Handle invalid JSON
                System.err.println("Invalid JSON format in the response: " + e.getMessage());
            }
        } else {
            jsonResponse.put("data", null); // Handle missing JSON
        }

        return jsonResponse;
    }
}
