package edu.unc.cs.httpserver.util;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

/**
 *
 * @author Andrew Vitkus
 */
public class HeaderUtil {

    private static final String COOKIE_HEADER_NAME = "Cookie";

    public static Optional<HttpCookie[]> getCookies(Header[] headers) {
        List<HttpCookie> cookies = new ArrayList<>();
        Arrays.stream(headers).forEach(header -> {
            if (header.getName().equals(COOKIE_HEADER_NAME)) {
                if (header.getValue().length() > 0) {
                    String[] cookieArr = header.getValue().split("; ");
                    for(String cookie : cookieArr) {
                        cookies.addAll(HttpCookie.parse(cookie));
                    }
                }
            }
        });
        if (cookies.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(cookies.toArray(new HttpCookie[cookies.size()]));
        }
    }

    public static Header[] addCookies(HttpCookie[] cookies) {
        return Arrays.stream(cookies).map(cookie -> new BasicHeader("Set-Cookie", cookie.toString())).toArray(Header[]::new);
    }
}
