package dk.statsbiblioteket.doms.tools.cleaner;

import java.io.*;
import java.util.ArrayList;
import java.net.URL;

import dk.statsbiblioteket.doms.client.DOMSWSClient;
import dk.statsbiblioteket.util.qa.QAInfo;

/**
 * Cleans up the mess when the ingester has failed, and put stuff in the
 * lukeWarmFolder.
 */
@QAInfo(author = "mar, eab", reviewers = "", level = QAInfo.Level.NORMAL,
        state = QAInfo.State.QA_OK)
public class Domscleaner {

    private static URL domsAPIWSLocation;
    private static String username;
    private static String password;

    /**
     *
     * @param args filepaths [-password=<password>][-username=<username>]
     *                      [-wsdl=<wsdlFileLocation>]
     */
    public static void main(String[] args) {
        File file = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        BufferedReader dis = null;
        try  {
        domsAPIWSLocation = new URL(
                "http://alhena:7980/centralDomsWebservice/central/?wsdl");

        username = "fedoraAdmin";
        password = "fedoraAdminPass";
        } catch(Exception e) {
            System.err.println(e);
        }

        ArrayList<String> strings = new ArrayList<String>();
        for(int i = 0 ; i < args.length ; i++) {
            System.out.println(i + ": " + args[i]);
            try {
                if (args[i].startsWith("-wsdl=")) {
                    domsAPIWSLocation = new URL(args[i].substring("-wsdl=".length()));
                } else if (args[i].startsWith("-username=")) {
                    username = args[i].substring("-username=".length());
                } else if (args[i].startsWith("-password=")) {
                    password = args[i].substring("-password=".length());
                } else {

                    file = new File(args[i]);
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    dis = new BufferedReader(new InputStreamReader(bis));
                    String s = "";
                    while(dis.ready()) {
                        s = dis.readLine();
                        strings.add(s);
                    }
                }

            } catch(Exception e) {
                System.out.println(e);
            }


            markAsDeletedInDOMS(strings);

        }
    }

    /**
     *
     * @param pids list of pids to delete.
     */
    private static void markAsDeletedInDOMS(ArrayList<String> pids) {
        DOMSWSClient domswsclient = new DOMSWSClient();
        try {
            domswsclient.login(domsAPIWSLocation, username, password);
            domswsclient.deleteObjects(pids);
        } catch(Exception e) {
            System.err.println(e);
        }



    }
}
