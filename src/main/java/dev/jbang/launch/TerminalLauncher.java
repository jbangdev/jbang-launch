package dev.jbang.launch;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TerminalLauncher {

    public static void launchInTerminal(List<String> commandArgs) throws IOException {
        if (commandArgs == null || commandArgs.isEmpty()) {
            throw new IllegalArgumentException("No command specified.");
        }

        // Security validation - hard fail on any suspicious patterns
        
        CommandSecurityValidator.validateCommand(commandArgs);
        
        String os = System.getProperty("os.name").toLowerCase();
        String command = commandArgs.stream()
                .map(TerminalLauncher::escapeShellArg)
                .collect(Collectors.joining(" "));

        if (isWSL()) {
            launchInWSL(command);
        } else if (os.contains("win")) {
            launchOnWindows(command);
        } else if (os.contains("mac")) {
            launchOnMac(command);
        } else if (os.contains("nux") || os.contains("nix")) {
            launchOnLinux(command);
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }
    }

    // region OS-specific implementations

    private static void launchOnWindows(String command) throws IOException {
        if (isCommandAvailable("wt.exe")) {
            new ProcessBuilder("cmd", "/c", "start", "wt.exe", "powershell", "-NoExit", "-Command", command).start();
        } else if (isCommandAvailable("powershell.exe")) {
            new ProcessBuilder("cmd", "/c", "start", "powershell", "-NoExit", "-Command", command).start();
        } else {
            new ProcessBuilder("cmd", "/c", "start", "cmd", "/k", command).start();
        }
    }

    private static void launchInWSL(String command) throws IOException {
        if (isCommandAvailable("wt.exe")) {
            new ProcessBuilder("cmd.exe", "/c", "start", "wt.exe", "wsl", "-e", "bash", "-c", command).start();
        } else {
            throw new UnsupportedOperationException("Windows Terminal (wt.exe) not found for WSL launch.");
        }
    }

    private static void launchOnMac(String command) throws IOException {
        new ProcessBuilder("osascript", "-e",
                "tell app \"Terminal\" to do script \"" + command.replace("\"", "\\\"") + "\"").start();
    }

    private static void launchOnLinux(String command) throws IOException {
        List<String> preferredTerms = detectPreferredLinuxTerminals();

        for (String term : preferredTerms) {
            if (isCommandAvailable(term)) {
                List<String> cmd = new ArrayList<>();
                cmd.add(term);
                if (!term.contains("xterm")) {
                    cmd.add("--");
                }
                cmd.addAll(List.of("bash", "-c", command + "; exec bash"));
                new ProcessBuilder(cmd).start();
                return;
            }
        }

        throw new IOException("No known terminal emulator found on Linux.");
    }

    // endregion

    // region Helpers

    private static boolean isCommandAvailable(String cmd) {
        try {
            Process p = new ProcessBuilder("which", cmd).start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isWSL() {
        try {
            Process process = new ProcessBuilder("uname", "-r").start();
            Scanner scanner = new Scanner(process.getInputStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase();
                if (line.contains("microsoft")) {
                    return true;
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    private static List<String> detectPreferredLinuxTerminals() {
        String desktop = System.getenv("XDG_CURRENT_DESKTOP");
        String session = System.getenv("DESKTOP_SESSION");

        Set<String> terms = new LinkedHashSet<>();

        if (desktop != null) {
            if (desktop.contains("GNOME")) terms.add("gnome-terminal");
            if (desktop.contains("KDE")) terms.add("konsole");
            if (desktop.contains("XFCE")) terms.add("xfce4-terminal");
            if (desktop.contains("MATE")) terms.add("mate-terminal");
        }

        if (session != null) {
            if (session.contains("xfce")) terms.add("xfce4-terminal");
            if (session.contains("kde")) terms.add("konsole");
        }

        terms.addAll(List.of("tilix", "gnome-terminal", "konsole", "xfce4-terminal", "mate-terminal", "xterm"));
        return new ArrayList<>(terms);
    }

    private static String escapeShellArg(String arg) {
        return arg.replace("'", "'\"'\"'");
    }

    // endregion
}