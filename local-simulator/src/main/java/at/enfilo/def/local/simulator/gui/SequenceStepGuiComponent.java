package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.local.simulator.Simulator;
import at.enfilo.def.local.simulator.SimulatorConfiguration;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.api.exception.RoutineCreationException;
import at.enfilo.def.node.api.exception.RoutineExecutionException;
import at.enfilo.def.node.routine.factory.RoutineProcessBuilderFactory;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.transfer.dto.Language;
import at.enfilo.def.transfer.dto.RoutineBinaryDTO;
import at.enfilo.def.transfer.dto.RoutineDTO;
import at.enfilo.def.transfer.dto.TaskDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class SequenceStepGuiComponent extends GuiComponent {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Simulator.class);
	private static final String FXML_FILE = "sequence_step.fxml";

	@FXML private Label lblRoutineType;
	@FXML private Label lblRoutineName;
	@FXML private TextField txtRoutineId;
	@FXML private TextArea txtBinaries;
	@FXML private TextArea txtArguments;
	@FXML private TextField txtControl;
	@FXML private TextArea txtCommand;
	@FXML private TextField txtInPipe;
	@FXML private TextField txtOutPipe;
	@FXML private TextField txtCtrlPipe;
	@FXML private ComboBox<Language> cmbLanguage;
	@FXML private Button btnStart;
	@FXML private Button btnStop;
	@FXML private Button btnDetails;

	private StringProperty binaries;
	private StringProperty arguments;
	private StringProperty command;
	private StringProperty control;
	private StringProperty routineId;
	private StringProperty routineName;
	private StringProperty routineType;
	private StringProperty inPipe;
	private StringProperty outPipe;
	private StringProperty ctrlPipe;
	private ObjectProperty<Language> language;

	private final SequenceStep step;
	private final TaskDTO task;
	private final SimulatorConfiguration configuration;
	private RoutineDTO routine;
	private ProcessBuilder processBuilder;
	private Process process;

	public SequenceStepGuiComponent(SequenceStep step, TaskDTO task, SimulatorConfiguration configuration) {
		super(FXML_FILE);

		this.step = step;
		this.task = task;
		this.configuration = configuration;

		// Set Background according to Step-Type
		Background background;
		switch (step.getRoutineType()) {
			case OBJECTIVE:
				background = new Background(new BackgroundFill(Color.web("#ffeeaa"), CornerRadii.EMPTY, Insets.EMPTY));
				break;
			case MAP:
				background = new Background(new BackgroundFill(Color.web("#ffccaa"), CornerRadii.EMPTY, Insets.EMPTY));
				break;
			case STORE:
				background = new Background(new BackgroundFill(Color.web("#aaeeff"), CornerRadii.EMPTY, Insets.EMPTY));
				break;
			case CLIENT:
			case REDUCE:
			default:
				background = new Background(new BackgroundFill(Color.web("#ffffff"), CornerRadii.EMPTY, Insets.EMPTY));
		}
		setBackground(background);


		// Bindings
		binaries = new SimpleStringProperty();
		Bindings.bindBidirectional(txtBinaries.textProperty(), binaries);
		arguments = new SimpleStringProperty();
		Bindings.bindBidirectional(txtArguments.textProperty(), arguments);
		language = new SimpleObjectProperty<>();
		Bindings.bindBidirectional(cmbLanguage.valueProperty(), language);
		command = new SimpleStringProperty();
		Bindings.bindBidirectional(txtCommand.textProperty(), command);
		control = new SimpleStringProperty();
		Bindings.bindBidirectional(txtControl.textProperty(), control);
		routineId = new SimpleStringProperty();
		Bindings.bindBidirectional(txtRoutineId.textProperty(), routineId);
		routineName = new SimpleStringProperty();
		Bindings.bindBidirectional(lblRoutineName.textProperty(), routineName);
		routineType = new SimpleStringProperty();
		Bindings.bindBidirectional(lblRoutineType.textProperty(), routineType);
		inPipe = new SimpleStringProperty();
		Bindings.bindBidirectional(txtInPipe.textProperty(), inPipe);
		outPipe = new SimpleStringProperty();
		Bindings.bindBidirectional(txtOutPipe.textProperty(), outPipe);
		ctrlPipe = new SimpleStringProperty();
		Bindings.bindBidirectional(txtCtrlPipe.textProperty(), ctrlPipe);

		binaries.addListener((observable, oldValue, newValue) -> this.buildProcess());
		arguments.addListener((observable, oldValue, newValue) -> this.buildProcess());
		language.addListener((observable, oldValue, newValue) -> this.buildProcess());

		initValues();

		Executors.newSingleThreadExecutor().submit(this::fetchRoutine);
	}

	private void buildProcess() {
		// Working dir
		Path workingDir = Paths.get(".");

		// Routine
		if (language.getValue() == null) {
			return;
		}
		if (arguments.getValue() != null) {
			routine.setArguments(Arrays.asList(arguments.getValue().split("\n")));
		}
		if (binaries.getValue() != null) {
			Set<RoutineBinaryDTO> routineBinaries = new HashSet<>();
			for (String url : binaries.getValue().split("\n")) {
				RoutineBinaryDTO binary = new RoutineBinaryDTO();
				binary.setUrl(url);
				routineBinaries.add(binary);
			}
			routine.setRoutineBinaries(routineBinaries);
		}

		try {
			NodeConfiguration nodeConfig = NodeConfiguration.getDefault();
			nodeConfig.setMatlabRuntime(configuration.getMatlabRuntime());
			processBuilder = new RoutineProcessBuilderFactory(null, nodeConfig).build(
					workingDir,
					routine,
					step
			);

			command.setValue(processBuilder.command().stream().collect(Collectors.joining(" ")));

		} catch (RoutineCreationException | RoutineExecutionException e) {
			LOGGER.error("Error while build process.", e);
		}
	}

	private void fetchRoutine() {
		try {
			Future<RoutineDTO> routineFuture = Simulator.getInstance().getLibraryServiceClient().getRoutine(step.getRoutineId());
			routine = routineFuture.get();

		} catch (Exception e) {
			routine = new RoutineDTO();
			routine.setType(step.getRoutineType());
			routine.setId("<new>");
			routine.setName("<new>");
			routine.setArguments(Collections.singletonList("arg1"));
			routine.setRoutineBinaries(new HashSet<>());
			RoutineBinaryDTO routineBinaryDTO = new RoutineBinaryDTO();
			routineBinaryDTO.setUrl("file:/path/to/binary");
			routine.getRoutineBinaries().add(routineBinaryDTO);
		}

		Platform.runLater(this::updateData);
	}

	private void updateData() {
		// Set routine specific data
		routineId.set(routine.getId());
		routineName.set(routine.getName());
		binaries.set(routine.getRoutineBinaries()
				.stream()
				.map(RoutineBinaryDTO::getUrl)
				.collect(Collectors.joining("\n"))
		);
		arguments.set(routine.getArguments()
				.stream()
				.collect(Collectors.joining("\n"))
		);
		btnStart.setDisable(false);
	}

	private void initValues() {
		cmbLanguage.getItems().addAll(Language.values());
		routineType.set(String.format("(%s)", step.getRoutineType().name()));
		inPipe.set(step.getInPipe().resolve().toString());
		if (step.getOutPipe() != null) {
			outPipe.set(step.getOutPipe().resolve().toString());
		}
		ctrlPipe.set(step.getCtrlPipe().resolve().toString());
	}


	public void startProcess(ActionEvent e) {
		try {
			process = processBuilder.start();
			control.setValue("running");
			btnStart.setDisable(true);
			btnStop.setDisable(false);
			//btnDetails.setDisable(true);

			Executors.newSingleThreadExecutor().submit(() -> {
				int exitValue = 0;
				try {
					exitValue = process.waitFor();
				} catch (InterruptedException e1) {
					LOGGER.error("Interrupted.", e1);
					Thread.currentThread().interrupt();
				}
				if (exitValue == 0) {
					control.setValue("success");
					txtControl.setStyle("-fx-text-inner-color: green;");
				} else {
					control.setValue("error");
					txtControl.setStyle("-fx-text-inner-color: red;");
				}
				btnStart.setDisable(false);
				btnStop.setDisable(true);
				btnDetails.setDisable(false);
			});
		} catch (IOException e1) {
			LOGGER.error("Error while start process.", e1);
		}
	}

	public void stopProcess(ActionEvent e) {
		process.destroy();
	}

	public void showProcessDetails(ActionEvent e) {
		Alert popup = new Alert(Alert.AlertType.INFORMATION);
		popup.setTitle("Process Details");
		if (process.exitValue() == 0) {
			popup.setHeaderText("Returned successfully.");
		} else {
			popup.setHeaderText("Returned with errors.");
		}
		popup.setContentText(String.format("Exit-Value: %d", process.exitValue()));

		GridPane gridPane = new GridPane();
		Label lblStdOut = new Label("StdOut:");
		Label lblStdErr = new Label("StdErr:");
		TextArea txtStdOut = new TextArea();
		TextArea txtStdErr = new TextArea();
		gridPane.add(lblStdOut, 0, 0);
		gridPane.add(lblStdErr, 0, 1);
		gridPane.add(txtStdOut, 1, 0);
		gridPane.add(txtStdErr, 1, 1);
		gridPane.setMaxWidth(Double.MAX_VALUE);
		GridPane.setVgrow(txtStdOut, Priority.ALWAYS);
		GridPane.setHgrow(txtStdErr, Priority.ALWAYS);

		String buffer;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			while ((buffer = reader.readLine()) != null) {
				txtStdOut.appendText(buffer);
				txtStdOut.appendText("\n");
			}
		} catch (IOException e1) {
			LOGGER.error("Error while show process details.", e1);
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
			while ((buffer = reader.readLine()) != null) {
				txtStdErr.appendText(buffer);
				txtStdErr.appendText("\n");
			}
		} catch (IOException e1) {
			LOGGER.error("Error while show process details.", e1);
		}
		popup.getDialogPane().setExpandableContent(gridPane);
		popup.getDialogPane().expandedProperty().setValue(true);

		popup.showAndWait();
	}

}
