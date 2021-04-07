package bg.sofia.uni.fmi.mjt.torrent.client.address;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AddressUpdaterThread extends Thread {
    private static final Logger LOGGER = Logger.getLogger(AddressUpdaterThread.class.getName());
    private final AddressHandler addressHandler;

    public AddressUpdaterThread(AddressHandler addressHandler) {
        this.addressHandler = addressHandler;
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

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                addressHandler.update();
                Thread.sleep(30_000);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Writing to the address file failed: " + e.getMessage(), e);
        } catch (AddressException e) {
            LOGGER.log(Level.SEVERE, "Connecting to the server failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Sleeping interrupted: " + e.getMessage(), e);
            interrupt();
        }
    }
}
