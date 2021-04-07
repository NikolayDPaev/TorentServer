package bg.sofia.uni.fmi.mjt.torrent.client.address;

import bg.sofia.uni.fmi.mjt.torrent.client.connection.ConnectionException;
import bg.sofia.uni.fmi.mjt.torrent.client.connection.ServerConnection;
import bg.sofia.uni.fmi.mjt.torrent.client.file.FileData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AddressHandlerTest {
    @Mock
    private FileData fileDataMock;
    @Mock
    private ServerConnection serverConnectionMock;

    private AddressHandler addressHandler;
    private static final String ADDRESSES = "niki - 127.0.0.1:1234" + System.lineSeparator()
                    + "kiko - 95.42.168.27:45876" + System.lineSeparator()
                    + "moni - 96.35.169.27:51245" + System.lineSeparator();

    @Before
    public void setUp() {
        fileDataMock = mock(FileData.class);
        serverConnectionMock = mock(ServerConnection.class);
        addressHandler = new AddressHandler(fileDataMock, serverConnectionMock);
    }

    @Test
    public void testUpdate() throws ConnectionException, IOException, AddressException {
        when(serverConnectionMock.sendMessage("list-addresses")).thenReturn(ADDRESSES);

        StringWriter stringWriter = new StringWriter();
        when(fileDataMock.newWriter()).thenReturn(stringWriter);

        addressHandler.update();

        assertEquals("Addresses written to the file must be equal to the submitted by the server",
                ADDRESSES, stringWriter.toString());

        verify(serverConnectionMock, times(1)).sendMessage("list-addresses");
        verify(fileDataMock, times(1)).newWriter();
    }

    @Test (expected = AddressException.class)
    public void testUpdateFailedConnection() throws ConnectionException, IOException, AddressException {
        doThrow(new ConnectionException("Failed connection to the server."))
                .when(serverConnectionMock).sendMessage("list-addresses");

        StringWriter stringWriter = new StringWriter();
        when(fileDataMock.newWriter()).thenReturn(stringWriter);

        addressHandler.update();

        verify(serverConnectionMock, times(1)).sendMessage("list-addresses");
        verify(fileDataMock, times(1)).newWriter();
    }

    @Test
    public void testAddressOf() throws IOException, AddressException {
        StringReader stringWriter = new StringReader(ADDRESSES);
        when(fileDataMock.newReader()).thenReturn(stringWriter);

        assertEquals("Method address of should correctly return address",
                "95.42.168.27:45876", addressHandler.addressOf("kiko"));

        verify(fileDataMock, times(1)).newReader();
    }

    @Test (expected = AddressException.class)
    public void testAddressOfNonexistentUser() throws IOException, AddressException {
        StringReader stringWriter = new StringReader(ADDRESSES);
        when(fileDataMock.newReader()).thenReturn(stringWriter);
        addressHandler.addressOf("petar");

        verify(fileDataMock, times(1)).newReader();
    }

    @Test (expected = AddressException.class)
    public void testAddressOfWithIOException() throws IOException, AddressException {
        doThrow(new IOException()).when(fileDataMock).newReader();
        addressHandler.addressOf("petar");

        verify(fileDataMock, times(1)).newReader();
    }
}
