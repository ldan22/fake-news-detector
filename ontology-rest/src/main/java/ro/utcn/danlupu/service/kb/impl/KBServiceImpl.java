package ro.utcn.danlupu.service.kb.impl;

import com.articulate.sigma.Formula;
import com.articulate.sigma.HTMLformatter;
import com.articulate.sigma.nlg.LanguageFormatter;
import com.articulate.sigma.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.model.KBExplorerResponse;
import ro.utcn.danlupu.model.KbQueryResponse;
import ro.utcn.danlupu.service.kb.KBFactory;
import ro.utcn.danlupu.service.kb.query.KBQueryServiceFactory;
import ro.utcn.danlupu.service.kb.KBService;
import ro.utcn.danlupu.service.verbalizer.SumoVerbalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class KBServiceImpl implements KBService {

    private final KBQueryServiceFactory kbQueryServiceFactory;
    private final KBFactory kbFactory;
    private final SumoVerbalizer verbalizer;


    @Override
    public KbQueryResponse performQuery(String engine, String query) {
        return kbQueryServiceFactory
                .getQueryService(engine)
                .load()
                .performQuery(query);
    }

    @Override
    public KBExplorerResponse getTermInformation(String term) {
        return KBExplorerResponse.builder()
                .asArgument(getExpressionWhereTermIsAsArgument(term))
                .antecedent(getExpressionWhereTermIsOfType(term, "ant"))
                .consequent(getExpressionWhereTermIsOfType(term, "cons"))
                .statement(getExpressionWhereTermIsOfType(term, "stmt"))
                .build();
    }

    private List<String> getExpressionWhereTermIsAsArgument(String term) {
        List<Formula> asArgumentFormulas = new ArrayList<>();
        for (int arg = 1; arg < 6; arg++) {
            asArgumentFormulas.addAll(getSumoFormulas(term, "arg", arg));
        }
        return verbalizer.verbalize(asArgumentFormulas);
    }

    private List<String> getExpressionWhereTermIsOfType(String term, String type) {
        List<Formula> formulas = getSumoFormulas(term, "ant", 0);
        return verbalizer.verbalize(formulas);
    }

    private List<Formula> getSumoFormulas(String term, String type, int arg) {
        return kbFactory.getKB().ask(type, arg, term);
    }
}
