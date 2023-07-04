package ro.utcn.danlupu.model;

import lombok.Builder;

import java.util.List;

@Builder
public record KbQueryResponse(
                              boolean inconsistency,
                              boolean noConjecture,
                              boolean containsFalse,
                              String tptpStatus,
                              List<String> bindings,
                              List<String> proof) {
}
