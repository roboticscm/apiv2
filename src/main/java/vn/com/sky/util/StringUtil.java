package vn.com.sky.util;

import java.util.UUID;

import org.jsoup.Jsoup;

public class StringUtil {

    public static String toSnackCase(String str, String sep) {
        String ret = "";
        for (var i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (Character.isUpperCase(ch)) ret += sep + Character.toLowerCase(ch); else ret += ch;
        }

        return ret;
    }

    public static Boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String generateGUUID() {
        return UUID.randomUUID().toString();
    }
    
    public static String html2Text(String source) {
    	return Jsoup.parse(source).text();
    }
}
