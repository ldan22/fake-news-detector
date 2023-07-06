package ro.utcn.danlupu.service.verbalizer.impl;

import com.articulate.sigma.Formula;
import com.articulate.sigma.nlg.LanguageFormatter;
import com.articulate.sigma.utils.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.service.kb.KBFactory;
import ro.utcn.danlupu.service.verbalizer.SumoVerbalizer;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SumoVerbalizerImpl implements SumoVerbalizer {

    private final KBFactory kbFactory;

    @Override
    public String verbalize(Formula formula) {
        String strForm = formula.getFormula();
        String language = "EnglishLanguage";

        LanguageFormatter languageFormatter = new LanguageFormatter(
                strForm,
                kbFactory.getKB().getFormatMap(language),
                kbFactory.getKB().getTermFormatMap(language),
                kbFactory.getKB(),
                language);

        return StringUtil.filterHtml(languageFormatter.htmlParaphrase(""));
    }

    @Override
    public List<String> verbalize(List<Formula> formulas) {
        return formulas.stream()
                .map(this::verbalize)
                .collect(Collectors.toList());
    }
}
