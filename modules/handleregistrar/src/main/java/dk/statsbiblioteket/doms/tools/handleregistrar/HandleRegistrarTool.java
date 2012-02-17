package dk.statsbiblioteket.doms.tools.handleregistrar;

import java.io.File;

/**
 * Tool, that given an RDF query that returns a list of objects, an URL
 * pattern and a configuration file registers all objects matching the RDF query
 * as handles resolving to the URL pattern.
 */
public class HandleRegistrarTool {
    public static void main(String[] args) {
        // TODO Reasonable argument reading, please.
        RegistrarConfiguration config
                = new PropertyBasedRegistrarConfiguration(new File(args[2]));
        HandleRegistrar registrar = new BasicHandleRegistrar(config,
                new DomsHandler(config),
                new HandleHandler(config));
        registrar.addHandles(args[0], args[1]);
    }
}
