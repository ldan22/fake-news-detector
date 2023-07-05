package ro.utcn.danlupu.fakenewsdetectorapi.dto;

import lombok.Builder;
import lombok.ToString;
import ro.utcn.danlupu.fakenewsdetectorapi.model.TextState;

import java.util.List;

@Builder
public record FakeNewsDetectorResponse(TextState state, @ToString.Exclude List<String> proof) {
}
