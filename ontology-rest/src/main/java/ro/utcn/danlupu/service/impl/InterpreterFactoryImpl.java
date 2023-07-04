package ro.utcn.danlupu.service.impl;

import com.articulate.nlp.RelExtract;
import com.articulate.nlp.corpora.TimeBank;
import com.articulate.nlp.semRewrite.Interpreter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ro.utcn.danlupu.service.InterpreterFactory;

import java.io.IOException;


@Component
@Slf4j
public class InterpreterFactoryImpl implements InterpreterFactory {

    private volatile Interpreter interpreter;


    @Override
    public Interpreter getInterpreter() throws IOException {
        if (interpreter == null) {
            synchronized (KBFactoryImpl.class) {
                if (interpreter == null) {
                    log.info("Interpreter is null. Initialize.");
                    TimeBank.init();
                    interpreter = new Interpreter();
                    interpreter.initialize();
                    RelExtract.initOnce();
                }
            }
        }
        return interpreter;
    }
}
