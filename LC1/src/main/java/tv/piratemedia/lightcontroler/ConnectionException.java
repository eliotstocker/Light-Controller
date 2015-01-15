package tv.piratemedia.lightcontroler;

/**
 * Created by eliotstocker on 15/11/14.
 */
public class ConnectionException extends Exception {
    public static final int WIFI_NOT_CONNECTED = 802;
    public static final int CANT_GET_ADDRESS = 404;

    private int errorCode = 0;

    public ConnectionException(String message, int ErrorCode) {
        super(message);
        errorCode = ErrorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
