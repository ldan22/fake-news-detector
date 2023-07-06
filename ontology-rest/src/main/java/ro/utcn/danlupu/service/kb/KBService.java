package ro.utcn.danlupu.service.kb;

import ro.utcn.danlupu.model.KBExplorerResponse;
import ro.utcn.danlupu.model.KbQueryResponse;


public interface KBService {
    KbQueryResponse performQuery(String engine, String query);
    KBExplorerResponse getTermInformation(String term);
}
