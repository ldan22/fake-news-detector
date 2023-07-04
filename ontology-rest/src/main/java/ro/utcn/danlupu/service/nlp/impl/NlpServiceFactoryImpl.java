package ro.utcn.danlupu.service.nlp.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ro.utcn.danlupu.service.nlp.NlpService;
import ro.utcn.danlupu.service.nlp.NlpServiceFactory;

@Component
public class NlpServiceFactoryImpl implements NlpServiceFactory {

    private final NlpService sigmaNlpService;
    private final NlpService gptNlpService;

    public NlpServiceFactoryImpl(@Qualifier("SIGMANLP") NlpService sigmaNlpService,
                                 @Qualifier("GPT") NlpService gptNlpService) {
        this.sigmaNlpService = sigmaNlpService;
        this.gptNlpService = gptNlpService;
    }

    @Override
    public NlpService getNlpProcessor(String type) {
        return switch (type.toUpperCase()) {
            case "GPT" -> gptNlpService;
            case "SIGMANLP" -> sigmaNlpService;
            default -> throw new IllegalArgumentException();
        };
    }
}
