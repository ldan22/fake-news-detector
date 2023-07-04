package ro.utcn.danlupu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.query.KBQueryServiceFactory;
import ro.utcn.danlupu.service.KBService;


@Service
@RequiredArgsConstructor
@Slf4j
public class KBServiceImpl implements KBService {

    private final KBQueryServiceFactory kbQueryServiceFactory;

    @Override
    public KbQueryResponse performQuery(String engine, String query) {
        return kbQueryServiceFactory
                .getQueryService(engine)
                .load()
                .performQuery(query);
    }
}
