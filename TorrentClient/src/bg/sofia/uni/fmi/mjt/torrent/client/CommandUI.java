package bg.sofia.uni.fmi.mjt.torrent.client;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class CommandUI {

    private final InputStream in;
    private final PrintStream out;

    public CommandUI(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public ClientActions setUpClient(String serverIp, int serverPort) {
        Scanner scanner = new Scanner(in);

        out.println("Enter username: ");
        String username = scanner.nextLine();

        out.println("Enter address file: ");
        String addressFile = scanner.nextLine();

        out.println("Enter port for the mini server: ");
        int port = scanner.nextInt();

        ClientProperties clientProperties =
                ClientProperties.setUpDependencies(serverIp, serverPort, addressFile, port, username);

        return new ClientActions(clientProperties, port);
    }

    private boolean consoleReader(Scanner scanner, ClientActions clientActions) {
        String command = scanner.nextLine();

        if (command.equals("disconnect")) {
            out.println(clientActions.stop());
            return false;
        }
        if (command.startsWith("download ")) {
            out.println(clientActions.download(command));
            return true;
        }
        if (command.startsWith("register ")) {
            out.println(clientActions.register(command));
            return true;
        }
        out.println(clientActions.serverCommand(command));
        return true;
    }

    public void run(ClientActions clientActions) {
        out.println(clientActions.start());
        Scanner scanner = new Scanner(in);

        boolean active = true;
        while (active) {
            active = consoleReader(scanner, clientActions);
        }

        scanner.close();
    }

    public static void main(String[] args) {
        //download niki C:\\Users\\nikip\\Documents\\test\\old\\dictionary.txt
        //C:\\Users\\nikip\\Documents\\test\\copy.txt");

        CommandUI commandUI = new CommandUI(System.in, System.out);

        commandUI.run(commandUI.setUpClient("localhost", 5555));
    }
}
