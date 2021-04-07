package bg.sofia.uni.fmi.mjt.torrent.client.download;

import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressException;
import bg.sofia.uni.fmi.mjt.torrent.client.address.AddressHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DownloadDataTest {
    @Mock
    private AddressHandler addressHandler;

    private static final String TEST_PATH_1 = "/home/pictures/img3456.jpg";
    private static final String TEST_PATH_2 = "D:\\Games\\Steam\\userdata.txt";

    @Before
    public void setAddressHandler() {
        addressHandler = mock(AddressHandler.class);
    }

    @Test (expected = DownloadException.class)
    public void testParseDownloadCommandWithAddressException() throws AddressException, DownloadException {
        doThrow(new AddressException("User not found!")).when(addressHandler).addressOf("moni");

        DownloadData.parseDownloadCommand(addressHandler, "download moni " + TEST_PATH_1 + " " + TEST_PATH_2);
    }

    @Test (expected = DownloadException.class)
    public void testParseDownloadCommandWithCorruptedAddress() throws AddressException, DownloadException {
        when(addressHandler.addressOf("moni")).thenReturn("96.35.169.27.51245");

        DownloadData.parseDownloadCommand(addressHandler, "download moni " + TEST_PATH_1 + " " + TEST_PATH_2);
    }

    @Test (expected = DownloadException.class)
    public void testParseDownloadCommandWithCorruptedPort() throws AddressException, DownloadException {
        when(addressHandler.addressOf("moni")).thenReturn("96.35.169.27:a51245");

        DownloadData.parseDownloadCommand(addressHandler, "download moni " + TEST_PATH_1 + " " + TEST_PATH_2);
    }

    @Test (expected = DownloadException.class)
    public void testParseDownloadCommandCorruptedCommand() throws AddressException, DownloadException {
        when(addressHandler.addressOf("moni")).thenReturn("96.35.169.27:51245");

        DownloadData.parseDownloadCommand(addressHandler, "download moni " + TEST_PATH_1);
    }

    @Test
    public void testParseDownloadCommand() throws AddressException, DownloadException {
        when(addressHandler.addressOf("moni")).thenReturn("96.35.169.27:51245");

        DownloadData expected = new DownloadData("96.35.169.27", 51245, TEST_PATH_1, TEST_PATH_2);

        String command = "download moni " + TEST_PATH_1 + " " + TEST_PATH_2;
        DownloadData actual = DownloadData.parseDownloadCommand(addressHandler, command);
        assertEquals("Download command must be parsed correctly", expected, actual);
    }
}
