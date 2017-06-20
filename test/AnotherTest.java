import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tom.history.Action;
import tom.history.History;

/**
 * @author Thomas Povinelli
 *         Created 4/29/17
 *         In UndoTest
 */
public class AnotherTest extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FlowPane main = new FlowPane();

        Button b = new Button("Click Me");
        Button undo = new Button("undo");
        Button redo = new Button("redo");

        History.getInstance().registerUndoButton(undo);
        History.getInstance().registerRedoButton(redo);

        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Action a = new Action() {

                    String old;
                    String neue;

                    @Override
                    public void execute() {
                        old = b.getText();
                        b.setText(b.getText() + " clicked");
                    }

                    @Override
                    public void undo() {
                        neue = b.getText();
                        b.setText(old);
                    }

                    @Override
                    public void redo() {
                        old = b.getText();
                        b.setText(neue);
                    }
                };

                History.getInstance().registerActionAndExecute(a);

            }
        });

        main.getChildren().addAll(b, undo, redo);

        primaryStage.setScene(new Scene(main));
        primaryStage.show();

    }
}
