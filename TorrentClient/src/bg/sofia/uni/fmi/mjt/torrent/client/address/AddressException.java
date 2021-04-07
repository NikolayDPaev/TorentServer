package bg.sofia.uni.fmi.mjt.torrent.client.address;

import java.io.IOException;

public class AddressException extends Exception {
    public AddressException(String s, Exception e) {
        super(s, e);
    }

    public AddressException(String s) {
        super(s);
    }
}
