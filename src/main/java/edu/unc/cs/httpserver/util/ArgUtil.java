package edu.unc.cs.httpserver.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Andrew Vitkus
 */
public class ArgUtil {

    public static final int KB = 1024;
    public static final int MB = KB * 1024;
    public static final int GB = MB * 1024;
    private static final int DEFAULT_MEMORY_SIZE_LIMIT = 5 * KB; // 5 KB
    private static final String HTTP_DEFAULT_CHARSET = "ISO-8859-1";
    private static final HashMap<Integer, ServletFileUpload> uploadMap;
    private static final Logger LOG = Logger.getLogger(ArgUtil.class.getName());

    static {
        uploadMap = new HashMap<>(1);
    }

    public static Optional<FileItem[]> parse(HttpRequest request) throws FileUploadException, ParseException, IOException {
        return parse(request, DEFAULT_MEMORY_SIZE_LIMIT);
    }

    public static Optional<FileItem[]> parse(HttpRequest request, int sizeLimit) throws FileUploadException, ParseException, IOException {
        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        Optional<FileItem[]> args;
        switch (method) {
            case "POST":
                args = parsePost(request, sizeLimit);
                break;
            case "GET":
                args = parseGet(request);
                break;
            default:
                args = Optional.empty();
        }
        return args;
    }

    private static Optional<FileItem[]> parseGet(HttpRequest request) throws FileUploadException, ParseException, IOException {
        FileItem[] items = null;

        String uri = request.getRequestLine().getUri();
        System.out.println(uri);
        String[] uriParts = uri.split("\\?", 2);
        if (uriParts.length > 1) {
            System.out.println(uriParts[1]);
            items = parseUrlFormEncoded(uriParts[1]);
        }
        if (items == null || items.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(items);
        }
    }

    private static Optional<FileItem[]> parsePost(HttpRequest request, int sizeLimit) throws FileUploadException, ParseException, IOException {
        FileItem[] items = null;
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest heeRequest = (HttpEntityEnclosingRequest) request;
            if (isMultipart(heeRequest)) {
                items = parseMultipart(heeRequest, sizeLimit);
            } else {
                HttpEntity entity = heeRequest.getEntity();
                String postData = EntityUtils.toString(entity);
                items = parseUrlFormEncoded(postData);
            }
        }
        if (items == null || items.length == 0) {
            return Optional.empty();
        } else {
            return Optional.of(items);
        }
    }

    public static boolean isMultipart(HttpRequest request) {
        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest heeRequest = (HttpEntityEnclosingRequest) request;
            return isMultipart(heeRequest);
        }
        return false;
    }

    public static boolean isMultipart(HttpEntityEnclosingRequest request) {
        for (Header header : request.getHeaders("Content-Type")) {
            for (HeaderElement element : header.getElements()) {
                if (element.getName().matches("multipart/.*")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Wraps a HttpEntityEnclosingRequest in a RequestContext assuming the
     * default HTTP charset
     *
     * @param request the HttpEntityEnclosingRequest to be wrapped
     * @return the RequestContext wrapping the HttpEntityEnclosingRequest
     */
    private static UploadContext httpEntityEnclosingRequestToRequestContext(final HttpEntityEnclosingRequest request) {
        return httpEntityEnclosingRequestToRequestContext(request, HTTP_DEFAULT_CHARSET);
    }

    /**
     * Wraps a HttpEntityEnclosingRequest in a RequestContext
     *
     * @param defaultEncoding the default encoding of the request
     * @param entity the HttpEntityEnclosingRequest to to be wrapped
     * @return the RequestContext wrapping the HttpEntityEnclosingRequest
     */
    private static UploadContext httpEntityEnclosingRequestToRequestContext(final HttpEntityEnclosingRequest request, final String defaultEncoding) {
        return new HttpRequestUploadContext(request, defaultEncoding);
    }

    private static FileItem[] parseMultipart(HttpEntityEnclosingRequest request, int sizeLimit) throws FileUploadException {
        ServletFileUpload upload = uploadMap.get(sizeLimit); // get the upload hander from the map

        if (upload == null) { // does the ServerletFileUpload for the requested size limit exist?
            // Create a factory for disk-based file items
            DiskFileItemFactory factory = new DiskFileItemFactory();

            // Set factory constraints
            factory.setSizeThreshold(sizeLimit);

            // Create a new file upload handler
            upload = new ServletFileUpload(factory);

            // Set overall request size constraint
            //upload.setSizeMax(yourMaxRequestSize);
            uploadMap.put(sizeLimit, upload); // save the upload hander to the map
        }

        // Parse the request
        List<FileItem> items = upload.parseRequest(httpEntityEnclosingRequestToRequestContext(request));

        return items.toArray(new FileItem[items.size()]);
    }

    private static FileItem[] parseUrlFormEncoded(String args) throws ParseException, IOException {
        ArrayList<FileItem> items = new ArrayList<>();
        String[] dataElements = args.split("&");
        for (String dataElement : dataElements) {
            String[] elementNVP = dataElement.split("=");
            if (elementNVP.length == 2) {
                String name = formatUrlFormEncString(elementNVP[0]);
                String value = formatUrlFormEncString(elementNVP[1]);
                items.add(new URLEncodedPostFileItem(name, value, "ASCII"));
            }
        }
        return items.toArray(new FileItem[items.size()]);
    }

    private static String formatUrlFormEncString(String raw) {
        StringBuilder formatted = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            switch (c) {
                case '+':
                    formatted.append(" ");
                    break;
                case '%':
                    String hex = raw.substring(i + 1, i + 3);
                    byte b = Byte.parseByte(hex, 16);
                    formatted.append(Character.toChars(b));
                    i += 2;
                    break;
                default:
                    formatted.append(c);
            }
        }
        return formatted.toString();
    }
}
