package ro.utcn.danlupu.service.nlp;

public interface NlpServiceFactory {
    NlpService getNlpProcessor(String type);
}
