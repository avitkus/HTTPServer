package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author Andrew Vitkus
 */
public interface IStringPageGenerator extends IPageGenerator {

    public String getPageString(Optional<FileItem[]> request) throws ResponseStatusNotice;
    
    default public String getPageString(Optional<FileItem[]> request, Optional<Header[]> headers) throws ResponseStatusNotice {
        return getPageString(request);
    }
    
    
    @Override
    default public HttpEntity getPage(Optional<FileItem[]> request) throws ResponseStatusNotice {
        return new StringEntity(getPageString(request), getContentType());
    }

    @Override
    default public HttpEntity getPage(Optional<FileItem[]> request, Optional<Header[]> headers) throws ResponseStatusNotice {
        return new StringEntity(getPageString(request, headers), getContentType());
    }
}
