package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.transfer.dto.ExecutionState;
import at.enfilo.def.transfer.dto.JobDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.converter.NumberStringConverter;

import java.time.Instant;

public class JobGuiComponent extends GuiComponent {
	private static final String FXML_FILE = "job.fxml";

	@FXML private TextField txtJobId;
	@FXML private TextField txtMapRoutineId;
	@FXML private TextField txtReduceRoutineId;
	@FXML private TextField txtNumberOfTasks;
	@FXML private ComboBox<ExecutionState> cmbExecState;
	private ObjectProperty<ExecutionState> execState;

	public JobGuiComponent(JobDTO job) {
		super(FXML_FILE);

		StringProperty jobId = new SimpleStringProperty(job.getId());
		Bindings.bindBidirectional(txtJobId.textProperty(), jobId);

		StringProperty mapRoutineId = new SimpleStringProperty(job.getMapRoutineId());
		mapRoutineId.addListener((observable, oldValue, newValue) -> job.setMapRoutineId(newValue));
		Bindings.bindBidirectional(txtMapRoutineId.textProperty(), mapRoutineId);

		StringProperty reduceRoutineId = new SimpleStringProperty(job.getReduceRoutineId());
		reduceRoutineId.addListener((observable, oldValue, newValue) -> job.setReduceRoutineId(newValue));
		Bindings.bindBidirectional(txtReduceRoutineId.textProperty(), reduceRoutineId);

		IntegerProperty numberOfTasks = new SimpleIntegerProperty(job.getScheduledTasks());
		Bindings.bindBidirectional(txtNumberOfTasks.textProperty(), numberOfTasks, new NumberStringConverter());

		execState = new SimpleObjectProperty<>();
		Bindings.bindBidirectional(cmbExecState.valueProperty(), execState);

		initValues(job);
		execState.addListener((observable, oldValue, newValue) -> {
			job.setState(newValue);
			if (newValue == ExecutionState.RUN) {
				job.setStartTime(Instant.now().toEpochMilli());
			} else if (newValue == ExecutionState.FAILED || newValue == ExecutionState.SUCCESS) {
				job.setFinishTime(Instant.now().toEpochMilli());
			}
		});
	}

	private void initValues(JobDTO job) {
		cmbExecState.getItems().addAll(ExecutionState.values());
		cmbExecState.getSelectionModel().select(job.getState());
	}
}
