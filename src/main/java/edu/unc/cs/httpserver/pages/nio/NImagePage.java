package edu.unc.cs.httpserver.pages.nio;

import edu.unc.cs.httpserver.pages.*;
import edu.unc.cs.httpserver.util.ChecksumUtil;
import edu.unc.cs.httpserver.util.ResponseStatusNotice;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NByteArrayEntity;

/**
 *
 * @author Andrew Vitkus
 */
public class NImagePage extends AbstractPage implements IImagePage {

    private static final Logger LOG = Logger.getLogger(NImagePage.class.getName());

    private final Path imagePath;
    private BufferedImage image;
    private byte[] sha1;
    private byte[] imageBytes;
    private String extension;
    private final ContentType dispType;
    private final Object lock = new Object();

    public NImagePage(Path pagePath, BufferedImage image) throws IOException {
        super(pagePath);
        this.imagePath = null;
        updateExtension();
        synchronized (lock) {
            this.image = image;
            updateImage();
        }
        dispType = ContentType.parse(URLConnection.guessContentTypeFromName(getPagePath().getFileName().toString()));
    }

    public NImagePage(Path pagePath, Path imagePath) throws IOException {
        super(pagePath);
        this.imagePath = imagePath;
        updateExtension();
        synchronized (lock) {
            updateImage();
        }
        dispType = ContentType.parse(URLConnection.guessContentTypeFromName(getPagePath().getFileName().toString()));
    }

    @Override
    public Image getImage() {
        synchronized (lock) {
            try {
                if (doUpdateImage()) {
                    updateImage();
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            return image;
        }
    }

    @Override
    public HttpEntity getResponse(HttpRequest request) throws ResponseStatusNotice {
        synchronized (lock) {
            try {
                if (doUpdateImage()) {
                    updateImage();
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            return new NByteArrayEntity(imageBytes, dispType);
        }
    }

    @Override
    public boolean isFileBacked() {
        return imagePath != null;
    }

    private void updateExtension() {
        String pageName = getPagePath().getFileName().toString();
        int extensionStart = pageName.lastIndexOf('.') + 1;
        if (extensionStart > 1 && extensionStart < pageName.length() - 1) {
            extension = pageName.substring(extensionStart);
        }
    }

    private boolean doUpdateImage() throws IOException {
        return isFileBacked() && !java.util.Arrays.equals(sha1, ChecksumUtil.sha1(Files.newInputStream(imagePath, StandardOpenOption.READ)));
    }

    private void updateImage() throws IOException {
        if (isFileBacked()) {
            BufferedImage newImage = ImageIO.read(imagePath.toFile());
            image = newImage;
        }
        byte[] newBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, extension, baos);
            newBytes = baos.toByteArray();
        } catch (Exception e) {
            LOG.log(Level.WARNING, null, e);
            newBytes = new byte[]{};
        }
        imageBytes = newBytes;
        if (isFileBacked()) {
            try (BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(imageBytes))) {
                sha1 = ChecksumUtil.sha1(bis);
            }
        }
    }

    @Override
    public Path getImagePath() {
        return imagePath;
    }

    @Override
    public String[] getValidMethods() {
        return new String[]{"GET", "HEAD"};
    }
}
