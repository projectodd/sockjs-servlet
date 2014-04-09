package org.projectodd.sockjs;

import com.google.gson.Gson;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class Utils {

    public static String md5Hex(String content) throws SockJsException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(content.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new SockJsException("Error generating MD5 hex string", e);
        }
    }

    public static String generateExpires(Date date) {
        SimpleDateFormat df =  new SimpleDateFormat(RFC1123_PATTERN, LOCALE_US);
        df.setTimeZone(GMT_ZONE);
        return df.format(date);
    }

    public static String join(List<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> i = strings.iterator(); i.hasNext();) {
            sb.append(i.next()).append(i.hasNext() ? separator : "");
        }
        return sb.toString();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String quote(String string) {
        String quoted = "\"" + string + "\"";
        // TODO: crap with JSON.stringify, escapable
        return quoted;
    }

    public static <T> T parseJson(String json, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazz);
    }

    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    private static final Locale LOCALE_US = Locale.US;
    private static final TimeZone GMT_ZONE = TimeZone.getTimeZone("GMT");
}
