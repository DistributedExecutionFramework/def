package at.enfilo.def.local.simulator.gui;

import at.enfilo.def.communication.impl.ticket.TicketHandlerDaemon;
import at.enfilo.def.config.server.core.DEFTicketServiceConfiguration;
import at.enfilo.def.execlogic.impl.ExecLogicException;
import at.enfilo.def.local.simulator.ExecLogicServiceStarter;
import at.enfilo.def.local.simulator.IdType;
import at.enfilo.def.local.simulator.SimulatorExecLogicController;
import at.enfilo.def.local.simulator.Type;
import at.enfilo.def.transfer.UnknownJobException;
import at.enfilo.def.transfer.UnknownProgramException;
import at.enfilo.def.transfer.UnknownTaskException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class SimulatorGuiController implements Initializable {

	private SimulatorExecLogicController simulatorExecLogicController;

	@FXML private TreeView<IdType> trvProgramsJobsTasks;
	@FXML private Pane pnlSelection;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Start Services
		TicketHandlerDaemon.start(new DEFTicketServiceConfiguration());

		//library = Library.getInstance();
		//Executors.newSingleThreadExecutor().submit(() -> library.startServices());

		TreeItem<IdType> root = new TreeItem<>(new IdType("", Type.CLUSTER));

		simulatorExecLogicController = new SimulatorExecLogicController(root);
		ExecLogicServiceStarter starter = new ExecLogicServiceStarter(simulatorExecLogicController);
		simulatorExecLogicController.setConfiguration(starter.getConfiguration());
		Executors.newSingleThreadExecutor().submit(() -> starter.startServices());

		// Databindings
		trvProgramsJobsTasks.setRoot(root);
	}

	public void trvReleased(MouseEvent mouseEvent)
	throws UnknownProgramException, UnknownJobException, ExecLogicException, UnknownTaskException {

		TreeItem<IdType> treeItem = trvProgramsJobsTasks.getSelectionModel().getSelectedItem();
		if (treeItem != null) {
			GuiComponent guiComponent = null;

			switch (treeItem.getValue().getType()) {
				case CLUSTER:
					guiComponent = new ClusterGuiComponent(simulatorExecLogicController.getClusterInfo());
					break;
				case PROGRAM: {
						String pId = treeItem.getValue().getId();
						guiComponent = new ProgramGuiComponent(simulatorExecLogicController.getProgram(pId));
					}
					break;
				case JOB: {
						String jId = treeItem.getValue().getId();
						String pId = treeItem.getParent().getValue().getId();
						guiComponent = new JobGuiComponent(simulatorExecLogicController.getJob(pId, jId));
					}
					break;
				case TASK: {
						String tId = treeItem.getValue().getId();
						String jId = treeItem.getParent().getValue().getId();
						String pId = treeItem.getParent().getParent().getValue().getId();
						guiComponent = new TaskGuiComponent(
								simulatorExecLogicController,
								pId,
								jId,
								tId
						);
					}
					break;
				default:
					break;
			}
			if (guiComponent != null) {
				pnlSelection.getChildren().clear();
				pnlSelection.getChildren().add(guiComponent);
			}
		}
	}

}
