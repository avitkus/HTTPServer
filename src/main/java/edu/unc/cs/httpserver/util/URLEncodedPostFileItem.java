package edu.unc.cs.httpserver.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

/**
 *
 * @author Andrew Vitkus
 */
public class URLEncodedPostFileItem implements FileItem {

    private final String name;
    private final String value;
    private final String encoding;

    public URLEncodedPostFileItem(String name, String value, String encoding) {
        this.name = name;
        this.value = value;
        this.encoding = encoding;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new StringBufferInputStream(value);
    }

    @Override
    public String getContentType() {
        return encoding;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isInMemory() {
        return true;
    }

    @Override
    public long getSize() {
        return value.length();
    }

    @Override
    public byte[] get() {
        return value.getBytes();
    }

    @Override
    public String getString(String encoding) throws UnsupportedEncodingException {
        return "";
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public void write(File file) throws Exception {
    }

    @Override
    public void delete() {
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public void setFieldName(String name) {
    }

    @Override
    public boolean isFormField() {
        return true;
    }

    @Override
    public void setFormField(boolean state) {
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new NullOutputStream();
    }

    @Override
    public FileItemHeaders getHeaders() {
        return new FileItemHeaders() {

            @Override
            public String getHeader(String name) {
                return "";
            }

            @Override
            public Iterator<String> getHeaders(String name) {
                return new EmptyIterator();
            }

            @Override
            public Iterator<String> getHeaderNames() {
                return new EmptyIterator();
            }

            class EmptyIterator implements Iterator<String> {

                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public String next() {
                    return "";
                }
            }
        };
    }

    @Override
    public void setHeaders(FileItemHeaders headers) {
    }

    class NullOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
        }

    }
}
