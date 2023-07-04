package ro.utcn.danlupu.service.query;


public interface KBQueryServiceFactory {
    KBQueryService getQueryService(String engineName);
}
