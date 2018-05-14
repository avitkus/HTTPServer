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
public class GeneratedStringPage extends GeneratedPage implements IGeneratedPage {

    private static final Logger LOG = Logger.getLogger(GeneratedStringPage.class.getName());

    public GeneratedStringPage(Path pagePath, IStringPageGenerator generator) {
        super(pagePath, generator);
    }
    
    @Override
    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice {
        try {
            Optional<FileItem[]> args = ArgUtil.parse(request);
            Optional<Header[]> headers = Optional.of(request.getAllHeaders());
            IStringPageGenerator generator = (IStringPageGenerator)getGenerator();
            return new StringEntity(generator.getPageString(args, headers), generator.getContentType());
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

}
