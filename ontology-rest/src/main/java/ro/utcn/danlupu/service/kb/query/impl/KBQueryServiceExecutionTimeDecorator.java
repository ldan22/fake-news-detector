package ro.utcn.danlupu.service.kb.query.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.kb.query.KBQueryService;


@RequiredArgsConstructor
@Slf4j
public class KBQueryServiceExecutionTimeDecorator implements KBQueryService {

    private final KBQueryService kbQueryService;

    @Override
    public KbQueryResponse performQuery(String query) {
        StopWatch watch = new StopWatch();
        watch.start();
        KbQueryResponse response = kbQueryService.performQuery(query);
        watch.stop();
        log.info("Query service finished processing. Took: {} ms", watch.getTime());
        return response;
    }

    @Override
    public KBQueryService load() {
        return kbQueryService.load();
    }
}
