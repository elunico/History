module tom.history {
    requires javafx.controls;
    requires annotations;

    opens tom.history to javafx.controls, javafx.base;

    exports tom.history;

}
