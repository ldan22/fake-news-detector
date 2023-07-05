package ro.utcn.danlupu.service.nlp.impl;

import com.articulate.nlp.WSDAnnotator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.utcn.danlupu.service.nlp.MapperPipeline;
import ro.utcn.danlupu.service.nlp.SumoMapper;

import java.io.IOException;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class SumoMapperImpl implements SumoMapper {

    private final MapperPipeline mapperPipeline;

    @Override
    public String mapToSumoTerms(String text) {
        try {
            Annotation document = new Annotation(text);
            setDocDateAnnotation(document);
            mapperPipeline.getPipeline().pipeline.annotate(document);
            List<CoreMap> sentences = getSentences(document);
            String mappedText = text;
            for (CoreMap sentence : sentences) {
                List<CoreLabel> tokens = getTokens(sentence);
                for (CoreLabel token : tokens) {
                    String sumo = getSumoTerm(token);
                    log.info("Orig: {}, Sumo: {}", token.originalText(), sumo);
                    mappedText = mappedText.replace(token.originalText(), sumo);
                }
            }
            return mappedText;
        } catch (IOException e) {
            return text;
        }
    }

    private void setDocDateAnnotation(Annotation document) {
        document.set(CoreAnnotations.DocDateAnnotation.class, "2017-05-08");
    }

    private List<CoreMap> getSentences(Annotation document) {
        return document.get(CoreAnnotations.SentencesAnnotation.class);
    }

    private List<CoreLabel> getTokens(CoreMap sentence) {
        return sentence.get(CoreAnnotations.TokensAnnotation.class);
    }

    private String getSumoTerm(CoreLabel token) {
        return token.get(WSDAnnotator.SUMOAnnotation.class);
    }
}
