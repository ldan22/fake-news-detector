package ro.utcn.danlupu.service.nlp;

import com.articulate.nlp.pipeline.Pipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class MapperPipeline {
    private volatile Pipeline pipeline;

    public Pipeline getPipeline() throws IOException {
        if (pipeline == null) {
            synchronized (MapperPipeline.class) {
                if (pipeline == null) {
                    String propString = "tokenize, ssplit, pos, lemma, parse, depparse, ner, wsd, wnmw";
                    log.info("Pipeline is null. Initialize: {}", propString);
                    pipeline = new Pipeline(true, propString);
                }
            }
        }
        return pipeline;
    }
}
