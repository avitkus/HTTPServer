package edu.unc.cs.httpserver.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 *
 * @author Andrew Vitkus
 */
public class ChecksumUtil {

    private static final int BUFFER_SIZE = 1024;

    public static byte[] sha1(InputStream data) throws IOException {
        try {
            return generate(data, "SHA1");
        } catch (NoSuchAlgorithmException ex) {
            return new byte[]{};
        }
    }

    public static byte[] md5(InputStream data) throws IOException {
        try {
            return generate(data, "MD5");
        } catch (NoSuchAlgorithmException ex) {
            return new byte[]{};
        }
    }

    public static byte[] crc32(InputStream data) throws IOException {
        return generate(data, new CRC32());
    }

    private static byte[] generate(InputStream data, String encoding) throws NoSuchAlgorithmException, IOException {
        MessageDigest generator = MessageDigest.getInstance(encoding);

        byte[] buf = new byte[BUFFER_SIZE];
        try (BufferedInputStream bis = new BufferedInputStream(data)) {
            int read;
            do {
                read = bis.read(buf);
                if (read >= 0) {
                    generator.update(buf, 0, read);
                }
            } while (read > 0);
        }
        return generator.digest();
    }

    private static byte[] generate(InputStream data, Checksum checksum) throws IOException {
        byte[] buf = new byte[BUFFER_SIZE];
        try (BufferedInputStream bis = new BufferedInputStream(data)) {
            int read;
            do {
                read = bis.read(buf);
                checksum.update(buf, 0, read);
            } while (read > 0);
        }
        long value = checksum.getValue();
        ByteBuffer valueBuf = ByteBuffer.allocate(Long.BYTES);
        valueBuf.putLong(value);
        return valueBuf.array();
    }
}
