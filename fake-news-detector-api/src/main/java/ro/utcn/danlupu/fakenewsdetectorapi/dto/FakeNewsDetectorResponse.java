package ro.utcn.danlupu.fakenewsdetectorapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import ro.utcn.danlupu.fakenewsdetectorapi.model.TextState;

import java.util.List;

@Builder
public record FakeNewsDetectorResponse(TextState state, @JsonIgnore List<String> proof) {
}
