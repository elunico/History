import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import tom.history.Action;
import tom.history.History;
import tom.utils.javafx.JavaFXUtilsKt;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Thomas Povinelli
 * Created 2/12/17
 * In UndoTest
 */

public class SimplePracticalTest extends Application {

    public final History history = new History();
    public Button redo = new Button("Redo");
    public Button undo = new Button("Undo");

    public static void main(String[] args) { launch(); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Label l = new Label();
        TextField t = new TextField();
        Button button = new Button("Do something");
        Button ccb = new Button("Change Color");
        Button test = new Button("Do Something else");
        Button dangerousButton = new Button("DO NOT CLICK ME");

        history.registerUndoButton(undo);
        history.registerRedoButton(redo);

        dangerousButton.setOnAction(event -> {
            Action a = new Action() {
                @Override
                public void execute() {
                    /* Do nothing */
                }

                @Override
                public void undo() {
                    // This action waits FOREVER when undoing
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void redo() {
                    execute();
                }
            };
            history.registerActionAndExecute(a);
        });

        test.setOnAction(event ->
        {
            Action a = new Action() {

                double oldHeight;
                double oldWidth;

                @Override
                public void execute()
                {
                    oldHeight = primaryStage.getHeight();
                    oldWidth = primaryStage.getWidth();

                    primaryStage.setHeight(oldHeight * 1.2);
                    primaryStage.setWidth(oldWidth * 1.2);
                }

                @Override
                public void undo()
                {
                    primaryStage.setWidth(oldWidth);
                    primaryStage.setHeight(oldHeight);
                    System.out.println(Arrays.toString(Thread.currentThread()
                                                             .getStackTrace()));

                }

                @Override
                public void redo()
                {
                    execute();
                }
            };
            history.registerActionAndExecute(a);
        });

        ccb.setOnAction(event ->
        {
            Action a = new Action() {

                private Paint oldColor;
                private Paint recolor;

                @Override
                public void execute()
                {
                    oldColor = l.getTextFill();
                    Random r = new Random();
                    l.setTextFill(
                      Color.color(r.nextDouble(), r.nextDouble(),
                        r.nextDouble()));
                    recolor = l.getTextFill();

                }

                @Override
                public void undo()
                {
                    l.setTextFill(oldColor);
                }

                @Override
                public void redo()
                {
                    l.setTextFill(recolor);
                }
            };
            history.registerActionAndExecute(a);

        });


        button.setOnAction(event ->
        {
            Action a = new Action() {

                private String previous;
                private String redoData;

                @Override
                public void execute()
                {
                    previous = l.getText();
                    String text = t.getText();
                    l.setText(l.getText() + "\n" + text);
                    t.clear();
                    t.requestFocus();
                    redoData = l.getText();
                }

                @Override
                public void undo()
                {
                    l.setText(previous);
                }

                public void redo()
                {
                    l.setText(redoData);
                }
            };
            history.registerActionAndExecute(a);
        });

        // ACQUIRES NO LOCK
        undo.setOnAction(event -> history.undo());

        redo.setOnAction(event -> history.redo());

        VBox b = new VBox(l);
        b.setPrefHeight(300);
        HBox buttonBox = new HBox(undo, redo, button, ccb, dangerousButton, test);
        VBox v = new VBox(b, t, buttonBox);

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.getCode() == KeyCode.EQUALS && event.isShiftDown()) {
                l.requestFocus();
                Action a = new Action() {

                    private double previousSize;

                    @Override
                    public void execute() {
                        previousSize = l.getFont().getSize();
                        l.setFont(Font.font(l.getFont().getFamily(),
                          previousSize * 1.25));
                    }

                    @Override
                    public void undo() {
                        l.setFont(Font.font(l.getFont()
                                             .getFamily(), previousSize));
                    }

                    @Override
                    public void redo() {
                        execute();
                    }
                };
                history.registerActionAndExecute(a);
            }

        });

        /*
        Enter being a filter results in new lines being appended to input
        Enter being a handler works as expected
         */
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event ->
        {
            if (event.getCode() == KeyCode.ENTER) {
                button.fire();
            }
        });

        /*
        Either of these methods being a handler renders them apparently ineffective
        however, as a filter they work properly
         */
        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (JavaFXUtilsKt.isUndoEvent(event)) {
                undo.fire();
            }
        });

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, event ->
        {
            if (JavaFXUtilsKt.isRedoEvent(event)) {
                redo.fire();
            }
        });

        primaryStage.setScene(new Scene(v));
        primaryStage.show();

    }
}
