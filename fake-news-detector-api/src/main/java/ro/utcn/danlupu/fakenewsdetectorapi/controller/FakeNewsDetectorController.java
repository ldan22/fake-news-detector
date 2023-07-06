package ro.utcn.danlupu.fakenewsdetectorapi.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorRequest;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorResponse;
import ro.utcn.danlupu.fakenewsdetectorapi.service.FakeNewsDetectorService;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/detector")
@Validated
@Slf4j
@AllArgsConstructor
public class FakeNewsDetectorController {

    private final FakeNewsDetectorService fakeNewsDetectorService;

    @PostMapping
    public ResponseEntity<FakeNewsDetectorResponse> performQuery(@RequestBody FakeNewsDetectorRequest request) {
        log.info("Request received: Perform query: {}", request.text());
        FakeNewsDetectorResponse fakeNewsDetectorResponse = fakeNewsDetectorService.check(request);
        return ResponseEntity.ok(fakeNewsDetectorResponse);
    }

    @GetMapping("/term")
    public ResponseEntity<Map<String, Object>> getTermInformation(@RequestParam String term) {
        log.info("Request received: Get  term information: {}", term);
        Map<String, Object> termInfo = fakeNewsDetectorService.getTermInformation(term);
        return ResponseEntity.ok(termInfo);
    }

}
