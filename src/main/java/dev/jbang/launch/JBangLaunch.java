//JAVA 21+

package dev.jbang.launch;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

import javax.swing.JOptionPane;

public class JBangLaunch {
    public static void main(String[] args) {
        if(Desktop.isDesktopSupported()) {
            Desktop.getDesktop().setOpenURIHandler((event) -> {
                handleURI(event.getURI());
            });
        }

        if (args != null && args.length > 0) {
            String firstArg = args[0];
            
            URI uri = URI.create(firstArg);
            
            if(uri!=null && uri.getScheme()!=null) {
                handleURI(uri);
                System.exit(0);
            }
        }
    }
    
    private static void handleURI(URI uri) {
        
        if(GraphicsEnvironment.isHeadless()) {
            System.out.println(uri);
            return;
        } else {
            JOptionPane.showMessageDialog(
            null,
            "Received URL:\n" + uri.toString(),
            "URL Handler",
            JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
}
