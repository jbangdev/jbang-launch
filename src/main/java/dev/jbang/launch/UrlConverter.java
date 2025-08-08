package dev.jbang.launch;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for converting between jbang command lines and jbang:// URLs.
 * This class has minimal dependencies and can be used independently of picocli.
 */
public class UrlConverter {
    
    /**
     * Converts a command line to a <cmd>://<arg1>/<arg2>/... URL.
     * 
     * @param args the command line arguments (including "cmd" as the first argument)
     * @return the jbang:// URL
     * @throws IllegalArgumentException if args is null or empty
     */
    public static URI commandToUrl(String... args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Command line arguments cannot be null or empty");
        }
        
        if (args.length < 2) {
            throw new IllegalArgumentException("Command line must have at least 2 arguments (jbang <command>)");
        }
        
        // Convert each argument to a URL segment, but skip the first "jbang"
        List<String> segments = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            segments.add(encode(args[i]));
        }
        
        String path = "/" + String.join("/", segments);
        return URI.create(args[0] + "://" + path);
    }
    
    
    /**
     * Converts a jbang:// URL back to a command line.
     * 
     * @param url the jbang:// URL to convert
     * @return the command line arguments as a list (including "jbang" as the first element)
     * @throws IllegalArgumentException if url is null or invalid
     */
    public static List<String> urlToCommand(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        try {
            URI uri = new URI(url);
            
            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("Missing path in " + url);
            }
            
            // Split path into segments and decode each one
            List<String> args = new ArrayList<>();
            args.add(uri.getScheme()); // Add scheme as first argument
            String[] segments = path.split("/", -1); // -1 preserves trailing empty segments
            for (int i = 1; i < segments.length; i++) { // Skip first empty segment from leading slash
                args.add(decode(segments[i]));
            }
            
            return args;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
    
    /**
     * Converts a jbang:// URL back to a command line string.
     * 
     * @param url the jbang:// URL to convert
     * @return the command line as a quoted string
     * @throws IllegalArgumentException if url is null or invalid
     */
    public static String urlToCommandString(String url) {
        List<String> args = urlToCommand(url);
        return String.join(" ", quoteArgs(args));
    }
    
    /**
     * URL-encodes a string using RFC 3986 encoding for URL paths.
     */
    private static String encode(String s) {
        try {
            // Use URI constructor for RFC 3986 compliant encoding
            URI uri = new URI("http", "example.com", "/" + s, null);
            String encoded = uri.getRawPath();
            // Remove the leading slash that URI adds
            return encoded.substring(1);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Failed to encode string: " + s, e);
        }
    }
    
    /**
     * URL-decodes a string using UTF-8 encoding.
     */
    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
    
    /**
     * Quotes arguments that contain spaces or quotes.
     */
    private static List<String> quoteArgs(List<String> args) {
        List<String> quoted = new ArrayList<>();
        for (String arg : args) {
            if (arg.contains(" ") || arg.contains("\"") || arg.contains("'")) {
                quoted.add("\"" + arg.replace("\"", "\\\"") + "\"");
            } else {
                quoted.add(arg);
            }
        }
        return quoted;
    }
} 