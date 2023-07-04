package ro.utcn.danlupu.service.query;

import ro.utcn.danlupu.model.KbQueryResponse;

public interface KBQueryService {
    KbQueryResponse performQuery(String query);

    KBQueryService load();
}
