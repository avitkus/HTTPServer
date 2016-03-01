package edu.unc.cs.httpserver.util;

import java.util.Map;
import org.apache.http.entity.ContentType;
import org.apache.tika.mime.MediaType;

/**
 *
 * @author Andrew Vitkus
 */
public class ContentTypeParser {

    public static ContentType fromMediaType(MediaType mediaType) {
        String type = mediaType.getType();
        String subtype = mediaType.getSubtype();
        String mime = type + "/" + subtype;
        ContentType contentType = null;
        if (mediaType.hasParameters()) {
            Map<String, String> parameters = mediaType.getParameters();
            String charset = parameters.get("charset");
            if (charset != null) {
                contentType = ContentType.create(mime, charset);
            }
        }
        if (contentType != null) {
            contentType = ContentType.create(mime);
        }
        return contentType;
    }
}
