package bg.sofia.uni.fmi.mjt.torrent.client;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandUITest {
    private final String registerCommand = "register niki /home/img.jpg";
    private final String listFilesCommand = "list-files";
    private final String downloadCommand = "download ivan /home/img.jpg /home/img2.jpg";
    private final String disconnectCommand = "disconnect";

    @Mock
    private static ClientActions clientActions;

    private CommandUI commandUI;

    @BeforeClass
    public static void setUpMock() {
        clientActions = mock(ClientActions.class);
    }

    @Test
    public void testRun() {
        when(clientActions.start()).thenReturn("Connected to the server!");
        when(clientActions.register(registerCommand)).thenReturn("File(s) successfully registered!");
        when(clientActions.serverCommand(listFilesCommand))
                .thenReturn("niki : /home/img.jpg"
                        + System.lineSeparator()
                        + "ivan : /home/img.jpg");
        when(clientActions.download(downloadCommand)).thenReturn("Download request sent!");
        when(clientActions.stop()).thenReturn("Disconnected from the server!");

        final String input = registerCommand + System.lineSeparator()
                + listFilesCommand + System.lineSeparator()
                + downloadCommand + System.lineSeparator()
                + disconnectCommand + System.lineSeparator();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));

        commandUI = new CommandUI(inputStream, new PrintStream(outputStream));
        commandUI.run(clientActions);

        final String expectedOutput = "Connected to the server!" + System.lineSeparator()
                + "File(s) successfully registered!" + System.lineSeparator()
                + "niki : /home/img.jpg" + System.lineSeparator()
                + "ivan : /home/img.jpg" + System.lineSeparator()
                + "Download request sent!" + System.lineSeparator()
                + "Disconnected from the server!" + System.lineSeparator();

        assertArrayEquals("Testing output from the UI when the input commands are submitted",
                expectedOutput.getBytes(StandardCharsets.UTF_8), outputStream.toByteArray());
    }
}
