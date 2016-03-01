package edu.unc.cs.httpserver.ssl;

import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLDataPackage implements ISSLDataPackage {

    private final KeyManager[] keyManagers;
    private final TrustManager[] trustManagers;
    private final SecureRandom secureRandom;
    private final String protocol;

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, TrustManagerFactory trustManagerFactory, SecureRandom secureRandom, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        this(keystoreURL, keystorePassword, keystoreType, trustManagerFactory.getTrustManagers(), secureRandom, protocol);
    }

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, TrustManagerFactory trustManagerFactory, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        this(keystoreURL, keystorePassword, keystoreType, trustManagerFactory.getTrustManagers(), (SecureRandom) null, protocol);
    }

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, TrustManager[] trustManagers, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        this(keystoreURL, keystorePassword, keystoreType, trustManagers, (SecureRandom) null, protocol);
    }

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        this(keystoreURL, keystorePassword, keystoreType, (TrustManager[]) null, (SecureRandom) null, protocol);
    }

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, SecureRandom secureRandom, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        this(keystoreURL, keystorePassword, keystoreType, (TrustManager[]) null, secureRandom, protocol);
    }

    public SSLDataPackage(URL keystoreURL, String keystorePassword, String keystoreType, TrustManager[] trustManagers, SecureRandom secureRandom, String protocol) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, IOException {
        KeyStore keystore = KeyStore.getInstance(keystoreType);
        keystore.load(keystoreURL.openStream(), keystorePassword.toCharArray());
        KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmfactory.init(keystore, keystorePassword.toCharArray());

        this.keyManagers = kmfactory.getKeyManagers();
        this.trustManagers = trustManagers;
        this.secureRandom = secureRandom;
        this.protocol = protocol;
    }

    public SSLDataPackage(KeyManager[] keyManagers, TrustManagerFactory trustManagerFactory, SecureRandom secureRandom, String protocol) {
        this(keyManagers, trustManagerFactory.getTrustManagers(), secureRandom, protocol);
    }

    public SSLDataPackage(KeyManager[] keyManagers, TrustManagerFactory trustManagerFactory, String protocol) {
        this(keyManagers, trustManagerFactory.getTrustManagers(), (SecureRandom) null, protocol);
    }

    public SSLDataPackage(KeyManager[] keyManagers, TrustManager[] trustManagers, String protocol) {
        this(keyManagers, trustManagers, (SecureRandom) null, protocol);
    }

    public SSLDataPackage(KeyManager[] keyManagers, String protocol) {
        this(keyManagers, (TrustManager[]) null, (SecureRandom) null, protocol);
    }

    public SSLDataPackage(KeyManager[] keyManagers, SecureRandom secureRandom, String protocol) {
        this(keyManagers, (TrustManager[]) null, secureRandom, protocol);
    }

    public SSLDataPackage(KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom, String protocol) {
        this.keyManagers = keyManagers;
        this.trustManagers = trustManagers;
        this.secureRandom = secureRandom;
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public KeyManager[] getKeyManagers() {
        return keyManagers;
    }

    @Override
    public TrustManager[] getTrustManagers() {
        return trustManagers;
    }

    @Override
    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

}
