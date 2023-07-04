package ro.utcn.danlupu.service.kb.query.impl;

import com.articulate.sigma.nlg.LanguageFormatter;
import com.articulate.sigma.tp.Vampire;
import com.articulate.sigma.trans.TPTP3ProofProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.exception.KbQueryInvalidSyntaxException;
import ro.utcn.danlupu.exception.KbQueryVampireInitException;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.kb.KBFactory;
import ro.utcn.danlupu.service.kb.query.KBQueryService;
import tptp_parser.TPTPFormula;

import java.util.List;
import java.util.stream.Collectors;


@Service("VAMPIRE")
@Slf4j
@RequiredArgsConstructor
public class VampireKBQueryService implements KBQueryService {

    private final KBFactory kbFactory;
    private static final int VAMPIRE_TIMEOUT = 600;


    @Override
    public KbQueryResponse performQuery(String query) {
        Vampire vampire = kbFactory.getKB().askVampire(query, VAMPIRE_TIMEOUT, 10);

        if (vampire == null || vampire.output == null) {
            log.info("Vampire: {}", vampire);
            throw new KbQueryVampireInitException();
        }

        if (vampire.output.contains("Syntax error detected")) {
            throw new KbQueryInvalidSyntaxException(query);
        }

        TPTP3ProofProcessor tpp = parseVampireProofOutput(vampire, query);

        log.info("Status: {}", tpp.status);
        log.info("Bindings: {}", tpp.bindings);
        log.info("Inconsistency: {}", tpp.inconsistency);
        log.info("Contains false: {}", tpp.containsFalse);
        log.info("No conjecture: {}", tpp.noConjecture);

        List<String> sumoProof = tpp.proof.stream()
                .map(tptpFormula -> tptpFormula.sumo)
                .toList();

        String language = "EnglishLanguage";
        sumoProof.stream().forEach(sumoStmt -> {
            LanguageFormatter languageFormatter = new LanguageFormatter(
                    sumoStmt,
                    kbFactory.getKB().getFormatMap(language),
                    kbFactory.getKB().getTermFormatMap(language),
                    kbFactory.getKB(),
                    language);
            log.info(languageFormatter.htmlParaphrase(""));
        });

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
        kbFactory.getKB().loadVampire();
        return this;
    }

    private TPTP3ProofProcessor parseVampireProofOutput(Vampire vampire, String query) {
        TPTP3ProofProcessor tpp = new TPTP3ProofProcessor();
        tpp.parseProofOutput(vampire.output, query, kbFactory.getKB(), vampire.qlist);
        return tpp;
    }
}
