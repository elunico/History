package tom.history;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * @author Thomas Povinelli
 * Created 2018-Jan-23
 * In History
 */
public class AbortActionException extends Exception {

    public static class Flags {
        private final int point;
        private final String pointName;

        public Flags(int point, String pointName) {
            this.point = point;
            this.pointName = pointName;
        }

        public int getPoint( ) {
            return point;
        }

        public String getPointName( ) {
            return pointName;
        }
    }

    private final Flags theFlags;

    public AbortActionException(String msg, Throwable cause,
                                @Nullable Flags flags)
    {
        super(msg, cause);
        this.theFlags = flags;
    }

    @NotNull
    public Optional<Flags> flags( ) {
        return Optional.ofNullable(theFlags);
    }

    @Nullable
    public Flags getFlags() {
        return theFlags;
    }

    public AbortActionException(Throwable cause, @Nullable Flags flags) {
        super(cause);
        this.theFlags = flags;
    }

}
