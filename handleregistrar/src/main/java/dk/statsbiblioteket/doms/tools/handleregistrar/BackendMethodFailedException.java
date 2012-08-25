package dk.statsbiblioteket.doms.tools.handleregistrar;

/**
 * Exception signifying backend method failed.
 */
public class BackendMethodFailedException extends RuntimeException {
    public BackendMethodFailedException(String message) {
        super(message);
    }

    public BackendMethodFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
