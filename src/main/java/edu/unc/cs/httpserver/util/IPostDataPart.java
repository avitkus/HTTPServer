package edu.unc.cs.httpserver.util;

/**
 *
 * @author Andrew Vitkus
 */
public interface IPostDataPart {

    public boolean isFileBacked();

    public String getDisposition();

    public String getEncoding();

    public String getName();

    public String getValue();
}
