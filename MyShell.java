
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MyShell {

    public static List<String> commandHistory = new ArrayList<>();

    public static void main(String[] args) {
        Scanner getCommandLine = new Scanner(System.in);
        String commandLine;
        boolean loopFlag = true;

        try {
            while (loopFlag) {
                System.out.print("\nmyShell> ");
                commandLine = getCommandLine.nextLine().trim();
                commandHistory.add(commandLine);

                String[] pipedCommands = commandLine.split("|");
                PipedInputStream pin = null;

                for (String singleCommand : pipedCommands) {
                    PipedOutputStream pout = new PipedOutputStream();
                    PipedInputStream newPin = new PipedInputStream(pout);

                    executeSingleCommand(singleCommand.trim(), pin, pout);

                    pin = newPin;
                }
            }
        } catch (Exception e) {
            System.out.println("\n\nInterrupt was detected. myShell closing.");
            System.exit(0);
        }
    }

    private static CommandFactory commandFactory;

    public static void executeSingleCommand(String command, PipedInputStream pin, PipedOutputStream pout)
            throws IOException {
        if (commandFactory == null) {
            commandFactory = new CommandFactory();
        }
        commandFactory.executeCommand(command, pin, pout);
    }

    static class CommandFactory {
        private String[] cmdPlusArgs;
        private PipedInputStream pin;
        private PipedOutputStream pout;

        public CommandFactory(String[] cmdPlusArgs, PipedInputStream pin, PipedOutputStream pout) {
            this.cmdPlusArgs = cmdPlusArgs;
            this.pin = pin;
            this.pout = pout;
        }

        public void executeCommand() throws IOException {
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
            }
        }

        private void executeCat() throws IOException {
            if (pin == null) {
                for (int i = 1; i < cmdPlusArgs.length; i++) {
                    Path filePath = Paths.get(cmdPlusArgs[i]);
                    Files.lines(filePath).forEach(line -> {
                        try {
                            pout.write((line + "\n").getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(pin));
                String line;
                while ((line = reader.readLine()) != null) {
                    pout.write((line + "\n").getBytes());
                }
            }
            pout.close();
        }

        private void executeGrep() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(pin));
            String line;
            String searchString = cmdPlusArgs[1];
            while ((line = reader.readLine()) != null) {
                if (line.contains(searchString)) {
                    pout.write((line + "\n").getBytes());
                }
            }
            pout.close();
        }

        private void executeLc() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(pin));
            int lineCount = 0;
            while (reader.readLine() != null) {
                lineCount++;
            }
            pout.write(Integer.toString(lineCount).getBytes());
            pout.close();
        }

        private void executeHistory() throws IOException {
            for (String cmd : commandHistory) {
                pout.write((cmd + "\n").getBytes());
            }
            pout.close();
        }

        private void executePwd() throws IOException {
            String dir = System.getProperty("user.dir");
            pout.write((dir + "\n").getBytes());
            pout.close();
        }

        private void executeLs() throws IOException {
            File dir = new File(System.getProperty("user.dir"));
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        pout.write((file.getName() + "\n").getBytes());
                    }
                }
            }
            pout.close();
        }

        private void executeCd() {
            String dir = cmdPlusArgs[1];
            System.setProperty("user.dir", dir);
        }

        private void executeExit() {
            System.out.println("Exit command found. myShell closing.");
            System.exit(0);
        }
    }
}
