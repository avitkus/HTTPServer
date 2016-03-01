package edu.unc.cs.httpserver.util;

/**
 *
 * @author Andrew
 */
public class ResponseStatusNotice extends RuntimeException {
    
    private final int status;

    /**
     * Creates a new instance of <code>ResponseStatusNotice</code> without detail
     * message.
     * 
     * @param status the alternate request status
     */
    public ResponseStatusNotice(int status) {
        this.status = status;
    }

    /**
     * Constructs an instance of <code>ResponseStatusNotice</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     */
    public ResponseStatusNotice(String msg, int status) {
        super(msg);
        this.status = status;
    }
    
    public int getStatus() {
        return status;
    }
}
