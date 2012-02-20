package dk.statsbiblioteket.doms.tools.handleregistrar;

/**
 * Mockup pid resolver handler that always succeeds, and count calls and
 * remembers parameters in public variables.
 */
public class MockupPidResolverHandler implements PidResolverHandler {
    public int registerPidCount;
    public String registerPidRepositoryId;
    public String registerPidPid;
    public String registerPidUrlPattern;
    public int addUrlToPidAtServerCount;
    public String addUrlToPidAtServerPid;
    public String addUrlToPidAtServerUrl;
    public int replaceUrlOfPidAtServerCount;
    public String replaceUrlOfPidAtServerPid;
    public String replaceUrlOfPidAtServerUrl;
    public int AddPidToServerCount;
    public String AddPidToServerPid;
    public String AddPidToServerUrl;

    @Override
    public void registerPid(String repositoryId, String pid, String urlPattern)
            throws RegisteringPidFailedException {
        registerPidCount++;
        registerPidRepositoryId = repositoryId;
        registerPidPid = pid;
        registerPidUrlPattern = urlPattern;
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addUrlToPidAtServer(String pid, String url)
            throws RegisteringPidFailedException {
        addUrlToPidAtServerCount++;
        addUrlToPidAtServerPid = pid;
        addUrlToPidAtServerUrl = url;
    }

    @Override
    public void replaceUrlOfPidAtServer(String pid, int indexOfPidValue,
                                        String url)
            throws RegisteringPidFailedException {
        replaceUrlOfPidAtServerCount++;
        replaceUrlOfPidAtServerPid = pid;
        replaceUrlOfPidAtServerUrl = url;
    }

    @Override
    public void addPidToServer(String pid, String url)
            throws RegisteringPidFailedException {
        AddPidToServerCount++;
        AddPidToServerPid = pid;
        AddPidToServerUrl = url;
    }
}
