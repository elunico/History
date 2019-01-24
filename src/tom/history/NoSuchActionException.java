package tom.history;

/**
 * @author Thomas Povinelli
 * Created 2017-Dec-27
 * In History
 */
public abstract class NoSuchActionException extends RuntimeException {
  public NoSuchActionException() {
  }

  public NoSuchActionException(String message) {
    super(message);
  }

  public NoSuchActionException(String message, Throwable cause) {
    super(message, cause);
  }

  public NoSuchActionException(Throwable cause) {
    super(cause);
  }

  public NoSuchActionException(
      String message, Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace
  ) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
