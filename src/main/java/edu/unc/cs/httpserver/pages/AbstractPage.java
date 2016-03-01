package edu.unc.cs.httpserver.pages;

import java.nio.file.Path;

/**
 *
 * @author Andrew Vitkus
 */
public abstract class AbstractPage implements IPage {

    private final Path pagePath;

    public AbstractPage(Path pagePath) {
        this.pagePath = pagePath;
    }

    @Override
    public Path getPagePath() {
        return pagePath;
    }

    @Override
    public String[] getValidMethods() {
        return new String[]{"GET"};
    }
}
