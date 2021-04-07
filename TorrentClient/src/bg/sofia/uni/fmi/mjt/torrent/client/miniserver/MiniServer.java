package bg.sofia.uni.fmi.mjt.torrent.client.miniserver;

import bg.sofia.uni.fmi.mjt.torrent.client.file.FileData;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiniServer extends Thread {
    private static final Logger LOGGER = Logger.getLogger(MiniServer.class.getName());
    private static final int CHUNK_SIZE = 1024;
    private final int port;

    public MiniServer(int port) {
        this.port = port;
        createLogHandler();
    }

    private void createLogHandler() {
        try {
            FileHandler handler = new FileHandler("logs/miniServer.log");
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Creating log handler failed: "
                    + exception.getMessage(), exception);
        }
    }

    private void sendFile(String pathToFile, DataOutputStream socketOutput) throws IOException {
        FileData file = new FileData(pathToFile);
        socketOutput.writeLong(file.getSize());
        InputStream inputStream = file.newInputStream();

        int bytesCount;
        byte[] buffer = new byte[CHUNK_SIZE];
        while ((bytesCount = inputStream.read(buffer)) != -1) {
            socketOutput.write(buffer, 0, bytesCount);
            socketOutput.flush();
        }
        inputStream.close();
    }

    private void serviceClient(Socket clientSocket) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream socketOutput = new DataOutputStream(clientSocket.getOutputStream())) {
            String pathToFile = br.readLine();
            if (pathToFile.isBlank()) {
                return;
            }
            sendFile(pathToFile, socketOutput);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Sending file failed: "
                    + exception.getMessage(), exception);
        }
    }

    private void server() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("Mini-server started.");
            while (!isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                serviceClient(clientSocket);
                clientSocket.close();
            }
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Opening server failed: "
                    + exception.getMessage(), exception);
        }
    }

    public void close() {
        this.interrupt();
        try (Socket poisonSocket = new Socket("localhost", port);
                PrintWriter writer = new PrintWriter(poisonSocket.getOutputStream(), true)) {
            writer.println("");
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Closing server failed: "
                    + exception.getMessage(), exception);
        }
    }

    @Override
    public void run() {
        server();
    }
}
