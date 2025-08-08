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
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window.Type;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
                // Don't exit immediately - let the dialog handle the flow
                return;
            }
        }
        
        // If no URI was provided as argument, keep the application alive
        // for URI handler functionality (especially on macOS)
        try {
            // Keep the main thread alive
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            // Exit gracefully if interrupted
            exit(0);
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
        String[] commandArgs = urlToCommand(uri.toString()).toArray(new String[0]);
        
        if (isHeadless()) {
            out.println("URL: " + uri);
            out.println("Command: " + String.join(" ", commandArgs));
            // Execute the command in headless mode
            executeJbangCommand(commandArgs);
        } else {
            setupLookAndFeel();
            
            SwingUtilities.invokeLater(() -> {
                try {
                    Rectangle screenBounds = getActiveScreenBounds();
                    
                    // Create a simple confirmation dialog
                    JOptionPane optionPane = new JOptionPane(
                        "Execute jbang command?\n\n" + String.join(" ", commandArgs),
                        JOptionPane.QUESTION_MESSAGE,
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    JDialog dialog = optionPane.createDialog("jbang:// URL Handler");
                    
                    // Position the dialog on the same screen as the mouse, but centered
                    int dialogX = screenBounds.x + (screenBounds.width / 2) - 150;
                    int dialogY = screenBounds.y + (screenBounds.height / 2) - 75;
                    
                    dialog.setLocation(dialogX, dialogY);
                    
                    // Show the dialog
                    dialog.setVisible(true);
                    
                    // Get the result
                    Object selectedValue = optionPane.getValue();
                    int result = JOptionPane.CLOSED_OPTION;
                    
                    if (selectedValue != null) {
                        if (selectedValue.equals(JOptionPane.YES_OPTION)) {
                            result = JOptionPane.YES_OPTION;
                        } else if (selectedValue.equals(JOptionPane.NO_OPTION)) {
                            result = JOptionPane.NO_OPTION;
                        }
                    }
                    
                    if (result == JOptionPane.YES_OPTION) {
                        // Execute the command
                        executeJbangCommand(commandArgs);
                    }
                    
                    
                    // Exit after dialog is handled
                    exit(0);
                } catch (Exception e) {
                    err.println("Error showing dialog: " + e.getMessage());
                    e.printStackTrace();
                    exit(1);
                }
            });
            
        }
    }

    /**
     * Needed to position the dialog on the same screen as the mouse.
     * Otherwise the dialog will be shown on the default screen which
     * might not be the one the user is currently using.
     * @return
     */
    private static Rectangle getActiveScreenBounds() {
        // Get the current mouse location
        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        
        // Find which screen contains the mouse cursor
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice targetScreen = null;
        
        for (GraphicsDevice screen : ge.getScreenDevices()) {
            Rectangle bounds = screen.getDefaultConfiguration().getBounds();
            if (bounds.contains(mouseLocation)) {
                targetScreen = screen;
                break;
            }
        }
        
        // If no screen found, use the default screen
        if (targetScreen == null) {
            targetScreen = ge.getDefaultScreenDevice();
        }
        
        // Get the bounds of the target screen
        Rectangle screenBounds = targetScreen.getDefaultConfiguration().getBounds();
        return screenBounds;
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
     * Shows a security error dialog to the user.
     */
    private static void showSecurityErrorDialog(String title, String message) {
        
        
try {

                 Rectangle screenBounds = getActiveScreenBounds();
                    
                    JOptionPane optionPane = new JOptionPane(
                        "Security Error\n\n" + message + "\n\nThis command was rejected for security reasons.",
                        JOptionPane.ERROR_MESSAGE,
                        JOptionPane.DEFAULT_OPTION
                    );
                    
                    JDialog dialog = optionPane.createDialog(title);
                    
                    // Position the dialog on the same screen as the mouse, but centered
                    int dialogX = screenBounds.x + (screenBounds.width / 2) - 200;
                    int dialogY = screenBounds.y + (screenBounds.height / 2) - 100;
                    
                    dialog.setLocation(dialogX, dialogY);
                    
                    // Show the dialog
                    dialog.setVisible(true);
                    
                    // Exit after dialog is handled
                    exit(0);
                } catch (Exception e) {
                    err.println("Error showing security dialog: " + e.getMessage());
                    e.printStackTrace();
                    exit(1);
                }
            
       
    }
    
    /**
     * Executes a jbang command with stdin redirected to /dev/null to discard input.
     */
    private static void executeJbangCommand(String... args) {
       System.out.println("Executing jbang command: " + String.join(" ", args));

      try {
        TerminalLauncher.launchInTerminal(Arrays.asList(args));
        System.out.println("Command executed successfully");
      } catch (IOException e) {
        if (isHeadless()) {
            err.println("Failed to execute jbang command: " + e.getMessage());
            e.printStackTrace();
            exit(1);
        } else {
            showSecurityErrorDialog("Execution Error", "Failed to execute jbang command: " + e.getMessage());
        }
      } catch (SecurityException e) {
        if (isHeadless()) {
            err.println("Security violation: " + e.getMessage());
            exit(1);
        } else {
            showSecurityErrorDialog("Security Violation", e.getMessage());
        }
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
            } else {
                err.println("No input provided on stdin");
                exit(1);
                return new String[0]; // This line will never be reached due to System.exit(1)
            }
            }
        } else {
            return Arrays.copyOfRange(args, index, args.length);
        }
        return new String[0];
    }
}

