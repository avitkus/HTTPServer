package edu.unc.cs.httpserver;

import edu.unc.cs.httpserver.pages.IPage;
import edu.unc.cs.httpserver.ssl.ISSLDataPackage;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author Andrew Vitkus
 */
public abstract class AbstractHttpServer implements IHttpServer{
    
    private static final Logger LOG = Logger.getLogger(AbstractHttpServer.class.getName());

    private static boolean hasMethodOverlap(IPage a, IPage b) {
        String[] aMethods = a.getValidMethods();
        String[] bMethods = b.getValidMethods();
        
        for(String aMethod : aMethods) {
            for(String bMethod : bMethods) {
                if (aMethod.equals(bMethod)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    private final Map<Path, IPage[]> pageMap;
    private final Map<Integer, IPage> errorPageMap;
    private final Path root;
    private final int port;
    private final ISSLDataPackage SSLData;

    AbstractHttpServer(Path root, int port) {
        this.root = root;
        this.port = port;
        this.SSLData = null;

        pageMap = Collections.synchronizedMap(new HashMap<>());
        errorPageMap = Collections.synchronizedMap(new HashMap<>());
    }

    AbstractHttpServer(Path root, int port, ISSLDataPackage SSLData) {
        this.root = root;
        this.port = port;
        this.SSLData = SSLData;

        pageMap = Collections.synchronizedMap(new HashMap<>());
        errorPageMap = Collections.synchronizedMap(new HashMap<>());
    }

    protected ISSLDataPackage getSSLData() {
        return SSLData;
    }
    
    @Override
    public boolean addPage(IPage page) {
        Path pagePath = page.getPagePath();
        if (pageMap.containsKey(pagePath)) {
            IPage[] curPages = pageMap.get(pagePath);
            for(IPage curPage : curPages) {
                if (hasMethodOverlap(page, curPage)) {
                    return false;
                }
            }
            IPage[] newPages = Arrays.copyOf(curPages, curPages.length + 1);
            newPages[newPages.length - 1] = page;
            pageMap.put(pagePath, newPages);
            return true;
        } else {
            pageMap.put(pagePath, new IPage[]{page});
            return true;
        }
    }

    @Override
    public boolean setErrorPage(int error, IPage page) {
        if (errorPageMap.containsKey(error)) {
            return false;
        } else {
            errorPageMap.put(error, page);
            return true;
        }
    }

    @Override
    public Path getRoot() {
        return root;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isHttp() {
        return SSLData == null;
    }

    @Override
    public Map<Path, IPage[]> getPages() {
        return Collections.unmodifiableMap(pageMap);
    }

    @Override
    public Map<Integer, IPage> getErrorPages() {
        return Collections.unmodifiableMap(errorPageMap);
    }

}
