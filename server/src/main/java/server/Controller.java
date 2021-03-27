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

	private final int PORT = 556;									//change if getting allready in use errors

	public void initialize(){
		staticLogArea = logTextArea;
		statusLabel.setText("Running on port " + PORT + ".");
		server = new ListenServer( PORT );
		Thread serverThread = new Thread(server, "Server Thread");
		serverThread.start();
	}

	public static void log(String text){
		staticLogArea.appendText( LocalDateTime.now() + " - "+ text + "\n" );
	}

}
