package ro.utcn.danlupu.service.kb;

import ro.utcn.danlupu.model.KbQueryResponse;


public interface KBService {
    KbQueryResponse performQuery(String engine, String query);
    String getTermInformation(String term);
}
