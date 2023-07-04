package ro.utcn.danlupu.service.nlp;

import com.articulate.nlp.semRewrite.Interpreter;

import java.io.IOException;

public interface InterpreterFactory {
    Interpreter getInterpreter() throws IOException;
}
