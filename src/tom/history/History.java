package tom.history;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * To use this class, is meant to be very simple. It is not the strongest or most
 * complex implementation of Undo and Redo, but it is capable. First you must
 * fully implement {@link Action} in a class. Once this class is implemented
 * (either anonymously or otherwise) it can be used for tom.history.History. Given
 * {@code tom.history.Action action = new tom.history.Action() { ... } }. First thing you will need to do
 * is register the tom.history.Action with the tom.history.History instance. This is done with
 * {@link #registerAction(Action)} by calling {@code tom.history.History.getInstance().registerAction(action);}
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
 * limit is reached (by default there is not limit, use {@link #setLimit(int)}
 * to define a limit on the number of undos and redos. [To be clear, the number
 * of redos is the same as undos and so the sum of all possible undos and redos
 * maybe greater than the limit]) then the stack will begin to empty from the
 * most chronologically distant end. Each time undo is called the action is added
 * to the redo stack and it may be redone by calling the {@link #redo()} method
 * which functions basically exactly like the undo method but in reverse. The
 * redo stack is cleared whenever an action is registered.
 * <p>
 * Finally, you can use {@link #registerUndoButton(Button)} and {@link #registerRedoButton(Button)}
 * methods to register with the tom.history.History class. This allows the history class
 * to maintain the state of these buttons in accordance with the size of the undo stack
 * and redo stack. It is not necessary that you use these methods or give the class buttons
 * This function is independent of all other functions and it is safe to ignore these
 * two methods and use the tom.history.History class in the same way as you would with the buttons
 * If you do use these methods then unod and redo will be disabled and enabled
 * according to when such actions are available to the user
 * <p>
 * Implementation note: While the data type collecting actions for undo and redo
 * is described as a stack it is actually implemented as a Double Ended Queue
 * (deque) in order to implement undo/redo size limits in an efficient way.
 * However, for all intents and purposes it is treated like a stack except for that.
 * <p>
 * Note also that tom.history.History is designed to be thread safe. Calling it from many threads
 * is acceptable as it has locking mechanisms in place.
 */
public class History {

  private LinkedBlockingDeque<Action> undoDeque = new LinkedBlockingDeque<>();
  private LinkedBlockingDeque<Action> redoDeque = new LinkedBlockingDeque<>();
  private int limit;
  private Lock lock = new ReentrantLock();
  private Button undoButton;
  private Button redoButton;

  public History() {
    this(-1, null, null);
  }

  public History(
      final int limit, @Nullable final Button undoButton,
      @Nullable final Button redoButton
  ) {
    this.limit = limit;
    this.undoButton = undoButton;
    this.redoButton = redoButton;
  }

  public History(final int limit) {
    this(limit, null, null);
  }

  public History(
      @Nullable final Button undoButton,
      @Nullable final Button redoButton
  ) {
    this(-1, undoButton, redoButton);
  }

  /**
   * Return the maximum possible number of undos and redos to store. Undos and
   * redos are counted independently and so the sum of allowed redos and undos
   * maybe greater than this but the total number of saved {@link Action}
   * instances is never more than twice this limit. Returns -1 if no
   * limit has been set by the user
   *
   * @return the maximum number of undos or redos allowed or -1 if such a limit
   * was never set
   */
  public int getLimit() {
    return limit;
  }

  /**
   * Sets the limit on the maximum number of undos and redo actions to store
   *
   * @param limit the limit of the number of undos and redos to store
   * @see #getLimit()
   */
  public void setLimit(final int limit) {
    if (limit == 0) {
      throw new IllegalArgumentException("Limit for History must be " +
                                         "either greater than 0 or -1 " +
                                         "for no limit");
    }
    this.limit = limit;
  }

  /**
   * Registers the button passed as the undo button of the program. By passing
   * this method a {@link Button} instance it will disable and enable that
   * button in accordance with the availability of the undo function within
   * the tom.history.History class itself. So, if there are no actions to undo, then
   * the undo button would be disabled and otherwise it would be enabled
   * <p>
   * Because {@link History#undo()} also throws, this binds the button to
   * {@link History#undo()} using {@link javafx.scene.Node#addEventHandler(EventType, EventHandler)}
   * so that the user does not have to
   *
   * @param button the button to be used as the undo button
   */
  public void registerAndBindUndoButton(@NotNull final Button button) {
    button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> History.this.undo());
    registerUndoButton(button);
  }

  /**
   * This method retrieves the most recently registered {@link Action} that
   * has not already been undone by this method and calls the {@link Action#undo()}
   * method on it
   * <p>
   * Note that this method calls {@link Lock#lock()} and will wait for all
   * other threads to not be undoing, redoing, or registering an action before
   * it registers the action
   *
   * @throws NothingToUndoException if the undo deque is empty and there is
   *                                nothing to undo
   */
  public void undo() throws NothingToUndoException {
    lock.lock();
    try {
      if (undoDeque.isEmpty()) {
        throw new NothingToUndoException("Undo deque is empty: Nothing to undo");
      }
      else {
        final Action a = undoDeque.pop();
        a.undo();
        if (limit > 0 && redoDeque.size() >= limit) {
          redoDeque.removeLast();
        }
        redoDeque.push(a);
        updateButtonsForUndo();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Registers the button passed as the undo button of the program. By passing
   * this method a {@link Button} instance it will disable and enable that
   * button in accordance with the availability of the undo function within
   * the tom.history.History class itself. So, if there are no actions to undo, then
   * the undo button would be disabled and otherwise it would be enabled
   *
   * @param button the button to be treated as the undo button by the tom.history.History
   *               class
   */
  private void registerUndoButton(@NotNull final Button button) {
    undoButton = button;
    updateButtonsForRegistration();
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

  private void updateButtonsForRegistration() {
    if (undoButton != null) {
      if (!undoDeque.isEmpty()) {
        undoButton.setDisable(false);
      }
      else {
        undoButton.setDisable(true);
      }
    }
    if (redoButton != null) {
      if (!redoDeque.isEmpty()) {
        redoButton.setDisable(false);
      }
      else {
        redoButton.setDisable(true);
      }
    }
  }

  /**
   * Performs the same task as {@link #registerAndBindUndoButton(Button)}
   *
   * @param button the redo button to bind and register
   * @see #registerAndBindUndoButton(Button)
   */
  public void registerAndBindRedoButton(@NotNull final Button button) {
    button.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> History.this.redo());
    registerRedoButton(button);
  }

  /**
   * This method retrieves the most recently undone {@link Action} that
   * has not already been redone by this method and calls the {@link Action#redo()}
   * method on it. Note that an action must be undone by calling
   * {@link #undo()} first. Note also, that any and every time a new action
   * is registered using either {@link #registerAction(Action)} or
   * {@link #registerActionAndExecute(Action)} all actions saved for redoing are
   * cleared. That is the redo stack is cleared by calling {@link LinkedBlockingDeque#clear()}
   * <p>
   * Note that this method calls {@link Lock#lock()} and will wait for all
   * other threads to not be undoing, redoing, or registering an action before
   * it registers the action
   *
   * @throws NothingToRedoException if the redo deque is empty and there is nothing to redo
   */
  public void redo() throws NothingToRedoException {
    lock.lock();
    try {
      if (redoDeque.isEmpty()) {
        throw new NothingToRedoException("Redo deque is empty: Nothing to redo");
      }
      else {
        final Action a = redoDeque.pop();
        a.redo();
        if (limit > 0 && undoDeque.size() >= limit) {
          undoDeque.removeLast();
        }
        undoDeque.push(a);
        updateButtonsForRedo();
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * Registers a button as the redo button. Performs the same tasks as
   * {@link #registerUndoButton(Button)}
   *
   * @param button
   * @see #registerUndoButton(Button)
   */
  private void registerRedoButton(@NotNull final Button button) {
    redoButton = button;
    updateButtonsForRegistration();
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

  /**
   * Stores the action in the stack of actions collecting all undo-able actions
   * and then calls the {@link Action#execute()} method of the action.
   * <p>
   * Note that this method calls {@link Lock#lock()} and will wait for all
   * other threads to not be undoing, redoing, or registering an action before
   * it registers the action
   *
   * @param action the action that will be registered and executed
   * @see #registerAction(Action)
   */
  public void registerActionAndExecute(@NotNull final Action action)
      throws Exception {
    lock.lock();
    try {
      action.execute();
      registerAction(action);
      updateButtonsForExecute();
    } catch (AbortActionException e) {
      action.rollback(e);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Stores the action in the stack of action collecting all the undo-able
   * actions
   * <p>
   * Note that this method calls {@link Lock#lock()} and will wait for all
   * other threads to not be undoing, redoing, or registering an action before
   * it registers the action
   *
   * @param action the action to be stored
   * @see #registerActionAndExecute(Action)
   */
  public void registerAction(@NotNull final Action action) {
    lock.lock();
    try {
      if (limit > 0 && undoDeque.size() >= limit) {
        undoDeque.removeLast();
      }
      undoDeque.push(action);
      redoDeque.clear();
      if (redoButton != null) {
        redoButton.setDisable(true);
      }
    } finally {
      lock.unlock();
    }
  }

  private void updateButtonsForExecute() {
    if (redoButton != null) {
      redoButton.setDisable(true);
      redoDeque.clear();
    }
    if (undoButton != null) {
      if (!undoDeque.isEmpty()) {
        undoButton.setDisable(false);
      }
    }
  }

  /**
   * Use this function to execute an action that has just been registered
   * If you choose to use the {@link #registerAction(Action)} method instead
   * of the {@link #registerActionAndExecute(Action)} method, you can call this
   * method after to execute the <strong>most recently registered action</strong>
   *
   * @throws Exception if the most recent action fails, and {@link Action#rollback(AbortActionException)}
   *                   throws an exception back up the call stack
   */
  public void executeMostRecentAction() throws Exception {
    lock.lock();
    try {
      undoDeque.peekFirst().execute();
      updateButtonsForExecute();
    } catch (AbortActionException e) {
      undoDeque.peekFirst().rollback(e);
    } finally {
      lock.unlock();
    }
  }

  public Collection<Action> undoDeque() {
    return Collections.unmodifiableCollection(undoDeque);
  }

  public Collection<Action> redoDeque() {
    return Collections.unmodifiableCollection(redoDeque);
  }
}
