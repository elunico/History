import org.jetbrains.annotations.Nullable;
import tom.history.AbortActionException;
import tom.history.Action;
import tom.history.History;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * @author Thomas Povinelli
 * Created 2018-Jan-23
 * In History
 */
public class AllTest {

    public static void main(String[] args) throws Exception {
        History history = new History();

        Action a = new Action() {
            @Override
            public void execute( ) throws AbortActionException {
                System.out.println("Executing");
            }

            @Override
            public void undo( ) {
                System.out.println("Undoing....");
            }

            @Override
            public void redo( ) {

                System.out.println("Redoing....");
            }

            @Override
            public void rollback(@Nullable AbortActionException cause)
              throws Exception
            {
                System.out.println("Rolling back");
            }
        };
        Action a2 = new Action() {
            @Override
            public void execute( ) throws AbortActionException {
                System.out.println("Executing");
                throw new AbortActionException(new IOException("Test IO Exception"), new AbortActionException.Flags(2, "Second"));
            }

            @Override
            public void undo( ) {
                System.out.println("Undoing....");
            }

            @Override
            public void redo( ) {
                System.out.println("Redoing....");
            }

            @Override
            public void rollback(@Nullable AbortActionException cause)
              throws Exception
            {
                System.out.println("Rolling back");
                System.out.println("Caused by " + cause);
            }
        };
        Action a3 = new Action() {
            @Override
            public void execute( ) throws AbortActionException {
                System.out.println("Executing");
                throw new AbortActionException(new IOException("Test IO Exception"), new AbortActionException.Flags(3, "Third"));
            }

            @Override
            public void undo( ) {
                System.out.println("Undoing....");
            }

            @Override
            public void redo( ) {
                System.out.println("Redoing....");
            }

            @Override
            public void rollback(@Nullable AbortActionException cause)
              throws Exception
            {
                System.out.println("Rolling back");
                if (cause != null && cause.getCause() instanceof Exception) {
                    throw (Exception) cause.getCause();
                }
                Objects.requireNonNull(cause).flags().ifPresent(f -> {
                    if (f.getPoint() == 3) {
                        System.out.println("Hello");
                    }
                });
            }
        };

        Scanner scanner = new Scanner(System.in);
        char in;
        do {
            System.out.print("Enter action 1, 2, or 3 to execute or u, r for undo redo: ");
            in = scanner.nextLine().charAt(0);

            switch (in) {
                case '1':
                    history.registerActionAndExecute(a);
                    break;
                case '2':
                    history.registerActionAndExecute(a2);
                    break;
                case '3':
                    history.registerActionAndExecute(a3);
                    break;
                case 'u':
                    history.undo();
                    break;
                case 'r':
                    history.redo();
                    break;
                default:
                    break;
            }
        } while (in != 'q');
    }

}
