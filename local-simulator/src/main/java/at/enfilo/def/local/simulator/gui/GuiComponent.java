package at.enfilo.def.local.simulator.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;

abstract class GuiComponent extends HBox {

	GuiComponent(final String fxmlFile) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource(fxmlFile));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
