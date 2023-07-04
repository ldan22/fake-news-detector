package ro.utcn.danlupu.fakenewsdetectorapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorRequest;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorResponse;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.KbQueryResponse;
import ro.utcn.danlupu.fakenewsdetectorapi.model.TextState;
import ro.utcn.danlupu.fakenewsdetectorapi.service.FakeNewsDetectorService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class FakeNewsDetectorServiceImpl implements FakeNewsDetectorService {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String NLP_INTERPRETER_ENDPOINT = "http://ontology-rest:8080/api/v1/nlp/interpret";
    private static final String KB_QUERY_ENDPOINT = "http://ontology-rest:8080/api/v1/kb/query";
    HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public FakeNewsDetectorResponse check(FakeNewsDetectorRequest request) {
        String query = convertText(request);
        if (query == null) {
            return null;
        }

        CompletableFuture<HttpResponse<String>> queryResponseFuture = sendQueryRequest(httpClient, query);
        CompletableFuture.allOf(queryResponseFuture).join();

        KbQueryResponse queryResponse = extractResponseFromQueryResponse(queryResponseFuture.join().body());
        TextState queryTextState = getTextState(queryResponse);


        return FakeNewsDetectorResponse.builder()
                .state(queryTextState)
                .proof(queryResponse.proof())
                .build();
    }


    private CompletableFuture<HttpResponse<String>> sendQueryRequest(HttpClient httpClient, String query) {
        String queryRequestBody = "{\"query\":\"" + query.replace("\n", "").replace("\"", "\\\"") + "\"}";
        log.info("Query: {}", queryRequestBody);
        HttpRequest queryRequest = HttpRequest.newBuilder()
                .uri(URI.create(KB_QUERY_ENDPOINT))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(queryRequestBody))
                .build();

        return httpClient.sendAsync(queryRequest, HttpResponse.BodyHandlers.ofString());
    }

    private String convertText(FakeNewsDetectorRequest request) {
        try {
            HttpRequest nlpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(NLP_INTERPRETER_ENDPOINT))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"text\":\"" + request.text() + "\", \"nlpProcessor\":\"" + request.nlpProcessor() + "\"}"))
                    .build();
            HttpResponse<String> nlpResponse = httpClient.send(nlpRequest, HttpResponse.BodyHandlers.ofString());
            log.info("NLP response: {}", nlpResponse);
            return extractQueryFromNlpResponse(nlpResponse.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractQueryFromNlpResponse(String nlpResponse) {
        try {
            JsonNode responseJson = OBJECT_MAPPER.readTree(nlpResponse);
            return responseJson.get("query").asText();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private KbQueryResponse extractResponseFromQueryResponse(String queryResponse) {
        try {
            log.info("Query response body: {}", queryResponse);
            return OBJECT_MAPPER.readValue(queryResponse, KbQueryResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private TextState getTextState(KbQueryResponse response) {
        int proofSize = response.proof().size();
        if (proofSize > 0 && !response.noConjecture()) {
            return TextState.TRUE;
        }
        return TextState.FAKE;
    }
}
