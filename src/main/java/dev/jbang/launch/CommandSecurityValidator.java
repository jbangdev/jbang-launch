package dev.jbang.launch;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Security validator to prevent command injection attacks when launching commands in terminals.
 */
public class CommandSecurityValidator {
    
    // Pattern to detect dangerous shell characters and command injection patterns
    private static final Pattern DANGEROUS_PATTERN = Pattern.compile(
        "[;&|`$(){}<>\\[\\]\\\\!\\*\\?~\\n\\r]" // Shell metacharacters
    );
    
    // Pattern to detect potential command injection attempts
    private static final Pattern INJECTION_PATTERN = Pattern.compile(
        "(\\$\\{[^}]*\\}|\\$\\([^)]*\\)|`[^`]*`|\\$\\(\\$[^)]*\\))" // Command substitution
    );
    
    /**
     * Validates command arguments for security risks.
     * 
     * @param commandArgs the command arguments to validate
     * @throws SecurityException if any dangerous patterns are detected
     */
    public static void validateCommand(List<String> commandArgs) {
        if (commandArgs == null || commandArgs.isEmpty()) {
            throw new SecurityException("Command arguments cannot be null or empty");
        }
        
        for (String arg : commandArgs) {
            validateArgument(arg);
        }
    }
    
    /**
     * Validates a single argument for security risks.
     * 
     * @param arg the argument to validate
     * @throws SecurityException if dangerous patterns are detected
     */
    public static void validateArgument(String arg) {
        if (arg == null) {
            throw new SecurityException("Argument cannot be null");
        }
        
        // Check for dangerous shell characters
        if (DANGEROUS_PATTERN.matcher(arg).find()) {
            throw new SecurityException("Dangerous shell characters detected in argument: " + arg);
        }
        
        // Check for command injection patterns
        if (INJECTION_PATTERN.matcher(arg).find()) {
            throw new SecurityException("Potential command injection detected in argument: " + arg);
        }
        
    }
} 