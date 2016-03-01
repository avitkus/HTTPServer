package edu.unc.cs.httpserver;

/**
 *
 * @author Andrew Vitkus
 */
public class ServerInitalizationException extends Exception {

    /**
     * Creates a new instance of <code>ServerInitalizationException</code>
     * without detail message.
     */
    public ServerInitalizationException() {
    }

    /**
     * Constructs an instance of <code>ServerInitalizationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public ServerInitalizationException(String msg) {
        super(msg);
    }
}
