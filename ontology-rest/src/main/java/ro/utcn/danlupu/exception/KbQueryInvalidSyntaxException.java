package ro.utcn.danlupu.exception;

public class KbQueryInvalidSyntaxException extends RuntimeException {

    public KbQueryInvalidSyntaxException(String query) {
        super("Syntax error in query: " + query);
    }
}
