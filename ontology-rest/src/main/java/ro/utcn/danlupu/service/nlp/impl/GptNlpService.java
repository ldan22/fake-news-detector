package ro.utcn.danlupu.service.nlp.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.model.TextInterpreterRequest;
import ro.utcn.danlupu.model.TextInterpreterResponse;
import ro.utcn.danlupu.service.nlp.NlpService;
import ro.utcn.danlupu.service.nlp.SumoMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;


@Service("GPT")
@Slf4j
@RequiredArgsConstructor
public class GptNlpService implements NlpService {

    @Value("${gpt-translator.translator.url}")
    private String gptTranslatorUrl;

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final SumoMapper sumoMapper;

    @Override
    public TextInterpreterResponse interpretText(TextInterpreterRequest textInterpreterRequest) {
        try {
            log.info("Interpret text: {}", textInterpreterRequest.text());
            String text = mapToSumo(textInterpreterRequest.text());
            log.info("Translate mapped text: {}", text);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("text", text);
            log.info("Call gpt-translator. Body : {}", jsonObject.toJSONString());
            String kifFormula = sendRequest(jsonObject.toJSONString());
            log.info("Kif formula: {}", kifFormula);
            return new TextInterpreterResponse(kifFormula);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private String sendRequest(String body) throws IOException {
        URL url = new URL(gptTranslatorUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.writeBytes(body);
            outputStream.flush();
        }
        int responseCode = connection.getResponseCode();
        log.info("Response Code: " + responseCode);
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            log.info("Response Body: " + response);
        }
        connection.disconnect();
        return extractQueryFromResponse(response.toString());

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

    private String mapToSumo(String text) {
        try {
            String mappedText = sumoMapper.mapToSumoTerms(text);
            log.info("Mapped text: {}", mappedText);
            return mappedText;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return text;
        }
    }
}
