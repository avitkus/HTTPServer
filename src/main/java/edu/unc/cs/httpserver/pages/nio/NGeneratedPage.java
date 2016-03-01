package edu.unc.cs.httpserver.pages.nio;

import edu.unc.cs.httpserver.pages.*;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.ParseException;
import org.apache.http.nio.entity.NStringEntity;

/**
 *
 * @author Andrew Vitkus
 */
public class NGeneratedPage extends AbstractPage implements IGeneratedPage {

    private final static Logger LOG = Logger.getLogger(NGeneratedPage.class.getName());
    private final IPageGenerator generator;

    public NGeneratedPage(Path pagePath, IPageGenerator generator) {
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
            return new NStringEntity(generator.getPage(args), generator.getContentType());
        } catch (UnsupportedEncodingException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (FileUploadException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } 
        return null;
    }

    @Override
    public String[] getValidMethods() {
        return generator.getValidMethods();
    }
}
