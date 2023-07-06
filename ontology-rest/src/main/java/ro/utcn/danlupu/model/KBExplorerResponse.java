package ro.utcn.danlupu.model;

import lombok.Builder;

import java.util.List;

@Builder
public record KBExplorerResponse(
        List<String> asArgument,
        List<String> antecedent,
        List<String> consequent,
        List<String> statement
) {
}
