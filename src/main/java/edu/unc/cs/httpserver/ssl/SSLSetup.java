package edu.unc.cs.httpserver.ssl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLSetupHandler;

/**
 *
 * @author Andrew Vitkus
 */
public class SSLSetup implements SSLSetupHandler {

    private static final Set<String> CIPHERS = Collections.synchronizedSet(new HashSet<String>(7));
    private static final Set<String> PROTOCOLS = Collections.synchronizedSet(new HashSet<String>(4));

    static {
        CIPHERS.addAll(Arrays.asList(new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_RSA_WITH_AES_128_CBC_SHA256",
            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_ECDH_ECDSA_WITH_RC4_128_SHA",
            "TLS_ECDH_RSA_WITH_RC4_128_SHA"}));

        PROTOCOLS.addAll(Arrays.asList(new String[]{"SSLv3",
            "TLSv1",
            "TLSv1.1",
            "TLSv1.2"}));
    }

    public static boolean addCipher(String cipher) {
        return CIPHERS.add(cipher);
    }

    public static boolean addCiphers(Collection<String> ciphers) {
        return CIPHERS.addAll(ciphers);
    }

    public static boolean addProtocol(String protocol) {
        return PROTOCOLS.add(protocol);
    }

    public static boolean addProtocols(Collection<String> protocols) {
        return PROTOCOLS.addAll(protocols);
    }

    public static boolean removeCipher(String cipher) {
        return CIPHERS.remove(cipher);
    }

    public static boolean removeCiphers(Collection<String> ciphers) {
        return CIPHERS.removeAll(ciphers);
    }

    public static boolean removeProtocol(String protocol) {
        return PROTOCOLS.remove(protocol);
    }

    public static boolean removeProtocols(Collection<String> protocols) {
        return PROTOCOLS.removeAll(protocols);
    }

    @Override
    public void initalize(SSLEngine sslengine) throws SSLException {
        sslengine.setEnabledCipherSuites((String[]) CIPHERS.toArray(new String[CIPHERS.size()]));
        sslengine.setEnabledProtocols((String[]) PROTOCOLS.toArray(new String[PROTOCOLS.size()]));
    }

    @Override
    public void verify(IOSession ioSession, SSLSession sslSession) throws SSLException {
        if (!CIPHERS.contains(sslSession.getCipherSuite())) {
            throw new SSLException("Illegal cipher '" + sslSession.getCipherSuite() + "'");
        }

        if (!PROTOCOLS.contains(sslSession.getProtocol())) {
            throw new SSLException("Illegal protocol '" + sslSession.getProtocol() + "'");

        }
    }
}
