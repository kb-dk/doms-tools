package dk.statsbiblioteket.doms.tools.handleregistrar;

import java.net.URL;

/**
 * Provide configuration for the handle registrar.
 */
public interface RegistrarConfiguration {
    String getFedoraLocation();

    String getUsername();

    String getPassword();

    URL getDomsWSAPIEndpoint();

    String getHandlePrefix();

    String getPrivateKeyPath();

    String getPrivateKeyPassword();
}
