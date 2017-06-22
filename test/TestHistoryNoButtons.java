import org.junit.Test;
import tom.history.Action;
import tom.history.History;

/**
 * @author Thomas Povinelli
 *         Created 6/20/17
 *         In UndoTest
 */
public class TestHistoryNoButtons {

    @Test
    public void testHistoryInstantiation() throws InterruptedException {
        History history = new History();
        History history1 = new History();

        history.registerAction(new Action() {
            @Override
            public void execute() {
                System.out.println("Hello");
            }

            @Override
            public void undo() {
                System.out.println("Whoops");
            }

            @Override
            public void redo() {
                System.out.println("No you know what that works");
            }
        });

        history.executeMostRecentAction();
        Thread.sleep(1500);
        history.undo();
        Thread.sleep(1500);
        history.redo();
    }
}
