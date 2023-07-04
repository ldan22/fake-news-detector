package ro.utcn.danlupu.exception;

public class KbQueryVampireInitException extends RuntimeException {
    public KbQueryVampireInitException() {
        super("Vampire is not initialized");
    }
}
