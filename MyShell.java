import java.io.*;
import java.util.*;

public class MyShell {
    // Store command history and the current directory
    private static ArrayList<String> history = new ArrayList<>();
    private static String currentDirectory = System.getProperty("user.dir").trim();

    public static void main(String[] args) {
        // Initialize scanner for command input
        Scanner getCommandLine = new Scanner(System.in);
        boolean loopFlag = true;

        try {
            // Main loop to keep shell running
            while (loopFlag) {
                // Display shell prompt
                System.out.print("\nmyShell> ");

                // Read command from the user
                String commandLine = getCommandLine.nextLine();
                if (commandLine.isEmpty()) {
                    continue; // Skip empty lines
                }

                // Handle 'exit' command
                if (commandLine.equals("exit")) {
                    System.out.println("Exit command found. myShell closing.\n");
                    break;
                }

                // Handle '!n' command to execute a previous command from history
                if (commandLine.startsWith("!")) {
                    int n = Integer.parseInt(commandLine.substring(1));
                    if (n <= history.size()) {
                        commandLine = history.get(n - 1);
                    } else {
                        System.out.println("Invalid history index");
                        continue;
                    }
                }

                // Add command to history
                history.add(commandLine);

                // Handle piped commands
                String[] commands = commandLine.split("\\|");
                PipedInputStream pin = null;
                PipedOutputStream pout = null;
                PipedInputStream newPin = null;

                // Execute each command
                for (String cmd : commands) {
                    cmd = cmd.trim();
                    pout = new PipedOutputStream();
                    newPin = new PipedInputStream(pout);
                    try {
                        executeCommand(cmd, pin, pout);
                    } catch (Exception e) {
                        System.out.println("Error while executing command: " + e.getMessage());
                    } finally {
                        if (pin != null) {
                            pin.close();
                        }
                        pin = newPin; // This sets pin for the next iteration.
                    }
                }
                if (pout != null) {
                    pout.close();
                }
            }
        } catch (Exception e) {
            // Handle any exceptions
            System.out.println("Interrupt detected. myShell closing.");
        } finally {
            // Close the scanner
            getCommandLine.close();
        }
    }

    // Method to execute a command
    private static void executeCommand(String cmd, PipedInputStream pin, PipedOutputStream pout) throws IOException {
        // Split command and arguments
        String[] parts = cmd.split("\\s+");
        String command = parts[0];

        // Determine which command to execute
        switch (command) {
            case "ls":
                ls(pout);
                break;
            case "cd":
                cd(parts, pout);
                break;
            case "cat":
                cat(parts, pin, pout);
                break;
            case "grep":
                grep(parts, pin, pout);
                break;
            case "lc":
                lc(pin, pout);
                break;
            case "pwd":
                pwd(pout);
                break;
            case "history":
                history(pout);
                break;
            default:
                System.out.println("Unknown command: " + command);
        }
    }

    // Implement 'ls' command
    private static void ls(PipedOutputStream pout) throws IOException {
        File directory = new File(currentDirectory);
        if (directory.exists() && directory.isDirectory()) {
            String[] files = directory.list();
            if (files != null) {
                for (String file : files) {
                    System.out.println(file); // Debug: Directly output to console
                    pout.write((file + "\n").getBytes());
                }
            }
        }
    }

    // Implement 'cd' command
    private static void cd(String[] parts, PipedOutputStream pout) throws IOException {
        if (parts.length > 1) {
            String newPath = parts[1];
            File newDir = new File(newPath);
            if (!newDir.isAbsolute()) {
                newDir = new File(currentDirectory, newPath);
            }
            if (newDir.isDirectory()) {
                currentDirectory = newDir.getAbsolutePath();
            } else {
                System.out.println("Directory not found: " + newPath);
            }
        } else {
            System.out.println("Please provide a directory path.");
        }
    }

    // Implement 'cat' command
    private static void cat(String[] parts, PipedInputStream pin, PipedOutputStream pout) throws IOException {
        for (int i = 1; i < parts.length; i++) {
            String fileName = parts[i];
            File fileToRead = new File(currentDirectory, fileName);
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileToRead));
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line); // Debug: Directly output to console
                    pout.write((line + "\n").getBytes());
                }
                br.close();
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + fileName);
                return; // Exit the current 'cat' command and go back to the main loop
            }
        }
    }

    // Implement 'grep' command
    private static void grep(String[] parts, PipedInputStream pin, PipedOutputStream pout) throws IOException {
        if (parts.length > 1) {
            String searchString = parts[1];
            BufferedReader br = new BufferedReader(new InputStreamReader(pin));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains(searchString.toLowerCase())) {
                    System.out.println(line); // Debug: Directly output to console
                    pout.write((line + "\n").getBytes());
                }
            }
        }
    }

    // Implement 'lc' command
    private static void lc(PipedInputStream pin, PipedOutputStream pout) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(pin));
        int lineCount = 0;
        while (br.readLine() != null) {
            lineCount++;
        }
        pout.write(Integer.toString(lineCount).getBytes());
    }

    // Implement 'pwd' command
    private static void pwd(PipedOutputStream pout) throws IOException {
        pout.write(currentDirectory.getBytes());
    }

    // Implement 'history' command
    private static void history(PipedOutputStream pout) throws IOException {
        for (int i = 0; i < history.size(); i++) {
            pout.write((i + 1 + " " + history.get(i) + "\n").getBytes());
        }
    }
}