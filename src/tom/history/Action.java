package tom.history;

/**
 * This interface is used by {@link History} to implement undo and redo
 * This class functions <i>similar</i> to a functional interface except that
 * you must define 3 methods to define the behavior of execution (this method
 * is like the run method of a {@link Runnable} or the handle method of an
 * {@link java.beans.EventHandler}. It is the first method called and can even
 * be called in the tom.history.History class using {@link History#registerActionAndExecute(Action)}
 * method. It defines the way to execute the tom.history.Action for the first time
 * <p>
 * The method {@link #undo()} defines how to undo the action. This often requires
 * adding fields to the implementing class to contain the data that needs to be
 * put back while undoing. This method is called by {@link History#undo()}.
 * {@link #redo()} defines how to redo this action.
 * While this may seem similar to (or even exactly the same as) the {@link #execute()}
 * method, it may also be different. This method is called by {@link History#redo()}.
 * <p>
 * Take for example an tom.history.Action which chooses a random number and sets the Text of
 * a label to that random number. Undoing this action would require you to have
 * stored the previous state of the Label before executing in a field in the class.
 * Likewise, redoing this would not involve choosing a random number but rather
 * putting back the random number chosen by execute the first time, which again
 * would require a field to hold the value.
 * <p>
 * On the other hand, scaling a window by a factor, for instance, can be done
 * in execute and reversed in undo and in such a case redo, could simply call
 * execute.
 */
public interface Action {

    /**
     * Called to execute the action. It is called when the tom.history.Action is to proceed
     * or take place for the first time. Also called by {@link History#registerActionAndExecute(Action)}
     * While this method need not check to ensure, the execute method should only
     * be called in code once and then {@link #undo()} and {@link #redo()} should
     * be used to move back and forth. Still, the redo method may, in cases where
     * it is logically sound to do so, consist of only a single line: a call to
     * execute
     */
    void execute( );

    /**
     * Called in order to undo the action, restoring all things to their
     * states as they were before the call to {@link #execute()}. While this
     * method does no checking to ensure execute was called first, it is logically
     * senseless to use this method before execute.
     * <p>
     * Also called by {@link History#undo()}
     */
    void undo( );

    /**
     * Called in order to redo the action. This can sometimes be implemented
     * as a simple call to {@link #execute()} but sometimes requires more processing
     * This method should only be called following a call to execute and subsequent
     * call to {@link #undo()}
     * <p>
     * Also called by {@link History#redo()}
     *
     * @see History
     */
    void redo( );
}
