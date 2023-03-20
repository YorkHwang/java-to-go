package com.york.java.to.go.util;

/**
 * @author York.Hwang
 */
public class Utils {
    public static boolean isBlank(final String str) {
        return str == null || str.trim().length() == 0;
    }

    private Utils(){/*do nothing*/}
}
