package bg.sofia.uni.fmi.mjt.torrent.client;

import bg.sofia.uni.fmi.mjt.torrent.client.file.FileData;
import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressHandler;
import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressUpdaterThread;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ServerConnection;
import bg.sofia.uni.fmi.mjt.torrent.client.download.DownloadService;
import bg.sofia.uni.fmi.mjt.torrent.client.miniserver.MiniServer;

public record ClientProperties(DownloadService downloadService,
                               MiniServer miniServer,
                               AddressUpdaterThread addressUpdaterThread,
                               ServerConnection serverConnection) {

    private static String getRegisterCommandPrefix(int userPort, String username) {
        return "register " + userPort + " " + username + " ";
    }

    public static ClientProperties setUpDependencies(String serverIp, int serverPort,
                                                     String addressFile, int userPort, String username) {
        ServerConnection serverConnection = new ServerConnection(serverIp, serverPort);

        AddressHandler addressHandler = new AddressHandler(new FileData(addressFile), serverConnection);
        AddressUpdaterThread addressUpdaterThread = new AddressUpdaterThread(addressHandler);
        addressUpdaterThread.setDaemon(true);

        DownloadService downloadService =
                new DownloadService(addressHandler, serverConnection, getRegisterCommandPrefix(userPort, username));
        MiniServer miniServer = new MiniServer(userPort);
        return new ClientProperties(downloadService, miniServer, addressUpdaterThread, serverConnection);
    }
}
