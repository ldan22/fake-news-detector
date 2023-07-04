package ro.utcn.danlupu.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.service.ManualMappingsExtractor;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ManualMappingsExtractorImpl implements ManualMappingsExtractor {

    private final ResourceLoader resourceLoader;
    private final String MANUAL_MAPPINGS_FILE = "classpath:ManualMappings.jsonl";

    public String getSumoMapping(String searchText) {
        log.info("Start parsing JsonL file: {}", MANUAL_MAPPINGS_FILE);
        log.info("Looking for: {}", searchText);

        try {
            Resource resource = resourceLoader.getResource(MANUAL_MAPPINGS_FILE);
            InputStream inputStream = resource.getInputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            MappingIterator<JsonNode> it = objectMapper
                    .readerFor(JsonNode.class)
                    .readValues(inputStream);
            while (it.hasNextValue()) {
                JsonNode node = it.nextValue();
                String text = node.get("text").asText();
                if (text.equals(searchText)) {
                    return node.get("sumo").asText();
                }
            }
            inputStream.close();
            it.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
