package dk.statsbiblioteket.doms.tools.handleregistrar;

import junit.framework.TestCase;

import java.io.File;

/**
 * Test BasicHandleRegistrar calls the expected methods the expected number of
 * times.
 */
public class BasicHandleRegistrarTest extends TestCase {
    public void testAddHandles() throws Exception {
        MockupRepositoryHandler repositoryHandler
                = new MockupRepositoryHandler();
        MockupPidResolverHandler pidResolverHandler
                = new MockupPidResolverHandler();
        new BasicHandleRegistrar(new PropertyBasedRegistrarConfiguration(
                new File("src/test/resources/handleregistrar.properties")),
                                 repositoryHandler, pidResolverHandler)
                .addHandles("foo", "bar");
        assertEquals(1, repositoryHandler.findObjectFromQueryCount);
        assertEquals("foo", repositoryHandler.findObjectFromQueryQuery);
        assertEquals(1, repositoryHandler.findObjectFromQueryCount);
        assertEquals("info:fedora/foo", repositoryHandler.addHandleToObjectPid);
        assertEquals(1, pidResolverHandler.registerPidCount);
        assertEquals("hdl:109.1.3/foo", pidResolverHandler.registerPidPid);
        assertEquals("info:fedora/foo", pidResolverHandler.registerPidRepositoryId);
        assertEquals("bar", pidResolverHandler.registerPidUrlPattern);
    }
}