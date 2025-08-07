//JAVA 21+
//SOURCES UrlConverter.java
package dev.jbang.launch;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
                    System.err.println("Usage: jbang-launch --to-url jbang <command> [args]");
                    System.err.println("       jbang-launch --to-url - (read command from stdin)");
                    System.exit(1);
                }
                
                String[] commandArgs = readFromStdinOrArgs(args, 1);
                
                String url = UrlConverter.commandToUrl(commandArgs);
                System.out.println(url);
                System.exit(0);
            } else if (args[0].equals("--from-url")) {
                if (args.length < 2) {
                    System.err.println("Usage: jbang-launch --from-url <jbang://url>");
                    System.err.println("       jbang-launch --from-url - (read URL from stdin)");
                    System.exit(1);
                }
                
                String url = readFromStdinOrArgs(args, 1)[0];
                
                String command = UrlConverter.urlToCommandString(url);
                System.out.println(command);
                System.exit(0);
            }
        }

        // Original URL handler functionality
        installURIListener();

        if (args != null && args.length > 0) {
            String firstArg = args[0];

            URI uri = URI.create(firstArg);

            if (uri != null && uri.getScheme() != null) {
                handleURI(uri);
                System.exit(0);
            }
        }
    }

    /**
     * Installs the URI listener which is used to handle the jbang:// protocol 
     * on MacOS.
     */
    private static void installURIListener() {
        if (Desktop.isDesktopSupported()) {
            Desktop dt = Desktop.getDesktop();
            if (dt.isSupported(Desktop.Action.APP_OPEN_URI)) {
                dt.setOpenURIHandler((event) -> {
                    handleURI(event.getURI());
                });
            }
        }
    }

    private static void handleURI(URI uri) {
            // Convert URI to command using UrlConverter
            List<String> commandArgs = UrlConverter.urlToCommand(uri.toString());
            String commandString = String.join(" ", commandArgs);
            
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("URL: " + uri);
                System.out.println("Command: " + commandString);
                return;
            } else {
                // Create message with both URL and converted command
                String message = "Received URL:\n" + uri.toString() + "\n\nConverted to command:\n" + commandString;
                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                } catch (UnsupportedLookAndFeelException e) {
                    System.err.println("WARN: Failed to set look and feel");
                    e.printStackTrace();
                }
                JOptionPane.showMessageDialog(
                        null,
                        message,
                        "URL Handler",
                        JOptionPane.INFORMATION_MESSAGE);
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
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();
                scanner.close();
                return input.split("\\s+");
            } else {
                System.err.println("No input provided on stdin");
                System.exit(1);
                return new String[0]; // This line will never be reached due to System.exit(1)
            }
        } else {
            // Use provided arguments
            return Arrays.copyOfRange(args, index, args.length);
        }
    }
}

