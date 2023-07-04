package ro.utcn.danlupu.service;

import ro.utcn.danlupu.model.KbQueryResponse;


public interface KBService {
    KbQueryResponse performQuery(String engine, String query);
}
