package edu.unc.cs.httpserver.pages.nio;

import edu.unc.cs.httpserver.pages.*;
import edu.unc.cs.httpserver.util.ContentTypeParser;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NFileEntity;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.Detector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 *
 * @author Andrew Vitkus
 */
public class NFilePage extends AbstractPage implements IFilePage {

    private final Path filePath;
    private final ContentType type;

    public NFilePage(Path pagePath, Path filePath, String type) {
        this(pagePath, filePath, ContentType.create(type));
    }

    public NFilePage(Path pagePath, Path filePath, String type, Charset charset) {
        this(pagePath, filePath, ContentType.create(type, charset));
    }

    public NFilePage(Path pagePath, Path filePath, ContentType type) {
        super(pagePath);
        this.filePath = filePath;
        this.type = type;
    }

    public NFilePage(Path pagePath, Path filePath) throws IOException {
        super(pagePath);
        this.filePath = filePath;

        TikaConfig config = TikaConfig.getDefaultConfig();
        Detector detector = config.getDetector();

        TikaInputStream stream = TikaInputStream.get(filePath.toUri());

        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, filePath.getFileName().toString());
        MediaType mediaType = detector.detect(stream, metadata);
        type = ContentTypeParser.fromMediaType(mediaType);
    }

    @Override
    public Path getFilePath() {
        return filePath;
    }

    @Override
    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice {
        return new NFileEntity(filePath.toFile(), type);
    }

    @Override
    public String[] getValidMethods() {
        return new String[]{"GET", "HEAD"};
    }
}
