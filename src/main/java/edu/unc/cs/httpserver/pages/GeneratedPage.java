package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ArgUtil;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author Andrew Vitkus
 */
public class GeneratedPage extends AbstractPage implements IGeneratedPage {

    private static final Logger LOG = Logger.getLogger(GeneratedPage.class.getName());

    private final IPageGenerator generator;

    public GeneratedPage(Path pagePath, IPageGenerator generator) {
        super(pagePath);
        this.generator = generator;
    }

    @Override
    public IPageGenerator getGenerator() {
        return generator;
    }

    @Override
    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice {
        try {
            Optional<FileItem[]> args = ArgUtil.parse(request);
            Optional<Header[]> headers = Optional.of(request.getAllHeaders());
            return generator.getPage(args, headers);
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (FileUploadException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (ParseException ex) {
            LOG.log(Level.WARNING, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        return null;
    }

    @Override
    public String[] getValidMethods() {
        return generator.getValidMethods();
    }
}
