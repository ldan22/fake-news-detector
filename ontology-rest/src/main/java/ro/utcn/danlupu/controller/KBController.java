package ro.utcn.danlupu.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ro.utcn.danlupu.model.KbQueryRequest;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.KBService;


@CrossOrigin
@RestController
@RequestMapping("/api/v1/kb")
@Validated
@Slf4j
@AllArgsConstructor
public class KBController {

    private final KBService kbService;


    @PostMapping("/query")
    public ResponseEntity<KbQueryResponse> performQuery(@RequestParam(defaultValue = "vampire") String engine,
                                                        @RequestBody KbQueryRequest kbQueryRequest) {
        log.info("Request received: Perform query: {}", kbQueryRequest.query());
        KbQueryResponse response = kbService.performQuery(engine, kbQueryRequest.query());
        return ResponseEntity.ok(response);
    }
}
