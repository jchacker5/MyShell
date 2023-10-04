import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MyShell {

    public static List<String> commandHistory = new ArrayList<>();

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        String commandLine;
        boolean loopFlag = true;

        while (loopFlag) {
            System.out.print("\nmyShell> ");
            commandLine = scanner.nextLine().trim();
            commandHistory.add(commandLine);

            String[] pipedCommands = commandLine.split("\\|");
            PipedInputStream pin = null;

            for (String cmd : pipedCommands) {
                CommandFactory cf = new CommandFactory(cmd, pin, new PipedOutputStream());
                pin = cf.executeCommand();
            }
        }

        System.out.println("\nmyShell closed.");
        scanner.close();
    }

    static class CommandFactory {

        private String[] args;
        private PipedInputStream pin;
        private PipedOutputStream pout;

        public CommandFactory(String cmd, PipedInputStream pin, PipedOutputStream pout) {
            this.args = cmd.split(" ");
            this.pin = pin;
            this.pout = pout;
        }

        public PipedInputStream executeCommand() throws IOException {
            String cmd = args[0];

            switch (cmd) {
                case "cat":
                    return executeCat();
                case "grep":
                    return executeGrep();
                case "pwd":
                    return executePwd();
                case "ls":
                    return executeLs();
                case "cd":
                    return executeCd();
                case "history":
                    return executeHistory();
                case "exit":
                    executeExit();
            }
            return null;
        }

        private PipedInputStream executeCat() throws IOException {
            if (pin == null) {
                // read files and write to pout
            } else {
                // read from pin and write to pout
            }
            pout.close();
            return new PipedInputStream();
        }

        private PipedInputStream executeGrep() throws IOException {
            // implementation
            pout.close();
            return new PipedInputStream();
        }

        private PipedInputStream executePwd() throws IOException {
            pout.write(System.getProperty("user.dir").getBytes());
            pout.close();
            return new PipedInputStream();
        }

        private PipedInputStream executeLs() throws IOException {
            // list files and write to pout
            pout.close();
            return new PipedInputStream();
        }

        private PipedInputStream executeCd() {
            System.setProperty("user.dir", args[1]);
            return null;
        }

        private PipedInputStream executeHistory() throws IOException {
            // write command history to pout
            pout.close();
            return new PipedInputStream();
        }

        private void executeExit() {
            System.exit(0);
        }

    }

}