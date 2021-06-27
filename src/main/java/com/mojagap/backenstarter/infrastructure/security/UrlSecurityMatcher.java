package com.mojagap.backenstarter.infrastructure.security;

import java.util.regex.Pattern;

public class UrlSecurityMatcher {

    /**
     * '/*' matches at the level that is specified
     * '/**' matches the entire directory tree
     */
    public static boolean matches(String urlPattern, String url) {
        if (urlPattern == null || url == null) return false;
        url = appendEndingSlash(url);
        if (!urlPattern.endsWith("/**")) {
            urlPattern = appendEndingSlash(urlPattern);
        }
        String regex = convertToRegex(urlPattern).toLowerCase();
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(url.toLowerCase()).matches();
    }

    private static String appendEndingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }

    private static String convertToRegex(String urlPattern) {
        String doubleAsteriskPlaceholder = "iQiHwt5kc2rf95je9hf@@##";
        return urlPattern.trim()
                .replace("**", doubleAsteriskPlaceholder)
                .replace("*", "[^/]*?")
                .replace(doubleAsteriskPlaceholder, "**")
                .replace("**", ".*?");
    }
}
