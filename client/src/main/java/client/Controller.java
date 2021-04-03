package client;


import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;

public class Controller {
	@FXML TextField directoryField;
	@FXML ListView clientListView;
	@FXML ListView serverListView;
	@FXML TextField ipField;
	@FXML TextField portField;

	ObservableList<String> clientList = FXCollections.observableArrayList();
	ObservableList<String> serverList = FXCollections.observableArrayList();
	ListProperty<String> clientListProperty = new SimpleListProperty<>();
	ListProperty<String> serverListProperty = new SimpleListProperty<>();

	private static URI sharedDirectory;
	private final static String DEFAULT_SHARED_DIRECTORY = "./sharedData";

	public void initialize(){
		//Convert relative path above into absolute URI for later
		sharedDirectory  = new File(DEFAULT_SHARED_DIRECTORY).toPath().toAbsolutePath().toUri().normalize();
		directoryField.setText( sharedDirectory.getPath() );

		clientListView.itemsProperty().bind( clientListProperty );
		serverListView.itemsProperty().bind( serverListProperty );
		serverListProperty.set(serverList);
		clientListProperty.set(clientList);
	}

	public void handleRefreshButton( ActionEvent event ){
		Socket socket = openSocket();
		if(socket != null && socket.isConnected()){
			try{
				Scanner in = new Scanner(socket.getInputStream());
				PrintWriter out = new PrintWriter(socket.getOutputStream());

				serverList.clear();
				clientList.clear();

				//sends directory request command, expected response is list of files
				out.println("DIR ");
				out.flush();

				while(in.hasNextLine()){
					String filePath = in.nextLine();
					serverList.add(filePath);
				}

				serverListProperty.set(serverList);

				//Gets the local directory list
				File sharedFile = new File(sharedDirectory);
				if(sharedFile.isDirectory()){
					for(File currentFile : sharedFile.listFiles()){
						URI currentURI = sharedDirectory.relativize( currentFile.toURI() );		//Convert to relative path

						clientList.add(currentURI.getPath());
					}
				}

				clientListProperty.set(clientList);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	public void handleDownloadButton( ActionEvent event ) {
		//get selected file
		String selectedPath = (String)serverListView.getSelectionModel().getSelectedItem();

		if(!selectedPath.isEmpty()){
			Socket socket = openSocket();
			try {
				//Prep the streams
				Scanner in = new Scanner( socket.getInputStream() );
				PrintWriter netOut = new PrintWriter( socket.getOutputStream() );

				//Convert path to absolute path and open file at that location
				URI selectedURI = sharedDirectory.resolve( selectedPath );
				File outputFile = new File(selectedURI);
				PrintWriter fileOut = new PrintWriter( outputFile );

				//Send the download command, expect text stream of file contents back;
				netOut.println("DOWNLOAD "+ selectedPath);
				netOut.flush();
				while(in.hasNext()){
					fileOut.println(in.nextLine());
				}
				fileOut.flush();
				fileOut.close();
			}catch(Exception e){
				e.printStackTrace();
			}

			//Update the lists
			handleRefreshButton( event );
		}

	}

	public void handleUploadButton( ActionEvent event ) {
		//get selected file
		String selectedPath = (String)clientListView.getSelectionModel().getSelectedItem();

		//Convert local path to absolute and open the file
		try{
			URI selectedURI = new URI( selectedPath );
			selectedURI = sharedDirectory.resolve( selectedURI );
			File selectedFile = new File(selectedURI);

			if(selectedFile.exists()){

				//prep the text streams
				Socket socket = openSocket();
				Scanner in = new Scanner( selectedFile );
				PrintWriter out = new PrintWriter( socket.getOutputStream() );

				//Send the upload command, then send all the text
				out.println("UPLOAD " + selectedPath);

				while(in.hasNextLine()){
					out.println(in.nextLine());
				}
				out.flush();
				in.close();
				socket.close();


			}else{
				System.err.println("CLIENT: File does not exist");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		//Update the lists
		handleRefreshButton( event );
	}

	/**
	 * code for opening a socket
	 * @return socket
	 */
	private Socket openSocket(){
		Socket socket;
		int port = Integer.parseInt( portField.getText() );
		String ipAddress = ipField.getText();
		try {
			socket = new Socket( ipAddress, port );
			socket.setSoTimeout( 1000 );				//Prevent hanging
		}catch(Exception e){
			return null;
		}
		return socket;
	}

	private void updateListViews(){
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

			directoryField.setText( sharedDirectory.getPath() );
		}
	}
}
