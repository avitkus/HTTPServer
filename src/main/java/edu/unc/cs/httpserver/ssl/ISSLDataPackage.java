package edu.unc.cs.httpserver.ssl;

import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

/**
 *
 * @author Andrew Vitkus
 */
public interface ISSLDataPackage {

    public KeyManager[] getKeyManagers();

    public TrustManager[] getTrustManagers();

    public SecureRandom getSecureRandom();

    public String getProtocol();
}
