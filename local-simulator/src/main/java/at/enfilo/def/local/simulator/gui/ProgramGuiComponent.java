package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.time.Instant;

public class ProgramGuiComponent extends GuiComponent {
	private static final String FXML_FILE = "program.fxml";

	@FXML private TextField txtProgramId;
	@FXML private ComboBox<ExecutionState> cmbExecState;
	private ObjectProperty<ExecutionState> execState;

	public ProgramGuiComponent(ProgramDTO program) {
		super(FXML_FILE);

		StringProperty programId = new SimpleStringProperty(program.getId());
		Bindings.bindBidirectional(txtProgramId.textProperty(), programId);

		execState = new SimpleObjectProperty<>();
		Bindings.bindBidirectional(cmbExecState.valueProperty(), execState);

		initValues(program);
		execState.addListener((observable, oldValue, newValue) -> {
			if (newValue == ExecutionState.SUCCESS || newValue == ExecutionState.FAILED) {
				program.setFinishTime(Instant.now().toEpochMilli());
			}
			program.setState(newValue);
		});
	}

	private void initValues(ProgramDTO program) {
		cmbExecState.getItems().addAll(ExecutionState.values());
		cmbExecState.getSelectionModel().select(program.getState());
	}
}
