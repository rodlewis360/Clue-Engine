module org.goofy.clueengine {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive neuroph.core;
    requires visrec.api;


    opens org.goofy.clueengine to javafx.fxml;
    exports org.goofy.clueengine;
}