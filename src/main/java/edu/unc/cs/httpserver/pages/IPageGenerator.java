package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

/**
 *
 * @author Andrew Vitkus
 */
public interface IPageGenerator {

    public HttpEntity getPage(Optional<FileItem[]> request) throws ResponseStatusNotice;
    
    default public HttpEntity getPage(Optional<FileItem[]> request, Optional<Header[]> headers) throws ResponseStatusNotice {
        return getPage(request);
    }
    
    public default Optional<Header[]> getHeaders() {
        return Optional.empty();
    }
    
    public default Optional<Integer> getStatus() {
        return Optional.empty();
    }

    public String[] getValidMethods();

    public default ContentType getContentType() {
        return ContentType.TEXT_HTML;
    }
}
