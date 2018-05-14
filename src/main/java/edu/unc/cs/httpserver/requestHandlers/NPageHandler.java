package edu.unc.cs.httpserver.requestHandlers;

import edu.unc.cs.httpserver.pages.IPage;
import edu.unc.cs.httpserver.NHttpServer;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncRequestConsumer;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.http.nio.protocol.HttpAsyncRequestConsumer;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

/**
 *
 * @author Andrew Vitkus
 */
public class NPageHandler implements INPageHandler {

    private final Path root;
    private final Map<Path, IPage[]> pages;
    private final Map<Integer, IPage> errorPages;

    private static final Logger LOG = Logger.getLogger(NHttpServer.class.getName());

    public NPageHandler(Path root, Map<Path, IPage[]> pages, Map<Integer, IPage> errorPages) {
        this.root = root;
        this.pages = pages;
        this.errorPages = errorPages;
    }

    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(final HttpRequest request, final HttpContext context) {
        // Buffer request content in memory for simplicity
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(final HttpRequest request, final HttpAsyncExchange httpexchange, final HttpContext context) throws HttpException, IOException {
        HttpResponse response = httpexchange.getResponse();
        handleInternal(request, response, context);
        httpexchange.submitResponse(new BasicAsyncResponseProducer(response));
    }

    private void handleInternal(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        RequestLine requestLine = request.getRequestLine();
        String requestLineStr = "";
        if (requestLine instanceof BasicRequestLine) {
            BasicRequestLine brl = (BasicRequestLine) requestLine;
            requestLineStr = brl.toString();
            requestLineStr = requestLine.getMethod() + " " + requestLine.getUri() + " " + requestLine.getProtocolVersion().getProtocol();
        }
        LOG.log(Level.FINEST, "Handling request: {0}", requestLine);

        HttpCoreContext coreContext = HttpCoreContext.adapt(context);

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = request.getRequestLine().getUri();
        System.out.println("Target: '" + target + "'");
        target = target.split("\\?", 2)[0];
        System.out.println("Target: '" + target + "'");
        if (target.equals("")) {
            target = "index.html";
        }
        System.out.println("Target: '" + target + "'");
        Path requestedPath = root.resolve(URLDecoder.decode(target, "UTF-8"));
        if (requestedPath.startsWith(root)) {
            requestedPath = root.relativize(requestedPath);
        }
        System.out.println("Target: '" + requestedPath.toString() + "'");
        if (!pages.containsKey(requestedPath)) {

            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            String defaultMessage = "<html><body><h1>Page " + target + " not found</h1></body></html>";
            response.setEntity(getErrorPage(HttpStatus.SC_NOT_FOUND, defaultMessage, request));

            LOG.log(Level.FINEST, "Page {0} not found", requestedPath);

        } else {
            NHttpConnection conn = coreContext.getConnection(NHttpConnection.class);
            IPage[] pageChoices = pages.get(requestedPath);
            IPage page = null;
            for(IPage choice : pageChoices) {
                if (isAllowedMethod(choice, method)) {
                    page = choice;
                    break;
                }
            }
            if (page != null) {
                try {
                    response.setStatusCode(HttpStatus.SC_OK);
                    if (!method.equals("HEAD")) {
                        response.setEntity(page.getResponse(request));
                    }
                    
                    LOG.log(Level.FINEST, "{0}: Serving file {1}", new Object[]{conn, requestedPath});
                } catch (ResponseStatusNotice ex) {
                    response.setStatusCode(ex.getStatus());
                    response.setEntity(getErrorPage(ex.getStatus(), ex.getMessage(), request));

                    LOG.log(Level.FINEST, "{0}: Error #{1} on page {2}", new Object[]{conn, ex.getStatus(), requestedPath});
                }
            } else {
                response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                String defaultMessage = "<html><head><title>Error 405</title></head><body><h1>Error 405: Method not allowed</h1><hr>Method \"" + method + "\" not supported for this page.</body></html>";
                response.setEntity(getErrorPage(HttpStatus.SC_METHOD_NOT_ALLOWED, defaultMessage, request));

                LOG.log(Level.FINEST, "{0}: Illegal method requested for file {1}", new Object[]{conn, requestedPath});
            }
        }
    }

    private HttpEntity getErrorPage(int error, String defaultMessage, HttpRequest request) {
        if (errorPages.containsKey(error)) {
            try {
                return errorPages.get(error).getResponse(request);
            } catch (ResponseStatusNotice ex) { }
        }
        if (defaultMessage == null || defaultMessage.isEmpty()) {
            defaultMessage = "<html><head><title>Error " + error + "</title></head><body><h1>Error " + error + "</h1></body></html>";
        }
        return new NStringEntity(defaultMessage, ContentType.create("text/html", "UTF-8"));
    }

    private static boolean isAllowedMethod(IPage page, String currentMethod) {
        String[] validMethods = page.getValidMethods();
        for (String method : validMethods) {
            if (method.equals(currentMethod)) {
                return true;
            }
        }
        return false;
    }
}
