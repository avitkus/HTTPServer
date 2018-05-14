package edu.unc.cs.httpserver;

import edu.unc.cs.httpserver.pages.IPage;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author Andrew Vitkus
 */
public interface IHttpServer {

    public boolean addPage(IPage page);

    public void start() throws Exception;
    public void close() throws Exception;

    public boolean setErrorPage(int error, IPage page);

    public Path getRoot();

    public Map<Path, IPage[]> getPages();

    public Map<Integer, IPage> getErrorPages();

    public int getPort();

    public boolean isHttp();

    public boolean isBlocking();
}
