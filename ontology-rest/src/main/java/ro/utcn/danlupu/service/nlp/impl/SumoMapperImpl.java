package ro.utcn.danlupu.service.nlp.impl;

import com.articulate.nlp.WSDAnnotator;
import com.articulate.nlp.pipeline.Pipeline;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.service.nlp.SumoMapper;

import java.util.List;


@Service
@Slf4j
public class SumoMapperImpl implements SumoMapper {

    @Override
    public String mapToSumoTerms(String text) {
        Annotation wholeDocument = new Annotation(text);
        wholeDocument.set(CoreAnnotations.DocDateAnnotation.class, "2017-05-08");
        String propString = "tokenize, ssplit, pos, lemma, parse, depparse, ner, wsd, wnmw, tsumo";
        Pipeline p = new Pipeline(true, propString);
        p.pipeline.annotate(wholeDocument);
        List<CoreMap> sentences = wholeDocument.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                String sumo = token.get(WSDAnnotator.SUMOAnnotation.class);
                log.info("Token: {}\nSUMO: {}", token, sumo);
                return text.replace(token.originalText(), sumo);
            }
        }
        return "";
    }
}
