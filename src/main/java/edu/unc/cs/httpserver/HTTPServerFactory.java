package edu.unc.cs.httpserver;

import edu.unc.cs.httpserver.ssl.ISSLDataPackage;
import java.nio.file.Path;

/**
 *
 * @author Andrew Vitkus
 */
public class HTTPServerFactory {

    public static IHttpServer getNHTTPServer(Path root, int port) {
        return new NHttpServer(root, port);
    }

    public static IHttpServer getHTTPServer(Path root, int port) {
        return new HttpServer(root, port);
    }

    public static IHttpServer getNHTTPSServer(Path root, int port, ISSLDataPackage SSLData) {
        return new NHttpServer(root, port, SSLData);
    }

    public static IHttpServer getHTTPSServer(Path root, int port, ISSLDataPackage SSLData) {
        return new HttpServer(root, port, SSLData);
    }
}
