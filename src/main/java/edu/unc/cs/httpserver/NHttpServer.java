package edu.unc.cs.httpserver;

import edu.unc.cs.httpserver.ssl.ISSLDataPackage;
import edu.unc.cs.httpserver.ssl.SSLSetup;
import edu.unc.cs.httpserver.requestHandlers.NPageHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpServerIODispatch;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.impl.nio.DefaultNHttpServerConnectionFactory;
import org.apache.http.impl.nio.SSLNHttpServerConnectionFactory;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpConnectionFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.nio.protocol.HttpAsyncService;
import org.apache.http.nio.protocol.UriHttpAsyncRequestHandlerMapper;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

/**
 *
 * @author Andrew Vitkus
 */
public class NHttpServer extends AbstractHttpServer {

    private static final Logger LOG = Logger.getLogger(NHttpServer.class.getName());

    NHttpServer(Path root, int port) {
        this(root, port, null);
    }

    NHttpServer(Path root, int port, ISSLDataPackage SSLData) {
        super(root, port, SSLData);
    }

    @Override
    public void start() throws NoSuchAlgorithmException, KeyManagementException, IOReactorException {
        HttpProcessor httpProc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("JavaServer/0.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();
        // Create request handler registry
        UriHttpAsyncRequestHandlerMapper registry = new UriHttpAsyncRequestHandlerMapper();
        // Register the default handler for all URIs
        registry.register("*", new NPageHandler(getRoot(), getPages(), getErrorPages()));
        // Create server-side HTTP protocol handler
        HttpAsyncService protocolHandler = new HttpAsyncService(httpProc, registry) {

            @Override
            public void connected(final NHttpServerConnection conn) {
                LOG.log(Level.INFO, "{0}: connection open", conn);
                super.connected(conn);
            }

            @Override
            public void closed(final NHttpServerConnection conn) {
                LOG.log(Level.INFO, "{0}: connection closed", conn);
                super.closed(conn);
            }

        };
        // Create HTTP connection factory
        NHttpConnectionFactory<DefaultNHttpServerConnection> connFactory;
        if (!isHttp()) {
            ISSLDataPackage SSLData = getSSLData();
            KeyManager[] keyManagers = SSLData.getKeyManagers();
            TrustManager[] trustManagers = SSLData.getTrustManagers();
            SecureRandom secureRandom = SSLData.getSecureRandom();
            String protocol = SSLData.getProtocol();
            SSLContext sslcontext = SSLContext.getInstance(protocol);
            sslcontext.init(keyManagers, trustManagers, secureRandom);
            connFactory = new SSLNHttpServerConnectionFactory(sslcontext, new SSLSetup(), ConnectionConfig.DEFAULT);
        } else {
            connFactory = new DefaultNHttpServerConnectionFactory(ConnectionConfig.DEFAULT);
        }
        // Create server-side I/O event dispatch
        IOEventDispatch ioEventDispatch = new DefaultHttpServerIODispatch(protocolHandler, connFactory);
        // Set I/O reactor defaults
        IOReactorConfig config = IOReactorConfig.custom()
                .setIoThreadCount(1)
                .setSoTimeout(60000)
                .setConnectTimeout(60000)
                .build();
        // Create server-side I/O reactor
        ListeningIOReactor ioReactor = new DefaultListeningIOReactor(config);
        try {
            // Listen of the given port
            ioReactor.listen(new InetSocketAddress(getPort()));
            // Ready to go!
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        LOG.log(Level.INFO, "Shutdown");
    }

    @Override
    public boolean isBlocking() {
        return false;
    }
}
