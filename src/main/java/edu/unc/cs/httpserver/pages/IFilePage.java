package edu.unc.cs.httpserver.pages;

import java.nio.file.Path;

/**
 *
 * @author Andrew Vitkus
 */
public interface IFilePage extends IPage {

    public Path getFilePath();
}
