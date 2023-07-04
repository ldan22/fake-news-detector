package ro.utcn.danlupu.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.utcn.danlupu.model.TextInterpreterRequest;
import ro.utcn.danlupu.model.TextInterpreterResponse;
import ro.utcn.danlupu.service.nlp.NlpServiceFactory;


@CrossOrigin
@RestController
@RequestMapping("/api/v1/nlp")
@Validated
@Slf4j
@AllArgsConstructor
public class NlpController {

    private final NlpServiceFactory nlpServiceFactory;

    @PostMapping("/interpret")
    public ResponseEntity<TextInterpreterResponse> interpretText(@RequestBody TextInterpreterRequest textInterpreterRequest) {
        log.info("Request received: Interpret text to SUMO query.");
        TextInterpreterResponse response = nlpServiceFactory.getNlpProcessor(textInterpreterRequest.nlpProcessor())
                .interpretText(textInterpreterRequest);
        return ResponseEntity.ok(response);
    }
}
