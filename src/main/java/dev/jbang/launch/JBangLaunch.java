//JAVA 21+

package dev.jbang.launch;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;

public class JBangLaunch {
    public static void main(String[] args) {
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
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println(uri);
            return;
        } else {
             try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (UnsupportedLookAndFeelException e) {
                System.err.println("WARN: Failed to set look and feel");
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(
                    null,
                    "Received URL:\n" + uri.toString(),
                    "URL Handler",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

}
