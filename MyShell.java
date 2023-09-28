// Purpose: This program is a simple shell that can execute commands.
// author: Joseph Defendre 
// date: 9/15/2023

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MyShell {

    // List to keep track of command history
    public static List<String> commandHistory = new ArrayList<>();

    public static void main(String[] args) {
        // Create a Scanner object to read in command line
        Scanner getCommandLine = new Scanner(System.in);
        // String to store command line input
        String commandLine;
        // Flag to allow looping for additional command lines
        boolean loopFlag = true;

        try {
            while (loopFlag) {
                // Display Shell cursor
                System.out.print("\nmyShell> ");
                // Get new command line input
                commandLine = getCommandLine.nextLine().trim();
                // Add command to history
                commandHistory.add(commandLine);

                Pattern exitCommand = Pattern.compile("exit");
                Matcher exitMatched = exitCommand.matcher(commandLine);

                if (exitMatched.find()) {
                    System.out.println("Exit command found. myShell closing.\n");
                    getCommandLine.close();
                    System.exit(0);
                } else {
                    String[] commandParts = commandLine.split(" ");
                    CommandFactory commandFactory = new CommandFactory(commandParts);
                    commandFactory.executeCommand();
                }
            }
        } catch (Exception e) {
            System.out.println("\n\nInterrupt was detected. myShell closing.");
            System.exit(0);
        }
    }

    static class CommandFactory {
        private String[] cmdPlusArgs;

        public CommandFactory(String[] cmdPlusArgs) {
            this.cmdPlusArgs = cmdPlusArgs;
        }

        public void executeCommand() {
            try {
                String command = cmdPlusArgs[0];
                switch (command) {
                    case "cat":
                        executeCat();
                        break;
                    case "grep":
                        executeGrep();
                        break;
                    case "lc":
                        executeLc();
                        break;
                    case "history":
                        executeHistory();
                        break;
                    case "pwd":
                        executePwd();
                        break;
                    case "ls":
                        executeLs();
                        break;
                    case "cd":
                        executeCd();
                        break;
                    case "exit":
                        executeExit();
                        break;
                    case "!":
                        executeHistoryCommand();
                        break;
                    default:
                        System.out.println("Unknown command: " + command);
                }
            } catch (IOException e) {
                if (e.getMessage().contains("File not found")) {
                    System.out.println("The file does not exist.");
                } else {
                    System.out.println("An error occurred while reading the file: " + e.getMessage());
                }
            }
        }

        private void executeCat() throws IOException {
            // Here, the code reads all lines from the file and prints them out
            for (int i = 1; i < cmdPlusArgs.length; i++) {
                Path filePath = Paths.get(cmdPlusArgs[i]);
                Files.lines(filePath).forEach(System.out::println);
            }
        }

        private void executeGrep() {
            // Here, the code searches for the specified string in each line
            Scanner scanner = null;
            try {
                if (cmdPlusArgs.length < 2) {
                    System.out.println("Missing searchString argument");
                    return;
                }
                String searchString = cmdPlusArgs[1];
                scanner = new Scanner(System.in);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.contains(searchString)) {
                        System.out.println(line);
                    }
                }
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        private void executeLc() {
            Scanner scanner = null;
            try {
                scanner = new Scanner(System.in);
                int lineCount = 0;
                while (scanner.hasNext()) {
                    scanner.nextLine();
                    lineCount++;
                }
                System.out.println(lineCount);
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }

        private void executeHistory() {
            // Here, the code prints the history of commands
            int index = 1;
            for (String command : MyShell.commandHistory) {
                System.out.println(index + " " + command);
                index++;
            }
        }

        private void executePwd() {
            // Here, the code prints the current directory
            System.out.println(System.getProperty("user.dir"));
        }

        private void executeLs() throws IOException {
            // Here, the code lists all files in the current directory
            Path directory = Paths.get(System.getProperty("user.dir"));
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
                for (Path file : stream) {
                    System.out.println(file.getFileName());
                }
            }
        }

        private void executeCd() {
            // Here, the code changes the current directory
            if (cmdPlusArgs.length < 2) {
                System.out.println("Missing directory argument");
                return;
            }
            String dir = cmdPlusArgs[1];
            Path directory = Paths.get(dir);
            if (Files.exists(directory) && Files.isDirectory(directory)) {
                System.setProperty("user.dir", dir);
            } else {
                System.out.println("Invalid directory: " + dir);
            }
        }

        private void executeExit() {
            // Here, the code exits the application
            System.out.println("Exit command found. myShell closing.");
            System.exit(0);
        }

        private void executeHistoryCommand() {
            // Here, the code executes a command from the history
            if (cmdPlusArgs.length < 1) {
                System.out.println("Missing command number");
                return;
            }
            String commandNumberString = cmdPlusArgs[0].substring(1);
            int commandNumber = Integer.parseInt(commandNumberString);
            if (commandNumber <= 0 || commandNumber > MyShell.commandHistory.size()) {
                System.out.println("Command index out of range");
                return;
            }
            String commandToExecute = MyShell.commandHistory.get(commandNumber - 1);
            System.out.println("Executing: " + commandToExecute);

            // Logic to execute the retrieved command
            String[] commandParts = commandToExecute.split(" ");
            CommandFactory commandFactory = new CommandFactory(commandParts);
            commandFactory.executeCommand();
        }
    }
}
