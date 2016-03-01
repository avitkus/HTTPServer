package edu.unc.cs.httpserver.pages;

import java.awt.Image;
import java.nio.file.Path;

/**
 *
 * @author Andrew Vitkus
 */
public interface IImagePage extends IPage {

    public Image getImage();

    public boolean isFileBacked();

    public Path getImagePath();
}
