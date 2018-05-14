package edu.unc.cs.httpserver.requestHandlers;

import edu.unc.cs.httpserver.pages.IPage;
import edu.unc.cs.httpserver.NHttpServer;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;

/**
 *
 * @author Andrew Vitkus
 */
public class PageHandler implements IPageHandler {

    private final Path root;
    private final Map<Path, IPage[]> pages;
    private final Map<Integer, IPage> errorPages;

    private static final Logger LOG = Logger.getLogger(NHttpServer.class.getName());

    public PageHandler(Path root, Map<Path, IPage[]> pages, Map<Integer, IPage> errorPages) {
        this.root = root;
        this.pages = pages;
        this.errorPages = errorPages;
    }

    @Override
    public void handle(final HttpRequest request, final HttpResponse response, final HttpContext context) throws HttpException, IOException {
        RequestLine requestLine = request.getRequestLine();
        String requestLineStr = "";
        if (requestLine instanceof BasicRequestLine) {
            BasicRequestLine brl = (BasicRequestLine) requestLine;
            requestLineStr = brl.toString();
        } else {
            requestLineStr = requestLine.getMethod() + " " + requestLine.getUri() + " " + requestLine.getProtocolVersion().getProtocol();
        }
        LOG.log(Level.FINEST, "Handling request: {0}", requestLine);

        HttpCoreContext coreContext = HttpCoreContext.adapt(context);

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET") && !method.equals("HEAD") && !method.equals("POST")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String target = request.getRequestLine().getUri();
        if (target.startsWith("/")) {
            target = target.substring(1);
        }
        target = target.split("\\?", 2)[0];
        if (target.equals("")) {
            target = "index.html";
        }
        Path requestedPath = root.resolve(URLDecoder.decode(target, "UTF-8"));
        if (!pages.containsKey(requestedPath)) {
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            String defaultMessage = "<html><body><h1>Page " + requestedPath + " not found</h1></body></html>";
            response.setEntity(getErrorPage(HttpStatus.SC_NOT_FOUND, defaultMessage, request));

            LOG.log(Level.FINEST, "Page {0} not found", requestedPath);
        } else {
            HttpConnection conn = coreContext.getConnection(HttpConnection.class);
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
                    response.setEntity(page.getResponse(request));

                    LOG.log(Level.FINEST, "{0}: Serving file {1}", new Object[]{conn, requestedPath});
                } catch (ResponseStatusNotice ex) {
                    response.setStatusCode(ex.getStatus());
                    response.setEntity(getErrorPage(ex.getStatus(), ex.getMessage(), request));

                    LOG.log(Level.FINEST, "{0}: Error #{1} on page {2}", new Object[]{conn, ex.getStatus(), requestedPath});
                }
            } else {
                response.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
                String defaultMessage = "<html><body><h1>Method \"" + method + "\" not supported for this page.</h1></body></html>";
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
