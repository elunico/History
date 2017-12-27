package tom.history;

/**
 * @author Thomas Povinelli
 * Created 2017-Dec-27
 * In History
 */
public class NothingToUndoException extends NoSuchActionException {
    public NothingToUndoException( ) {
        super( );
    }

    public NothingToUndoException( String message ) {
        super( message );
    }

    public NothingToUndoException( String message, Throwable cause ) {
        super( message, cause );
    }

    public NothingToUndoException( Throwable cause ) {
        super( cause );
    }

    public NothingToUndoException( String message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace )
    {
        super( message, cause, enableSuppression, writableStackTrace );
    }
}
