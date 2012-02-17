package dk.statsbiblioteket.doms.tools.handleregistrar;

import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AddValueRequest;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.Common;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ModifyValueRequest;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.Util;
import net.handle.hdllib.ValueReference;

import java.io.File;
import java.nio.charset.Charset;
import java.security.PrivateKey;

/**
 * Implementation of PID-Resolver based on Handle System
 */
public class HandleHandler implements PidResolverHandler {
    /** The admin ID */
    private static final String ADMIN_ID = "0.NA/109.3.1";   // TODO rite?
    /** Password/passphrase for getting handle private key */
    private static final String HANDLE_PASSWORD = ""; // TODO set password
    /** Path to admpriv.bin file */
    private static final String PRIVATE_KEY_PATH = System.getProperty("user.home")
            + System.getProperty("path.separator") + ".config"
            + System.getProperty("path.separator") + "handle";
    /** Charset used by the Handle system. */
    private static final Charset DEFAULT_ENCODING = Charset.forName("UTF8");
    /** Admin index aka Handle index, default value 300 */
    private static final int ADMIN_INDEX = 300;
    /** Handle admin record index. */
    private static final int ADMIN_RECORD_INDEX = 200;
    /** Handle URL record index. */
    private static final int URL_RECORD_INDEX = 1;
    /** Handle value references. */
    private static final ValueReference[] REFERENCES = null;
    /** Handle admin read permission. */
    private static final Boolean ADMIN_READ = true;
    /** Handle admin write permission. */
    private static final Boolean ADMIN_WRITE = true;
    /** Handle public read permission. */
    private static final Boolean PUBLIC_READ = true;
    /** Handle public write permission. */
    private static final Boolean PUBLIC_WRITE = false;
    /** Init a public key authentication information object. */
    private static final PublicKeyAuthenticationInfo pubKeyAuthInfo;
    static {
        // AuthenticationInfo is constructed with the admin handle, index,
        // and PrivateKey as arguments.
        PrivateKey privateKey = loadPrivateKey();
        pubKeyAuthInfo = new PublicKeyAuthenticationInfo(
                HandleHandler.ADMIN_ID.getBytes(HandleHandler.DEFAULT_ENCODING),
                HandleHandler.ADMIN_INDEX, privateKey);
    }
    /** Configuration info for registrar. */
    private final RegistrarConfiguration config; //TODO: Add config

    /**
     * Initialize handle handler.
     *
     * @param config The configuration used.
     */
    public HandleHandler(RegistrarConfiguration config) {
        this.config = config;
    }

    /**
     * Load the private key from file.
     *
     * @return The private key loaded from file.
     *
     * @throws PrivateKeyException If something went wrong loading the private
     *                             key.
     */
    private static PrivateKey loadPrivateKey() throws PrivateKeyException {
        File privateKeyFile;
        privateKeyFile = new File(PRIVATE_KEY_PATH);
        PrivateKey key;

        if (!privateKeyFile.exists()) {
            throw new PrivateKeyException(
                    "The admin private key file could not be found.");
        }

        if (!privateKeyFile.canRead()) {
            throw new PrivateKeyException(
                    "The admin private key file cannot be read.");
        }

        try {
            key = Util.getPrivateKeyFromFileWithPassphrase(privateKeyFile,
                                                           HANDLE_PASSWORD);
        } catch (Exception e) {
            String message = "The admin private key could not be used, "
                    + " was the correct password used?" + e.getMessage();
            throw new PrivateKeyException(message, e);
        }
        return key;
    }

    @Override
    public void registerPid(String repositoryId, String pid, String urlPattern)
            throws RegisteringPidFailedException {
        String urlToRegister = String.format(urlPattern, repositoryId);
        HandleValue values[] = new HandleValue[]{};
        boolean handleExists;

        // Lookup handle in handleserver
        try {
            values = new HandleResolver().resolveHandle(pid, null, null);
            handleExists = (values != null);
        } catch (HandleException e) {  // True exception-handling, lol :)
            int exceptionCode = e.getCode();
            if (exceptionCode == HandleException.HANDLE_DOES_NOT_EXIST) {
                handleExists = false;
            } else {
                throw new RegisteringPidFailedException(
                        "Did not succeed in resolving handle, existing or not.",
                        e);
            }
        }

        if (handleExists) {
            // Handle was there, now find its url
            for (HandleValue value : values) {
                String type = value.getTypeAsString();
                if (type.equalsIgnoreCase("URL")) {
                    String urlAtServer = value.getDataAsString();

                    if (urlAtServer.equalsIgnoreCase(urlToRegister)) {
                        // It was the same url, so just return
                        return;
                    } else {
                        int indexAtServer = value.getIndex();
                        // It was a different url, replace it
                        replaceUrlOfPidAtServer(pid, indexAtServer,
                                                urlToRegister);
                    }
                }
            }
            // There was no url, so add it to the existing handle
            addUrlToPidAtServer(pid, urlToRegister);

        } else {
            // If not there, add handle and url in handle server
            addPidToServer(pid, urlToRegister);
        }
    }

    @Override
    public void addUrlToPidAtServer(String pid, String url)
            throws RegisteringPidFailedException {

        // Create the new value to be registered at the server. This will
        // be added to the given handle
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        HandleValue newValue = new HandleValue(URL_RECORD_INDEX,
                                               // unique index
                                               "URL".getBytes(DEFAULT_ENCODING),
                                               // handle value type
                                               url.getBytes(DEFAULT_ENCODING),
                                               // value data
                                               HandleValue.TTL_TYPE_RELATIVE,
                                               Common.DEFAULT_SESSION_TIMEOUT,
                                               timestamp, REFERENCES,
                                               ADMIN_READ, ADMIN_WRITE,
                                               PUBLIC_READ, PUBLIC_WRITE);

        // Create the request to send and the resolver to send it
        AddValueRequest request = new AddValueRequest(
                pid.getBytes(DEFAULT_ENCODING), newValue, pubKeyAuthInfo);
        HandleResolver resolver = new HandleResolver();
        AbstractResponse response;

        // Let the resolver process the request
        try {
            response = resolver.processRequest(request);
        } catch (HandleException e) {
            throw new RegisteringPidFailedException(
                    "Could not process the request to add URL to handle at the server.",
                    e);
        }

        // Check the response to see if operation was successful
        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            // Resolution successful, hooray
        } else {
            throw new RegisteringPidFailedException(
                    "Failed trying to " + "add URL to handle at the server.");
        }
    }

    @Override
    public void replaceUrlOfPidAtServer(String pid, int indexOfPidValue,
                                        String url)
            throws RegisteringPidFailedException {

        // Create the new value to be registered at the server. This will
        // replace the value on the server that has the same index.
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        HandleValue replacementValue
                = new HandleValue(indexOfPidValue,
                                  // unique index
                                  "URL".getBytes(DEFAULT_ENCODING),
                                  // handle value type
                                  url.getBytes(DEFAULT_ENCODING),
                                  // value data
                                  HandleValue.TTL_TYPE_RELATIVE,
                                  Common.DEFAULT_SESSION_TIMEOUT, timestamp,
                                  REFERENCES, ADMIN_READ, ADMIN_WRITE,
                                  PUBLIC_READ, PUBLIC_WRITE);

        // Create the request to send and the resolver to send it
        ModifyValueRequest request = new ModifyValueRequest(
                pid.getBytes(DEFAULT_ENCODING), replacementValue,
                pubKeyAuthInfo);
        HandleResolver resolver = new HandleResolver();
        AbstractResponse response;

        // Let the resolver process the request
        try {
            response = resolver.processRequest(request);
        } catch (HandleException e) {
            throw new RegisteringPidFailedException(
                    "Could not process the request to replace URL of handle at the server.",
                    e);
        }

        // Check the response to see if operation was successful
        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            // Resolution successful, hooray
        } else {
            throw new RegisteringPidFailedException(
                    "Failed trying to replace URL of handle at the server.");
        }
    }

    @Override
    public void addPidToServer(String pid, String url)
            throws RegisteringPidFailedException {

        // Define the admin record for the handle we want to create
        AdminRecord admin = new AdminRecord(ADMIN_ID.getBytes(DEFAULT_ENCODING),
                                            ADMIN_INDEX,
                                            AdminRecord.PRM_ADD_HANDLE,
                                            AdminRecord.PRM_DELETE_HANDLE,
                                            AdminRecord.PRM_ADD_NA,
                                            AdminRecord.PRM_DELETE_NA,
                                            AdminRecord.PRM_READ_VALUE,
                                            AdminRecord.PRM_MODIFY_VALUE,
                                            AdminRecord.PRM_REMOVE_VALUE,
                                            AdminRecord.PRM_ADD_VALUE,
                                            AdminRecord.PRM_MODIFY_ADMIN,
                                            AdminRecord.PRM_REMOVE_ADMIN,
                                            AdminRecord.PRM_ADD_ADMIN,
                                            AdminRecord.PRM_LIST_HANDLES);

        // Make a create-handle request.
        int timestamp = (int) (System.currentTimeMillis() / 1000);
        HandleValue values[] = {
                new HandleValue(ADMIN_RECORD_INDEX,       // unique index
                                "HS_ADMIN".getBytes(DEFAULT_ENCODING),
                                Encoder.encodeAdminRecord(admin),
                                HandleValue.TTL_TYPE_RELATIVE,
                                Common.DEFAULT_SESSION_TIMEOUT, timestamp,
                                REFERENCES, ADMIN_READ, ADMIN_WRITE,
                                PUBLIC_READ, PUBLIC_WRITE),

                new HandleValue(URL_RECORD_INDEX,         // unique index
                                "URL".getBytes(DEFAULT_ENCODING),
                                // handle value type
                                url.getBytes(DEFAULT_ENCODING),   // value data
                                HandleValue.TTL_TYPE_RELATIVE,
                                Common.DEFAULT_SESSION_TIMEOUT, timestamp,
                                REFERENCES, ADMIN_READ, ADMIN_WRITE,
                                PUBLIC_READ, PUBLIC_WRITE)};

        // Create the request to send and the resolver to send it
        CreateHandleRequest request = new CreateHandleRequest(
                pid.getBytes(DEFAULT_ENCODING), values, pubKeyAuthInfo);
        HandleResolver resolver = new HandleResolver();
        AbstractResponse response;

        // Let the resolver process the request
        try {
            response = resolver.processRequest(request);
        } catch (HandleException e) {
            throw new RegisteringPidFailedException(
                    "Could not process the request to create a new handle at the server.",
                    e);
        }

        // Check the response to see if operation was successful
        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            // Resolution successful, hooray
        } else {
            throw new RegisteringPidFailedException(
                    "Failed trying to create a new handle at the server.");
        }
    }
}
