package ro.utcn.danlupu.fakenewsdetectorapi.service;

import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorRequest;
import ro.utcn.danlupu.fakenewsdetectorapi.dto.FakeNewsDetectorResponse;

public interface FakeNewsDetectorService {
    FakeNewsDetectorResponse check(FakeNewsDetectorRequest request);
}
