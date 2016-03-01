package edu.unc.cs.httpserver.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.StringBufferInputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.UploadContext;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.NameValuePair;
import org.apache.http.RequestLine;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineFormatter;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.message.BufferedHeader;

/**
 *
 * @author Andrew Vitkus
 */
public class HttpRequestUploadContext implements UploadContext {

    private static final Logger LOG = Logger.getLogger(HttpRequestUploadContext.class.getName());

    private String encoding;
    private long length;
    private final String defaultEncoding;
    private final HttpEntityEnclosingRequest request;
    private final HttpEntity entity;

    public HttpRequestUploadContext(HttpEntityEnclosingRequest request, String defaultEncoding) {
        this.request = request;
        this.entity = request.getEntity();
        this.defaultEncoding = defaultEncoding;
        encoding = null;
        length = -1;
    }

    @Override
    public String getCharacterEncoding() {
        if (encoding == null) {
            for (Header header : request.getHeaders("Content-Type")) {
                for (HeaderElement element : header.getElements()) {
                    if (element.getName().equals("charset")) {
                        encoding = element.getValue();
                    }
                    for (NameValuePair nvp : element.getParameters()) {
                        if (nvp.getName().equals("charset")) {
                            encoding = nvp.getValue();
                        }
                    }
                }
            }
            if (encoding == null) {
                encoding = defaultEncoding;
            }
        }
        System.out.println(encoding);
        return encoding;
    }

    @Override
    public String getContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public int getContentLength() {
        return (int) contentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Vector<InputStream> streams = new Vector(3 + request.getAllHeaders().length);
        RequestLine requestLine = request.getRequestLine();
        if (requestLine instanceof BasicRequestLine) {
            BasicRequestLine brl = (BasicRequestLine) requestLine;
            streams.add(new StringBufferInputStream(brl.toString()));
        } else {
            streams.add(new StringBufferInputStream(requestLine.getMethod() + " " + requestLine.getUri() + " " + requestLine.getProtocolVersion().getProtocol()));
        }
        for (Header header : request.getAllHeaders()) {
            if (header instanceof BasicHeader) {
                BasicHeader headerC = (BasicHeader) header;
                streams.add(new StringBufferInputStream("\r\n" + headerC.toString()));
            } else if (header instanceof BufferedHeader) {
                BufferedHeader headerC = (BufferedHeader) header;
                streams.add(new StringBufferInputStream("\r\n" + headerC.toString()));
            } else {
                streams.add(new StringBufferInputStream("\r\n" + BasicLineFormatter.INSTANCE.formatHeader(null, header).toString()));
            }
        }
        streams.add(new StringBufferInputStream("\r\n\r\n"));
        streams.add(entity.getContent());
        SequenceInputStream requestStream = new SequenceInputStream(streams.elements());
        return requestStream;
    }

    @Override
    public long contentLength() {
        if (length == -1) {
            for (Header header : request.getHeaders("Content-Length")) {
                try {
                    length = Long.parseLong(header.getValue());
                } catch (NumberFormatException ex) {
                    LOG.log(Level.INFO, null, ex);
                }
            }
        }
        return length;
    }
}
