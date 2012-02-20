package dk.statsbiblioteket.doms.tools.handleregistrar;

import java.util.List;

/**
 * Find objects in repository, and update IDs in repository.
 */
public interface RepositoryHandler {
    public List<String> findObjectFromQuery(String query)
            throws BackendInvalidCredsException, BackendMethodFailedException;

    public String addHandleToObject(String pid);
}
