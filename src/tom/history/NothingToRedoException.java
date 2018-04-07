package tom.history;

/**
 * @author Thomas Povinelli
 * Created 2017-Dec-27
 * In History
 */
public class NothingToRedoException extends NoSuchActionException {
    public NothingToRedoException( ) {
        super();
    }

    public NothingToRedoException(String message) {
        super(message);
    }

    public NothingToRedoException(String message, Throwable cause) {
        super(message, cause);
    }

    public NothingToRedoException(Throwable cause) {
        super(cause);
    }

    public NothingToRedoException(String message, Throwable cause,
                                  boolean enableSuppression,
                                  boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
