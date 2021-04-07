package bg.sofia.uni.fmi.mjt.torrent.client;

import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressUpdaterThread;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ConnectionException;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ServerConnection;
import bg.sofia.uni.fmi.mjt.torrent.client.download.DownloadException;
import bg.sofia.uni.fmi.mjt.torrent.client.download.DownloadService;
import bg.sofia.uni.fmi.mjt.torrent.client.miniserver.MiniServer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClientActionsTest {
    @Mock
    private static MiniServer miniServer;
    @Mock
    private static AddressUpdaterThread addressUpdaterThread;
    @Mock
    private static ServerConnection serverConnection;
    @Mock
    private static DownloadService downloadService;
    private static ClientProperties clientProperties;

    private ClientActions clientActions;

    @BeforeClass
    public static void setMocks() {
        miniServer = mock(MiniServer.class);
        addressUpdaterThread = mock(AddressUpdaterThread.class);
        serverConnection = mock(ServerConnection.class);
        downloadService = mock(DownloadService.class);
        clientProperties = new ClientProperties(downloadService, miniServer, addressUpdaterThread, serverConnection);
    }

    @Before
    public void setClientActions() {
        clientActions = new ClientActions(clientProperties, 1234);
    }

    @Test
    public void testStart() throws ConnectionException {
        assertEquals("Testing reply when connection is successful.",
                "Connected to the server!", clientActions.start());

        verify(miniServer, times(1)).start();
        verify(serverConnection, times(1)).connect();
        verify(addressUpdaterThread, times(1)).start();
    }

    @Test
    public void testStartWhenConnectionFailed() throws ConnectionException {
        doThrow(new ConnectionException("Server inaccessible!")).when(serverConnection).connect();
        assertEquals("Method must provide suitable message when connection failed.",
                "Connection to the server failed!", clientActions.start());
    }

    @Test
    public void testStop() throws ConnectionException {
        assertEquals("Testing reply when connection is closed successfully.",
                "Disconnected from the server!", clientActions.stop());

        verify(miniServer, times(1)).close();
        verify(serverConnection, times(1)).close();
    }

    @Test
    public void testStopWhenConnectionFailed() throws ConnectionException {
        doThrow(new ConnectionException("Server inaccessible!")).when(serverConnection).close();
        assertEquals("Method must provide suitable message when disconnect fails.",
                "Disconnecting from the server failed!", clientActions.stop());
    }

    @Test
    public void testDownload() throws DownloadException {
        final String command = "download niki /home/img.jpg /home/videos/party.mp4";
        assertEquals("Testing reply when download is submitted.",
                "Download request sent!", clientActions.download(command));

        verify(downloadService).executeCommand(command);
    }

    @Test
    public void testDownloadWhenFileNotFound() throws DownloadException {
        final String command = "download niki /home/img.jpg /home/pictures/img.jpg";
        doThrow(new DownloadException("File not found!")).when(downloadService).executeCommand(command);

        assertEquals("Testing reply when user not found.",
                "File not found!", clientActions.download(command));
    }

    @Test
    public void testServerCommand() throws ConnectionException {
        final String command = "list-files";
        final String reply = "niki : /home/pictures/img.jpg";

        when(serverConnection.sendMessage(command)).thenReturn(reply);
        assertEquals("Testing reply when other command is submitted",
                reply, clientActions.serverCommand(command));

        verify(serverConnection, times(1)).sendMessage(command);
    }

    @Test
    public void testServerCommandWhenConnectionFailed() throws ConnectionException {
        final String command = "list-files";
        doThrow(new ConnectionException("Connection failed!")).when(serverConnection).sendMessage(command);

        assertEquals("Testing reply when command is submit fails",
                "Connection failed!", clientActions.serverCommand(command));
    }

    @Test
    public void register() throws ConnectionException {
        final String command = "register niki /home/pictures/img.jpg";
        final String modifiedCommand = "register 1234 niki /home/pictures/img.jpg";

        when(serverConnection.sendMessage(modifiedCommand)).thenReturn("File(s) successfully registered!");

        assertEquals("Testing reply when register command is submitted",
                "File(s) successfully registered!", clientActions.register(command));
        verify(serverConnection, times(1)).sendMessage(modifiedCommand);
    }
}
