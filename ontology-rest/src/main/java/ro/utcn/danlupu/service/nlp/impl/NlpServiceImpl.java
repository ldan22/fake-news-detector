package ro.utcn.danlupu.service.nlp.impl;

import com.articulate.sigma.Formula;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.model.TextInterpreterRequest;
import ro.utcn.danlupu.model.TextInterpreterResponse;
import ro.utcn.danlupu.service.nlp.InterpreterFactory;
import ro.utcn.danlupu.service.nlp.NlpService;
import ro.utcn.danlupu.service.ManualMappingsExtractor;

import java.io.IOException;
import java.util.List;


@Service("SIGMANLP")
@RequiredArgsConstructor
@Slf4j
public class NlpServiceImpl implements NlpService {

    private final InterpreterFactory interpreterFactory;
    private final ManualMappingsExtractor manualMappingsExtractor;

    @Override
    public TextInterpreterResponse interpretText(TextInterpreterRequest textInterpreterRequest) {
        String text = textInterpreterRequest.text().toLowerCase();
        String sumoMapping = manualMappingsExtractor.getSumoMapping(text);
        if (sumoMapping == null) {
            log.info("No manual translation found. Using SigmaNLP:");
            sumoMapping = interpretWithSigmaNlp(text);
        }
        return new TextInterpreterResponse(sumoMapping);
    }

    private String interpretWithSigmaNlp(String text) {
        List<String> forms;
        try {
            forms = interpreterFactory.getInterpreter().interpret(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.info(forms.toString());
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : forms) {
            Formula theForm = new Formula(s);
            stringBuilder.append(theForm);
        }
        return stringBuilder.toString();
    }
}
