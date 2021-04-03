package server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.time.LocalDateTime;


public class Controller {
	@FXML TextField directoryField;
	@FXML
	Label statusLabel;
	@FXML
	TextArea logTextArea;

	private static TextArea staticLogArea;
	private static TextField staticDirectoryField;

	private ListenServer server;
	Thread serverThread;

	private static URI sharedDirectory;

	private final int PORT = 555;									//change if getting already in use errors
	private final static String DEFAULT_SHARED_DIRECTORY = "./sharedData";

	public void initialize(){
		staticDirectoryField = directoryField;
		staticLogArea = logTextArea;
		try {
			//This mess gets me an absolute URI from the string path above
			Path tempFile = new File(DEFAULT_SHARED_DIRECTORY).toPath().toAbsolutePath();
			sharedDirectory = tempFile.toUri().normalize();
			directoryField.setText( sharedDirectory.getPath() );    //Convert to absolut path
		}catch(Exception e){
			e.printStackTrace();
		}

		statusLabel.setText("Running on port " + PORT + ".");
		server = new ListenServer( PORT );
		serverThread = new Thread(server, "Server Thread");
		serverThread.start();
	}

	public static void log(String text){
		staticLogArea.appendText( LocalDateTime.now() + " - "+ text + "\n" );
	}

	/**
	 * gets the shared directory path as a string
	 * @return string of the shared directory
	 */
	public static URI getSharedDirectory(){
		File testFile = new File(sharedDirectory);
		if(testFile.isDirectory()){
			return sharedDirectory;
		}else{
			System.err.println("SERVER: directory field does not point to a valid directory.");
			return null;
		}
	}


	public void handleDirectoryButton( ActionEvent actionEvent ) {
		//Get the current stage
		Node node = (Node) actionEvent.getSource();
		Stage thisStage = (Stage) node.getScene().getWindow();

		//Get current directory
		File currentDirectory = new File( sharedDirectory );
		if ( !currentDirectory.isDirectory() ) {
			currentDirectory = new File( "." );
		}

		//Prompt user
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setInitialDirectory( currentDirectory );
		File newDirectory = directoryChooser.showDialog( thisStage );

		//Check and save result
		if ( newDirectory != null ){
			//Converts it to an absolut URI
			sharedDirectory = newDirectory.toPath().toAbsolutePath().toUri().normalize();

			staticDirectoryField.setText( sharedDirectory.getPath() );
		}

	}
}
