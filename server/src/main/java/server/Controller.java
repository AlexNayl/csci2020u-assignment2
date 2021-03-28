package server;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;


public class Controller {
	@FXML
	Label statusLabel;
	@FXML
	TextArea logTextArea;

	private static TextArea staticLogArea;

	private ListenServer server;
	Thread serverThread;

	private final int PORT = 556;									//change if getting already in use errors
	private final static String sharedDirectory = "./sharedData";

	public void initialize(){
		staticLogArea = logTextArea;
		statusLabel.setText("Running on port " + PORT + ".");
		server = new ListenServer( PORT );
		serverThread = new Thread(server, "Server Thread");
		serverThread.start();
	}

	public static void log(String text){
		staticLogArea.appendText( LocalDateTime.now() + " - "+ text + "\n" );
	}

	public static String getSharedDirectory(){
		return sharedDirectory;
	}


}
