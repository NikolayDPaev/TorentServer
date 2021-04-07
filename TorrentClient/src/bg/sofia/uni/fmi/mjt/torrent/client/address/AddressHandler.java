package bg.sofia.uni.fmi.mjt.torrent.client.address;

import bg.sofia.uni.fmi.mjt.torrent.client.connection.ConnectionException;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ServerConnection;
import bg.sofia.uni.fmi.mjt.torrent.client.file.FileData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddressHandler {
    private static final Logger LOGGER = Logger.getLogger(AddressHandler.class.getName());

    private final FileData fileData;
    private final ServerConnection serverConnection;

    public AddressHandler(FileData fileData, ServerConnection serverConnection) {
        this.fileData = fileData;
        this.serverConnection = serverConnection;
        createLogHandler();
    }

    private void createLogHandler() {
        try {
            FileHandler handler = new FileHandler("logs/addresses.log");
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Creating log handler failed: "
                    + exception.getMessage(), exception);
        }
    }

    public String addressOf(String user) throws AddressException {
        synchronized (fileData) {
            try {
                BufferedReader fileReader = new BufferedReader(fileData.newReader());
                String address = fileReader
                        .lines()
                        .filter(s -> s.startsWith(user + ' '))
                        .findFirst().orElseThrow(() -> new AddressException("User not found!"))
                        .substring(user.length() + 3);
                fileReader.close();
                return address;
            } catch (IOException exception) {
                LOGGER.log(Level.SEVERE, "Address file reading failed: " + exception.getMessage(), exception);
                throw new AddressException(exception.getMessage(), exception);
            }
        }
    }

    public void update() throws IOException, AddressException {
        String addresses;
        try {
            addresses = serverConnection.sendMessage("list-addresses");
        } catch (ConnectionException e) {
            LOGGER.log(Level.SEVERE, "Getting addresses from server failed: " + e.getMessage(), e);
            throw new AddressException(e.getMessage(), e);
        }
        synchronized (fileData) {
            BufferedWriter fileWriter = new BufferedWriter(fileData.newWriter());
            fileWriter.write(addresses);
            fileWriter.flush();
            fileWriter.close();
        }
        LOGGER.info("Address file updated.");
    }
}
