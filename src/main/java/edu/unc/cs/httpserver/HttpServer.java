package edu.unc.cs.httpserver;

import edu.unc.cs.httpserver.ssl.ISSLDataPackage;
import edu.unc.cs.httpserver.requestHandlers.PageHandler;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpConnectionFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.DefaultBHttpServerConnectionFactory;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

/**
 *
 * @author Andrew Vitkus
 */
public class HttpServer extends AbstractHttpServer {

    private static final Logger LOG = Logger.getLogger(HttpServer.class.getName());
    
    private volatile boolean doClose = false;

    HttpServer(Path root, int port) {
        this(root, port, null);
    }

    HttpServer(Path root, int port, ISSLDataPackage SSLData) {
        super(root, port, SSLData);
    }

    @Override
    public void start() throws NoSuchAlgorithmException, KeyManagementException, IOReactorException, IOException {
        // Set up the HTTP protocol processor
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                .add(new ResponseDate())
                .add(new ResponseServer("JavaServer/0.1"))
                .add(new ResponseContent())
                .add(new ResponseConnControl()).build();

        // Set up request handlers
        UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
        reqistry.register("*", new PageHandler(getRoot(), getPages(), getErrorPages()));

        // Set up the HTTP service
        HttpService httpService = new HttpService(httpproc, reqistry);

        ServerSocketFactory serverSocketFactory;
        if (!isHttp()) {
            ISSLDataPackage SSLData = getSSLData();
            KeyManager[] keyManagers = SSLData.getKeyManagers();
            TrustManager[] trustManagers = SSLData.getTrustManagers();
            SecureRandom secureRandom = SSLData.getSecureRandom();
            String protocol = SSLData.getProtocol();
            SSLContext sslcontext = SSLContext.getInstance(protocol);
            sslcontext.init(keyManagers, trustManagers, secureRandom);
            serverSocketFactory = sslcontext.getServerSocketFactory();
        } else {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }

        ServerSocket serverSocket = serverSocketFactory.createServerSocket(getPort());

        Thread t = new RequestListenerThread(httpService, serverSocket);
        t.setDaemon(false);
        t.start();
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    private static class ConnectionHandlerThread extends Thread {

        private final HttpService httpService;
        private final HttpServerConnection serverConnection;

        public ConnectionHandlerThread(final HttpService httpservice, final HttpServerConnection serverConnection) {
            super();
            this.httpService = httpservice;
            this.serverConnection = serverConnection;
        }

        @Override
        public void run() {
            LOG.log(Level.FINEST, "New connection thread started");
            HttpContext context = new BasicHttpContext();
            try {
                while (!Thread.interrupted() && serverConnection.isOpen()) {
                    httpService.handleRequest(serverConnection, context);
                }
            } catch (ConnectionClosedException ex) {
                LOG.log(Level.FINEST, null, "Client closed connection");
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (HttpException ex) {
                LOG.log(Level.WARNING, null, ex);
            } finally {
                if(serverConnection != null) {
                    try {
                        serverConnection.shutdown();
                    } catch (IOException ex) {
                        LOG.log(Level.WARNING, null, ex);
                    }
                }
            }
        }
    }

    private class RequestListenerThread extends Thread {

        private final HttpConnectionFactory<DefaultBHttpServerConnection> connectionFactory;
        private final ServerSocket serverSocket;
        private final HttpService httpService;

        public RequestListenerThread(HttpService httpService, ServerSocket serverSocket) throws IOException {
            connectionFactory = DefaultBHttpServerConnectionFactory.INSTANCE;
            this.serverSocket = serverSocket;
            this.httpService = httpService;
        }

        @Override
        public void run() {
            if (isHttp()) {
                LOG.log(Level.CONFIG, "Listening for HTTP traffic on port {0}", serverSocket.getLocalPort());
            } else {
                LOG.log(Level.CONFIG, "Listening for HTTPS traffic on port {0}", serverSocket.getLocalPort());
            }
            while (!Thread.interrupted() && !doClose) {
                Socket socket = null;
                try {
                    // Set up HTTP connection
                    socket = serverSocket.accept();
                    LOG.log(Level.FINEST, "Incoming connection from {0}", socket.getInetAddress());
                    HttpServerConnection conn = connectionFactory.createConnection(socket);
                    // Start worker thread
                    Thread t = new ConnectionHandlerThread(httpService, conn);
                    t.setDaemon(true);
                    t.start();
                } catch (InterruptedIOException ex) {
                    LOG.log(Level.WARNING, null, ex);
                } catch (IOException e) {
                    LOG.log(Level.WARNING, null, e);
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex) {
                            LOG.log(Level.WARNING, null, ex);
                        }
                    }
                }
            }
            try {
                serverSocket.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void close() throws Exception {
        doClose = true;
    }
}
