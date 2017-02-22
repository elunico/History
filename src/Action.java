/**
 * This interface is used by {@link History} to implement undo and redo
 * This class functions <i>similar</i> to a functional interface except that
 * you must define 3 methods to define the behavior of execution (this method
 * is like the run method of a {@link Runnable} or the handle method of an
 * {@link java.beans.EventHandler}. It is the first method called and can even
 * be called in the History class using {@link History#registerActionAndExecute(Action)}
 * method. It defines the way to execute the Action for the first time
 *
 * The method {@link #undo()} defines how to undo the action. This often requires
 * adding fields to the implementing class to contain the data that needs to be
 * put back while undoing. This method is called by {@link History#undo()}.
 * {@link #redo()} defines how to redo this action.
 * While this may seem similar to (or even exactly the same as) the {@link #execute()}
 * method, it may also be different. This method is called by {@link History#redo()}.
 *
 * Take for example an Action which chooses a random number and sets the Text of
 * a label to that random number. Undoing this action would require you to have
 * stored the previous state of the Label before executing in a field in the class.
 * Likewise, redoing this would not involve choosing a random number but rather
 * putting back the random number chosen by execute the first time, which again
 * would require a field to hold the value.
 *
 * On the other hand, scaling a window by a factor, for instance, can be done
 * in execute and reversed in undo and in such a case redo, could simply call
 * execute.
 */
public interface Action {

    void execute();

    void undo();

    void redo();

}
