package ro.utcn.danlupu.service.kb.query;


public interface KBQueryServiceFactory {
    KBQueryService getQueryService(String engineName);
}
