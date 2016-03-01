package edu.unc.cs.httpserver.pages;

import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;

/**
 *
 * @author Andrew Vitkus
 */
public class FilePage extends AbstractPage implements IFilePage {

    private final Path filePath;
    private final ContentType type;

    public FilePage(Path pagePath, Path filePath, String type) {
        this(pagePath, filePath, ContentType.create(type));
    }

    public FilePage(Path pagePath, Path filePath, String type, Charset charset) {
        this(pagePath, filePath, ContentType.create(type, charset));
    }

    public FilePage(Path pagePath, Path filePath, ContentType type) {
        super(pagePath);
        this.filePath = filePath;
        this.type = type;
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice {
        return new FileEntity(filePath.toFile(), type);
    }

    @Override
    public String[] getValidMethods() {
        return new String[]{"GET", "HEAD"};
    }
}
