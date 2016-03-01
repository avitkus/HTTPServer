package edu.unc.cs.httpserver.pages;

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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;

/**
 *
 * @author Andrew Vitkus
 */
public class ImagePage extends AbstractPage implements IImagePage {

    private static final Logger LOG = Logger.getLogger(ImagePage.class.getName());

    private final Path imagePath;
    private BufferedImage image;
    private byte[] sha1;
    private byte[] imageBytes;
    private String extension;
    private final ContentType dispType;
    private final Object lock = new Object();

    public ImagePage(Path pagePath, BufferedImage image) throws IOException {
        super(pagePath);
        this.imagePath = null;
        updateExtension();
        synchronized (lock) {
            this.image = image;
            updateImage();
        }
        dispType = ContentType.parse(URLConnection.guessContentTypeFromName(getPagePath().getFileName().toString()));
    }

    public ImagePage(Path pagePath, Path imagePath) throws IOException {
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
                LOG.log(Level.SEVERE, null, ex);
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
                LOG.log(Level.SEVERE, null, ex);
            }
            return new ByteArrayEntity(imageBytes, dispType);
        }
    }

    @Override
    public boolean isFileBacked() {
        return imagePath != null;
    }

    private void updateExtension() {
        String pageName = getPagePath().getFileName().toString();
        int extensionStart = pageName.lastIndexOf('.');
        if (extensionStart > 0 && extensionStart < pageName.length() - 1) {
            extension = pageName.substring(extensionStart);
        }
    }

    private boolean doUpdateImage() throws IOException {
        return isFileBacked() && !Arrays.equals(sha1, ChecksumUtil.sha1(Files.newInputStream(imagePath, StandardOpenOption.READ)));
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
            LOG.log(Level.SEVERE, null, e);
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
