package ro.utcn.danlupu.service.nlp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.model.TextInterpreterRequest;
import ro.utcn.danlupu.model.TextInterpreterResponse;
import ro.utcn.danlupu.service.nlp.NlpService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Service("GPT")
public class GptNlpService implements NlpService {

    @Value("${gpt-translator.translator.url}")
    private String gptTranslatorUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public TextInterpreterResponse interpretText(TextInterpreterRequest textInterpreterRequest) {
        try {
            String body = "{\"text\":\"" + textInterpreterRequest.text() + "\"}";
            HttpRequest nlpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(gptTranslatorUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> nlpResponse = httpClient.send(nlpRequest, HttpResponse.BodyHandlers.ofString());
            String kifFormula = extractQueryFromResponse(nlpResponse.body());
            return new TextInterpreterResponse(kifFormula);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String extractQueryFromResponse(String nlpResponse) {
        try {
            JsonNode responseJson = OBJECT_MAPPER.readTree(nlpResponse);
            return responseJson.get("kif_formula").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
