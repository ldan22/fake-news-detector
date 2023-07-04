package ro.utcn.danlupu.fakenewsdetectorapi.dto;

import lombok.Builder;
import ro.utcn.danlupu.fakenewsdetectorapi.model.TextState;

import java.util.List;

@Builder
public record FakeNewsDetectorResponse(TextState state, List<String> proof) {
}
