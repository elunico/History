import tom.history.Action;
import tom.history.History;

/**
 * @author Thomas Povinelli
 * Created 12/11/17
 * In UndoTest
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        History history = new History();

        Action a = new Action() {
            @Override
            public void execute() {
                System.out.println("Hello");
            }

            @Override
            public void undo() {
                System.out.println("Nvm");
            }

            @Override
            public void redo() {
                System.out.println("Hello, again!");
            }
        };


        history.registerActionAndExecute(a);
        Thread.sleep(3000);
        history.undo();
        Thread.sleep(3000);
        history.redo();
    }
}
