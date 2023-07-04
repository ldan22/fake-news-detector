package ro.utcn.danlupu.service.query.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ro.utcn.danlupu.service.query.KBQueryService;
import ro.utcn.danlupu.service.query.KBQueryServiceFactory;


@Component
public class KBQueryServiceFactoryImpl implements KBQueryServiceFactory {


    private final KBQueryService eproverKBQueryService;
    private final KBQueryService vampireKBQueryService;

    public KBQueryServiceFactoryImpl(@Qualifier("EPROVER") KBQueryService eproverKBQueryService,
                                     @Qualifier("VAMPIRE") KBQueryService vampireKBQueryService) {
        this.eproverKBQueryService = new KBQueryServiceExecutionTimeDecorator(eproverKBQueryService);
        this.vampireKBQueryService = new KBQueryServiceExecutionTimeDecorator(vampireKBQueryService);
    }

    @Override
    public KBQueryService getQueryService(String engineName) {
        String normalizedEngineName = engineName.toUpperCase();

        return switch (normalizedEngineName) {
            case "EPROVER" -> eproverKBQueryService;
            case "VAMPIRE" -> vampireKBQueryService;
            default -> throw new IllegalArgumentException();
        };
    }
}
