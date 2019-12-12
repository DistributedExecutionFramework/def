package at.enfilo.def.local.simulator;

import at.enfilo.def.communication.dto.Protocol;
import at.enfilo.def.communication.dto.ServiceEndpointDTO;
import at.enfilo.def.communication.exception.ClientCreationException;
import at.enfilo.def.library.Library;
import at.enfilo.def.library.api.ILibraryServiceClient;
import at.enfilo.def.library.api.client.factory.LibraryServiceClientFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

public class Simulator extends Application {

	private static Simulator instance;
	private Library library;
	private ILibraryServiceClient libraryServiceClient;

	public static void main(String[] args) throws ClientCreationException, UnknownHostException {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) throws IOException, ClientCreationException {
		instance = this;
		// Show GUI
		URL fxml = Simulator.class.getClassLoader().getResource("simulator.fxml");
		if (fxml != null) {
			Parent root = FXMLLoader.load(fxml);
			primaryStage.setTitle("DEF Simulator");
			primaryStage.setScene(new Scene(root));
			primaryStage.setHeight(800);
			primaryStage.setWidth(1200);
			primaryStage.show();

			startLibrary();
		} else {
			System.err.println("FXML not found!");
		}
	}

	private void startLibrary() throws ClientCreationException, UnknownHostException {
		library = Library.getInstance();
		library.startServices();

		ServiceEndpointDTO libraryEndpoint = new ServiceEndpointDTO();
		libraryEndpoint.setHost(InetAddress.getLocalHost().getHostAddress());
		libraryEndpoint.setPort(library.getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration().getPort());
		libraryEndpoint.setPathPrefix(library.getConfiguration().getServerHolderConfiguration().getThriftTCPConfiguration().getUrlPattern());
		libraryEndpoint.setProtocol(Protocol.THRIFT_TCP);
		libraryServiceClient = new LibraryServiceClientFactory().createClient(libraryEndpoint);
	}


	public static Simulator getInstance() {
		return instance;
	}

	public Library getLibrary() {
		return library;
	}

	public ILibraryServiceClient getLibraryServiceClient() {
		return libraryServiceClient;
	}
}
