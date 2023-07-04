package ro.utcn.danlupu.service.query.impl;

import com.articulate.sigma.tp.EProver;
import com.articulate.sigma.trans.TPTP3ProofProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.exception.KbQueryInvalidSyntaxException;
import ro.utcn.danlupu.exception.KbQueryVampireInitException;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.KBFactory;
import ro.utcn.danlupu.service.query.KBQueryService;
import tptp_parser.TPTPFormula;

import java.util.stream.Collectors;


@Service("EPROVER")
@Slf4j
@RequiredArgsConstructor
public class EProverKBQueryService implements KBQueryService {
    private final KBFactory kbFactory;

    private static final int EPROVER_TIMEOUT = 600;

    @Override
    public KbQueryResponse performQuery(String query) {
        EProver eProver = kbFactory.getKB().askEProver(query, EPROVER_TIMEOUT, 1);

        if (eProver == null || eProver.output == null) {
            throw new KbQueryVampireInitException();
        }

        if (eProver.output.contains("Syntax error detected")) {
            throw new KbQueryInvalidSyntaxException(query);
        }

        TPTP3ProofProcessor tpp = parseEPRoverOutput(eProver, query);

        log.info("Proof: {}", tpp.proof);
        log.info("Bindings: {}", tpp.bindings);

        return KbQueryResponse.builder()
                .bindings(tpp.bindings)
                .tptpStatus(tpp.status)
                .inconsistency(tpp.inconsistency)
                .containsFalse(tpp.containsFalse)
                .noConjecture(tpp.noConjecture)
                .proof(tpp.proof.stream().map(TPTPFormula::toString).collect(Collectors.toList()))
                .build();
    }

    @Override
    public KBQueryService load() {
        kbFactory.getKB().loadEProver();
        return this;
    }

    private TPTP3ProofProcessor parseEPRoverOutput(EProver eProver, String query) {
        TPTP3ProofProcessor tpp = new TPTP3ProofProcessor();
        tpp.parseProofOutput(eProver.output, query, kbFactory.getKB(), eProver.qlist);
        return tpp;
    }
}
