package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.nio.file.Path;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;

/**
 *
 * @author Andrew Vitkus
 */
public interface IPage {

    public Path getPagePath();

    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice;

    public String[] getValidMethods();
}
