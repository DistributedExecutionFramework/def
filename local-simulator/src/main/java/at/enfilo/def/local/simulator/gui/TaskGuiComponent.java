package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.common.api.IThrowingConsumer;
import at.enfilo.def.datatype.*;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.local.simulator.Simulator;
import at.enfilo.def.local.simulator.SimulatorConfiguration;
import at.enfilo.def.local.simulator.SimulatorExecLogicController;
import at.enfilo.def.logging.api.IDEFLogger;
import at.enfilo.def.logging.impl.DEFLoggerFactory;
import at.enfilo.def.node.routine.exec.RoutinesCommunicator;
import at.enfilo.def.node.routine.exec.SequenceStep;
import at.enfilo.def.node.routine.exec.SequenceStepsBuilder;
import at.enfilo.def.node.util.NodeConfiguration;
import at.enfilo.def.routine.api.Result;
import at.enfilo.def.routine.factory.NamedPipeFactory;
import at.enfilo.def.routine.util.Pipe;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownTaskException;
import at.enfilo.def.transfer.dto.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TaskGuiComponent extends GuiComponent {
	private static final IDEFLogger LOGGER = DEFLoggerFactory.getLogger(Simulator.class);
	private static final String FXML_FILE = "task.fxml";

	public static class InParameter {
		private final StringProperty name;
		private final StringProperty dataType;
		private final StringProperty data;

		public InParameter(StringProperty name, StringProperty dataType, StringProperty data) {
			this.name = name;
			this.dataType = dataType;
			this.data = data;
		}

		public String getName() {
			return name.get();
		}

		public StringProperty nameProperty() {
			return name;
		}

		public String getDataType() {
			return dataType.get();
		}

		public StringProperty dataTypeProperty() {
			return dataType;
		}

		public Object getData() {
			return data.get();
		}

		public StringProperty dataProperty() {
			return data;
		}
	}

	public static class OutParameter {
		private final StringProperty key;
		private final BooleanProperty shared;
		private final StringProperty url;
		private final StringProperty data;

		public OutParameter(StringProperty key, BooleanProperty shared, StringProperty url, StringProperty data) {
			this.key = key;
			this.shared = shared;
			this.url = url;
			this.data = data;
		}

		public String getKey() {
			return key.getValue();
		}

		public StringProperty keyProperty() {
			return key;
		}

		public String getUrl() {
			return url.getValue();
		}

		public StringProperty urlProperty() {
			return url;
		}

		public String getData() {
			return data.getValue();
		}

		public StringProperty dataProperty() {
			return data;
		}

		public Boolean getShared() {
			return shared.getValue();
		}

		public BooleanProperty sharedProperty() {
			return shared;
		}
	}

	@FXML private TextField txtTaskId;
	@FXML private TableView<InParameter> tblInParameters;
	@FXML private TableColumn colInParamName;
	@FXML private TableColumn colInParamDataType;
	@FXML private TableColumn colInParamData;
	@FXML private TableView<OutParameter> tblOutParameters;
	@FXML private TableColumn colOutParamKey;
	@FXML private TableColumn colOutParamShared;
	@FXML private TableColumn colOutParamUrl;
	@FXML private TableColumn colOutParamData;
	@FXML private VBox vbxSequence;
	@FXML private Button btnStart;
	@FXML private Button btnStop;
	@FXML private ComboBox<ExecutionState> cmbExecState;
	private ObjectProperty<ExecutionState> execState;

	private final TaskDTO task;
	private final SimulatorExecLogicController simulatorExecLogicController;

	private List<SequenceStep> steps;
	private ObservableList<InParameter> inParametersData;
	private ObservableList<OutParameter> outParametersData;
	private RoutinesCommunicator routinesCommunicator;
	private SimulatorConfiguration configuration;

	public TaskGuiComponent(
			SimulatorExecLogicController simulatorExecLogicController,
			String pId,
			String jId,
			String tId
	) throws UnknownProgramException, ExecLogicException, UnknownJobException, UnknownTaskException {
		super(FXML_FILE);

		this.simulatorExecLogicController = simulatorExecLogicController;
		this.configuration = simulatorExecLogicController.getConfiguration();
		this.task = simulatorExecLogicController.getTask(pId, jId, tId);

		inParametersData = FXCollections.observableArrayList();
		outParametersData = FXCollections.observableArrayList();
		tblInParameters.setItems(inParametersData);
		tblOutParameters.setItems(outParametersData);

		colInParamName.setCellValueFactory(new PropertyValueFactory<InParameter, String>("name"));
		colInParamDataType.setCellValueFactory(new PropertyValueFactory<InParameter, String>("dataType"));
		colInParamData.setCellValueFactory(new PropertyValueFactory<InParameter, String>("data"));
		colOutParamKey.setCellValueFactory(new PropertyValueFactory<OutParameter, String>("key"));
		colOutParamShared.setCellValueFactory(new PropertyValueFactory<OutParameter, Boolean>("shared"));
		colOutParamUrl.setCellValueFactory(new PropertyValueFactory<OutParameter, String>("url"));
		colOutParamData.setCellValueFactory(new PropertyValueFactory<OutParameter, String>("data"));

		// Bindings
		StringProperty taskId = new SimpleStringProperty(task.getId());
		Bindings.bindBidirectional(txtTaskId.textProperty(), taskId);

		Executors.newSingleThreadExecutor().submit(this::showInParameters);
		Executors.newSingleThreadExecutor().submit(this::createAndShowSteps);

		execState = new SimpleObjectProperty<>();
		Bindings.bindBidirectional(cmbExecState.valueProperty(), execState);

		initValues(task);
		execState.addListener((observable, oldValue, newValue) -> {
			try {
				JobDTO job = simulatorExecLogicController.getJob(pId, jId);
				task.setState(newValue);
				switch (newValue) {
					case SCHEDULED:
						task.setStartTime(0);
						task.setFinishTime(0);
						job.setScheduledTasks(job.getScheduledTasks() + 1);
						break;
					case FAILED:
						task.setFinishTime(Instant.now().toEpochMilli());
						job.setFailedTasks(job.getFailedTasks() + 1);
						break;
					case SUCCESS:
						task.setFinishTime(Instant.now().toEpochMilli());
						job.setSuccessfulTasks(job.getSuccessfulTasks() + 1);
						break;
					case RUN:
						task.setStartTime(Instant.now().toEpochMilli());
						job.setRunningTasks(job.getRunningTasks() + 1);
						break;
				}

				switch (oldValue) {
					case SCHEDULED:
						job.setScheduledTasks(job.getScheduledTasks() - 1);
						break;
					case FAILED:
						job.setFailedTasks(job.getFailedTasks() - 1);
						break;
					case SUCCESS:
						job.setSuccessfulTasks(job.getSuccessfulTasks() - 1);
						break;
					case RUN:
						job.setRunningTasks(job.getRunningTasks() - 1);
						break;
				}
			} catch (ExecLogicException | UnknownProgramException | UnknownJobException e) {
				e.printStackTrace();
			}
		});
	}

	private void createAndShowSteps() {
		NodeConfiguration nodeConfiguration = NodeConfiguration.getDefault();
		nodeConfiguration.setWorkingDir(simulatorExecLogicController.getConfiguration().getWorkingDir());
		steps = new SequenceStepsBuilder(task.getId(), nodeConfiguration)
				.appendStep(task.getObjectiveRoutineId(), RoutineType.OBJECTIVE)
				.appendStep(task.getMapRoutineId(), RoutineType.MAP)
				.appendStep(simulatorExecLogicController.getClusterInfo().getStoreRoutineId(), RoutineType.STORE)
				.getSequence();

		for (SequenceStep step : steps) {
			SequenceStepGuiComponent sequenceStepGuiComponent = new SequenceStepGuiComponent(step, task, configuration);
			Platform.runLater(
					() -> vbxSequence.getChildren().add(sequenceStepGuiComponent)
			);
		}

	}


	private void showInParameters() {
		task.getInParameters().forEach(
				(name, resource) -> {
					SimpleStringProperty data = null;
					TDeserializer deserializer = new TDeserializer();
					try {
						switch (resource.getDataTypeId()) {
							case "13557af3-2524-3252-9b65-f288b64d922b":
								DEFBoolean b = new DEFBoolean();
								deserializer.deserialize(b, resource.data.array());
								data = new SimpleStringProperty(Boolean.toString(b.isValue()));
								break;
							case "6389a2fb-eace-310b-b178-9c4d7b1daaa0":
								DEFInteger i = new DEFInteger();
								deserializer.deserialize(i, resource.data.array());
								data = new SimpleStringProperty(Integer.toString(i.getValue()));
								break;
							case "5fb4621b-8de1-39f8-b282-9108cfe2adc0":
								DEFLong l = new DEFLong();
								deserializer.deserialize(l, resource.data.array());
								data = new SimpleStringProperty(Long.toString(l.getValue()));
								break;
							case "6e8d4e97-38f8-31df-887d-8b193c2e50b3":
								DEFDouble d = new DEFDouble();
								deserializer.deserialize(d, resource.data.array());
								data = new SimpleStringProperty(Double.toString(d.getValue()));
								break;
							case "b5f087fc-e8b3-3e2d-9e46-7492c2cb36cf":
								DEFString s = new DEFString();
								deserializer.deserialize(s, resource.data.array());
								data = new SimpleStringProperty(s.getValue());
								break;
							default:
								data = new SimpleStringProperty(Base64.getEncoder().encodeToString(resource.data.array()));
								break;
						}
					} catch (TException e) {
						LOGGER.error("Error while show inParams.", e);
					}
					InParameter inParameter = new InParameter(
							new SimpleStringProperty(name),
							new SimpleStringProperty(resource.getDataTypeId()),
							data
					);
					Platform.runLater(
							() -> inParametersData.add(inParameter)
					);
				}
		);
	}


	public void startCommunicator(ActionEvent e) {
		// Create pipes
		steps.forEach(
				(IThrowingConsumer<SequenceStep>) step -> {
					NamedPipeFactory.createPipe(step.getInPipe().resolve());
					NamedPipeFactory.createPipe(step.getCtrlPipe().resolve());
					if (step.hasOutPipe()) {
						NamedPipeFactory.createPipe(step.getOutPipe().resolve());
					}
				}
		);

		// Start communicator
		Pipe outPipe = steps.get(0).getInPipe();
		List<Pipe> ctrlPipes = steps.stream().map(SequenceStep::getCtrlPipe).collect(Collectors.toList());

		routinesCommunicator = new RoutinesCommunicator(
				task.getInParameters(),
				false,
				outPipe,
				ctrlPipes,
				DEFLoggerFactory.createTaskContext(task.getId())
		);
		Thread tRoutinesCommunicator = new Thread(routinesCommunicator);
		tRoutinesCommunicator.start();

		btnStart.setDisable(true);
		btnStop.setDisable(false);

		// Wait for RoutineCommunicator and show results
		Executors.newSingleThreadExecutor().submit(() -> {
			try {
				tRoutinesCommunicator.join();
				task.setState(ExecutionState.SUCCESS);
				simulatorExecLogicController.notifyTaskDone(task.getJobId(), task.getId());

				Platform.runLater(() -> {
					btnStart.setDisable(false);
					btnStop.setDisable(true);

					for (Result result : routinesCommunicator.getResults()) {
						ResourceDTO resultResource = new ResourceDTO();
						resultResource.setKey(result.getKey());
						if (result.isSetUrl() && !result.getUrl().isEmpty()) {
							resultResource.setUrl(result.getUrl());
						} else {
							resultResource.setData(result.getData());
						}
						task.addToOutParameters(resultResource);
						OutParameter outParameter = new OutParameter(
								new SimpleStringProperty(resultResource.getKey()),
								new SimpleBooleanProperty(),
								new SimpleStringProperty(resultResource.getUrl()),
								new SimpleStringProperty(Base64.getEncoder().encodeToString(resultResource.data.array()))
						);
						outParametersData.add(outParameter);
					}
				}
				);

			} catch (InterruptedException e1) {
				LOGGER.error("Interrupted.", e1);
				Thread.currentThread().interrupt();
			}
		});
	}

	public void stopCommunicator(ActionEvent e) {
		if (routinesCommunicator != null) {
			routinesCommunicator.shutdown();
		}
	}

	private void initValues(TaskDTO task) {
		cmbExecState.getItems().addAll(ExecutionState.values());
		cmbExecState.getSelectionModel().select(task.getState());
	}
}
