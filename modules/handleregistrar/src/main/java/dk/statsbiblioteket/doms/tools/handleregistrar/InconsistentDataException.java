package dk.statsbiblioteket.doms.tools.handleregistrar;

/**
 * Exception signifying inconsistent data.
 */
public class InconsistentDataException extends RuntimeException {
    public InconsistentDataException(String message) {
        super(message);
    }

    public InconsistentDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
