package ro.utcn.danlupu.fakenewsdetectorapi.service;

import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorRequest;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorResponse;

import java.util.Map;

public interface FakeNewsDetectorService {
    FakeNewsDetectorResponse check(FakeNewsDetectorRequest request);

    Map<String, Object> getTermInformation(String term);
}
