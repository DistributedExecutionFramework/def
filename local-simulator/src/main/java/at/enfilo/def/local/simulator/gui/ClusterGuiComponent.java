package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.transfer.dto.ClusterInfoDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class ClusterGuiComponent extends GuiComponent {
	private static final String FXML_FILE = "cluster.fxml";

	@FXML private TextField txtClusterId;
	@FXML private TextField txtStoreRoutineId;
	@FXML private TextField txtDefaultMapRoutineId;

	public ClusterGuiComponent(ClusterInfoDTO cluster) {
		super(FXML_FILE);

		if (cluster == null) {
			return;
		}

		StringProperty clusterId = new SimpleStringProperty(cluster.getId());
		Bindings.bindBidirectional(txtClusterId.textProperty(), clusterId);

		StringProperty storeRoutineId = new SimpleStringProperty(cluster.getStoreRoutineId());
		storeRoutineId.addListener((observable, oldValue, newValue) -> cluster.setStoreRoutineId(newValue));
		Bindings.bindBidirectional(txtStoreRoutineId.textProperty(), storeRoutineId);

		StringProperty defaultMapRoutineId = new SimpleStringProperty(cluster.getDefaultMapRoutineId());
		storeRoutineId.addListener((observable, oldValue, newValue) -> cluster.setDefaultMapRoutineId(newValue));
		Bindings.bindBidirectional(txtDefaultMapRoutineId.textProperty(), defaultMapRoutineId);
	}
}
