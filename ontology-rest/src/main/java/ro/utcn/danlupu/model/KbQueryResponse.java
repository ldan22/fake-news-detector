package ro.utcn.danlupu.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.util.List;

@Builder
public record KbQueryResponse(
                              boolean inconsistency,
                              boolean noConjecture,
                              boolean containsFalse,
                              String tptpStatus,
                              List<String> bindings,
                              @JsonIgnore List<String> proof) {
}
