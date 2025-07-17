package main;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class PlaceholderPage extends VBox {

    public PlaceholderPage(String message) {
        setAlignment(Pos.CENTER);
        setSpacing(15);
        setPrefSize(400, 300);

        Label label = new Label(message);
        getChildren().add(label);
    }
}
