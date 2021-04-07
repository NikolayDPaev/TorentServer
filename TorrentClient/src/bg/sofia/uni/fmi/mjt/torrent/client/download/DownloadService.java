package bg.sofia.uni.fmi.mjt.torrent.client.download;

import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressHandler;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ServerConnection;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DownloadService {
    private static final Logger LOGGER = Logger.getLogger(DownloadService.class.getName());

    private final AddressHandler addressHandler;
    private final ExecutorService executorService;

    private final ServerConnection serverConnection;
    private final String registerCommandPrefix;

    public DownloadService(AddressHandler addressHandler, ServerConnection serverConnection, String registerCommandPrefix) {
        this.serverConnection = serverConnection;
        this.registerCommandPrefix = registerCommandPrefix;
        this.addressHandler = addressHandler;
        executorService = Executors.newFixedThreadPool(5);
        createLogHandler();
    }

    private void createLogHandler() {
        try {
            FileHandler handler = new FileHandler("logs/downloads.log");
            handler.setLevel(Level.ALL);
            LOGGER.addHandler(handler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Creating log handler failed: "
                    + exception.getMessage(), exception);
        }
    }

    public void executeCommand(String command) throws DownloadException {
        DownloadData downloadData;
        try {
            downloadData = DownloadData.parseDownloadCommand(addressHandler, command);
        } catch (DownloadException e) {
            LOGGER.log(Level.SEVERE, "Parsing of the command failed: " + e.getMessage(), e);
            throw new DownloadException(e);
        }

        DownloadProcess downloadProcess =
                new DownloadProcess(downloadData, serverConnection, registerCommandPrefix);
        executorService.submit(downloadProcess);
        LOGGER.info("Download command executed.");
    }

    public void awaitAllSubmittedAndShutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ex) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Download service shut down.");
    }

    public void forceShutdown() {
        executorService.shutdownNow();
    }
}
