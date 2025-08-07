//JAVA 21+
//SOURCES UrlConverter.java
package dev.jbang.launch;

import static dev.jbang.launch.UrlConverter.urlToCommand;
import static dev.jbang.launch.UrlConverter.urlToCommandString;
import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;
import static java.awt.GraphicsEnvironment.isHeadless;
import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.out;

import java.awt.Desktop;
import java.net.URI;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

public class JBangLaunch {
    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            // Check for conversion flags
            if (args[0].equals("--to-url")) {
                if (args.length < 2) {
                    err.println("Usage: jbang-launch --to-url jbang <command> [args]");
                    err.println("       jbang-launch --to-url - (read command from stdin)");
                    exit(1);
                }
                
                var commandArgs = readFromStdinOrArgs(args, 1);
                
                var url = UrlConverter.commandToUrl(commandArgs);
                out.println(url);
                exit(0);
            } else if (args[0].equals("--from-url")) {
                if (args.length < 2) {
                    err.println("Usage: jbang-launch --from-url <jbang://url>");
                    err.println("       jbang-launch --from-url - (read URL from stdin)");
                    exit(1);
                }
                
                String url = readFromStdinOrArgs(args, 1)[0];
                
                out.println(urlToCommandString(url));
                exit(0);
            }
        }

        // Original URL handler functionality
        installURIListener();

        if (args != null && args.length > 0) {
            String firstArg = args[0];

            URI uri = URI.create(firstArg);

            if (uri != null && uri.getScheme() != null) {
                handleURI(uri);
                exit(0);
            }
        }
    }

    /**
     * Installs the URI listener which is used to handle the jbang:// protocol 
     * on MacOS.
     */
    private static void installURIListener() {
        if (isDesktopSupported()) {
            Desktop dt = getDesktop();
            if (dt.isSupported(Desktop.Action.APP_OPEN_URI)) {
                dt.setOpenURIHandler((event) -> {
                    handleURI(event.getURI());
                });
            }
        }
    }

    private static void handleURI(URI uri) {
            // Convert URI to command using UrlConverter
            String commandString = String.join(" ", urlToCommand(uri.toString()));
            
            if (isHeadless()) {
                out.println("URL: " + uri);
                out.println("Command: " + commandString);
                return;
            } else {
                
                setupLookAndFeel();
            
                JOptionPane.showMessageDialog(
                        null,
                        "Received URL:\n%s\n\nConverted to command:\n%s".formatted(uri.toString(), commandString),
                        "URL Handler",
                        JOptionPane.INFORMATION_MESSAGE);
            }
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            err.println("WARN: Failed to set look and feel");
            e.printStackTrace();
        }
    }
    
    /**
     * Reads input from stdin if the argument is "-", otherwise returns the argument at the specified index.
     * 
     * @param args the command line arguments
     * @param index the index of the argument to check
     * @return array of strings (either from stdin or the single argument)
     */
    private static String[] readFromStdinOrArgs(String[] args, int index) {
        if (args[index].equals("-")) {
            // Read from stdin
            try (var sc = new Scanner(System.in)) {
            if (sc.hasNextLine()) {
                String input = sc.nextLine().trim();
                return input.split("\\s+");
            } else {
                err.println("No input provided on stdin");
                exit(1);
                return new String[0]; // This line will never be reached due to System.exit(1)
            }
        }
        } else {
            // Use provided arguments
            return Arrays.copyOfRange(args, index, args.length);
        }
    }
}

