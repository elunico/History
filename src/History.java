import javafx.scene.control.Button;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * History is a singleton class used to interface with Undo and Redo using
 * the {@link Action} interface. Getting the instance of History uses {@link #getInstance()}
 * and is the only way to interface with the History class since the constructor is
 * private and there are no other static methods
 * <p>
 * To use this class, is meant to be very simple. It is not the strongest or most
 * complex implementation of Undo and Redo, but it is capable. First you must
 * fully implement {@link Action} in a class. Once this class is implemented
 * (either anonymously or otherwise) it can be used for History. Given
 * {@code Action action = new Action() { ... } }. First thing you will need to do
 * is register the Action with the History instance. This is done with
 * {@link #registerAction(Action)} by calling {@code History.getInstance().registerAction(action);}
 * Once you do this you may call the {@link Action#execute()} {@code action.execute();} method
 * Alternatively you could call {@link #registerActionAndExecute(Action)} which
 * will register the action and call its execute method.
 * <p>
 * After you do this for every action that the user takes, a simple call to
 * {@link #undo()} will call the undo method of the most recently registered action
 * in a safe way. Subsequent calls to undo will continue to traverse backwards through
 * a stack of actions starting with the most recent ones until the stack is empty
 * <p>
 * The stack may reach an end in a number of ways. If the first action taken
 * since program inception is undone, then the stack will be empty. If the undo
 * limit is reached (by default there is not limit, use {@link #setActionLimit(int)}
 * to define a limit on the number of undos and redos. [To be clear, the number
 * of redos is the same as undos and so the sum of all possible undos and redos
 * maybe greater than the limit]) then the stack will begin to empty from the
 * most chronologically distant end. Each time undo is called the action is added
 * to the redo stack and it may be redone by calling the {@link #redo()} method
 * which functions basically exactly like the undo method but in reverse. The
 * redo stack is cleared whenever an action is registered.
 * <p>
 * Finally, you can use {@link #registerUndoButton(Button)} and {@link #registerRedoButton(Button)}
 * methods to register with the History class. This allows the history class
 * to maintain the state of these buttons in accordance with the size of the undo stack
 * and redo stack. It is not necessary that you use these methods or give the class buttons
 * This function is independent of all other functions and it is safe to ignore these
 * two methods and use the History class in the same way as you would with the buttons
 * If you do use these methods then unod and redo will be disabled and enabled
 * according to when such actions are available to the user
 * <p>
 * Implementation note: While the data type collecting actions for undo and redo
 * is described as a stack it is actually implemented as a Double Ended Queue
 * (deque) in order to implement undo/redo size limits in an efficient way.
 * However, for all intents and purposes it is treated like a stack except for that.
 * <p>
 * Note also that History is designed to be thread safe. Calling it from many threads
 * is acceptable as it has locking mechanisms in place.
 */
public class History {

    private static History instance;
    private static Lock instanceLock = new ReentrantLock();
    private int limit = -1;
    private LinkedBlockingDeque<Action> undoDeque = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<Action> redoDeque = new LinkedBlockingDeque<>();
    private Lock lock = new ReentrantLock();
    private Button undoButton;
    private Button redoButton;

    private History() {
    }

    public static History getInstance()
    {
        if (instance == null) {
            instanceLock.lock();
            try {
                if (instance != null) {
                    return instance;
                }
                instance = new History();
            } finally {
                instanceLock.unlock();
            }
        }
        return instance;
    }

    public int getLimit() {
        return limit;
    }

    public void setActionLimit(int limit) {
        this.limit = limit;
    }

    public void registerUndoButton(Button button)
    {
        undoButton = button;
    }

    public void registerRedoButton(Button button)
    {
        redoButton = button;
    }

    public void registerActionAndExecute(Action action) {
        lock.lock();
        try {
            registerAction(action);
            action.execute();
        } finally {
            lock.unlock();
        }

    }

    public void registerAction(Action action) {
        lock.lock();
        try {
            if (limit > 0 && undoDeque.size() >= limit) {
                undoDeque.removeLast();
            }
            undoDeque.push(action);
            redoDeque.clear();
            redoButton.setDisable(true);
        } finally {
            lock.unlock();
        }
    }

    public boolean undo() {
        lock.lock();
        try {
            if (undoDeque.isEmpty()) {
                return false;
            } else {
                Action a = undoDeque.pop();
                a.undo();
                if (limit > 0 && redoDeque.size() >= limit) {
                    redoDeque.removeLast();
                }
                redoDeque.push(a);
                updateButtonsForUndo();
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateButtonsForUndo() {
        if (undoDeque.isEmpty()) {
            if (undoButton != null) {
                undoButton.setDisable(true);
            }
        }
        if (redoButton != null) {
            redoButton.setDisable(false);
        }
    }

    public boolean redo() {
        lock.lock();
        try {
            if (redoDeque.isEmpty()) {
                return false;
            } else {
                Action a = redoDeque.pop();
                a.redo();
                if (limit > 0 && undoDeque.size() >= limit) {
                    undoDeque.removeLast();
                }
                undoDeque.push(a);
                updateButtonsForRedo();
                return true;
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateButtonsForRedo() {
        if (redoDeque.isEmpty()) {
            if (redoButton != null) {
                redoButton.setDisable(true);
            }
        }
        if (undoButton != null) {
            undoButton.setDisable(false);
        }
    }
}
