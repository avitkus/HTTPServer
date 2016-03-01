package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.util.Optional;
import org.apache.commons.fileupload.FileItem;
import org.apache.http.entity.ContentType;

/**
 *
 * @author Andrew Vitkus
 */
public interface IPageGenerator {

    public String getPage(Optional<FileItem[]> request) throws ResponseStatusNotice;

    public String[] getValidMethods();

    public default ContentType getContentType() {
        return ContentType.TEXT_HTML;
    }
}
