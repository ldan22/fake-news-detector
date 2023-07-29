package ro.utcn.danlupu.fakenewsdetectorapi.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.w3c.dom.Text;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class FakeNewsDetectorServiceImpl implements FakeNewsDetectorService {

    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String NLP_INTERPRETER_ENDPOINT = "http://ontology-rest:8080/api/v1/nlp/interpret";
    private static final String KB_QUERY_ENDPOINT = "http://ontology-rest:8080/api/v1/kb/query";
    private static final String KB_TERM_ENDPOINT = "http://ontology-rest:8080/api/v1/kb/term";
    HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public FakeNewsDetectorResponse check(FakeNewsDetectorRequest request) {
        return doubleCheck(request);
    }

    @Override
    public Map<String, Object> getTermInformation(String term) {
        try {
            if (!isOneWord(term)) {
                throw new RuntimeException("Term should be one word");
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(KB_TERM_ENDPOINT + "?term=" + term))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonBody = response.body();
            return OBJECT_MAPPER.readValue(jsonBody, new TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private FakeNewsDetectorResponse singleCheck(FakeNewsDetectorRequest request) {
        StopWatch watch = new StopWatch();
        watch.start();
        String query = convertText(request);
        if (query == null) {
            return null;
        }
        CompletableFuture<HttpResponse<String>> queryResponseFuture = sendQueryRequest(httpClient, query);
        CompletableFuture.allOf(queryResponseFuture).join();

        KbQueryResponse queryResponse = extractResponseFromQueryResponse(queryResponseFuture.join().body());
        TextState queryTextState = getTextState(queryResponse);
        watch.stop();

        return FakeNewsDetectorResponse.builder()
                .state(queryTextState)
                .proof(queryResponse.proof())
                .elapsedSeconds(watch.getTotalTimeSeconds())
                .build();
    }

    private FakeNewsDetectorResponse doubleCheck(FakeNewsDetectorRequest request) {
        StopWatch watch = new StopWatch();
        watch.start();
        String query = convertText(request);

        if (query == null) {
            return null;
        }

        CompletableFuture<HttpResponse<String>> queryResponseFuture = sendQueryRequest(httpClient, query);
        KbQueryResponse queryResponse = extractResponseFromQueryResponse(queryResponseFuture.join().body());
        TextState queryState = getTextState(queryResponse);
        log.info("Query text state: {}", queryState);

        if (queryState.equals(TextState.TRUE)) {
            watch.stop();
            return FakeNewsDetectorResponse.builder()
                    .state(TextState.TRUE)
                    .proof(queryResponse.proof())
                    .elapsedSeconds(watch.getTotalTimeSeconds())
                    .build();
        }

        if (queryState.equals(TextState.FAKE)) {
            watch.stop();
            return FakeNewsDetectorResponse.builder()
                    .state(TextState.FAKE)
                    .proof(queryResponse.proof())
                    .elapsedSeconds(watch.getTotalTimeSeconds())
                    .build();
        }

        String negatedQuery = getNegatedQuery(query);
        CompletableFuture<HttpResponse<String>> negatedQueryResponseFuture = sendQueryRequest(httpClient, negatedQuery);
        KbQueryResponse negatedQueryResponse = extractResponseFromQueryResponse(negatedQueryResponseFuture.join().body());
        TextState negatedQueryState = getTextState(negatedQueryResponse);
        log.info("Negated query text state: {}", negatedQueryState);

        if (negatedQueryState.equals(TextState.TRUE)) {
            watch.stop();
            return FakeNewsDetectorResponse.builder()
                    .state(TextState.FAKE)
                    .proof(negatedQueryResponse.proof())
                    .elapsedSeconds(watch.getTotalTimeSeconds())
                    .build();
        }

        watch.stop();
        return FakeNewsDetectorResponse.builder()
                .state(TextState.UNKNOWN)
                .proof(Collections.emptyList())
                .elapsedSeconds(watch.getTotalTimeSeconds())
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
        if (response.inconsistency()) {
            return TextState.FAKE;
        }
        if(response.proof() == null){
            return TextState.UNKNOWN;
        }
        int proofSize = response.proof().size();
        if (proofSize > 0 && !response.noConjecture()) {
            return TextState.TRUE;
        }
        return TextState.UNKNOWN;
    }

    private String getNegatedQuery(String query) {
        return "(not " + query + ")";
    }

    private TextState getVerdict(TextState queryTextState, TextState negatedQueryTextState) {
        if (queryTextState.equals(TextState.TRUE)) {
            return TextState.TRUE;
        }
        if (negatedQueryTextState.equals(TextState.TRUE)) {
            return TextState.FAKE;
        }
        return TextState.UNKNOWN;
    }

    private boolean isOneWord(String input) {
        return input.matches("\\b\\w+\\b");
    }
}
