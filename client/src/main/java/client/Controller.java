package client;


import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import java.io.File;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Controller {
	@FXML ListView clientListView;
	@FXML ListView serverListView;
	@FXML TextField ipField;
	@FXML TextField portField;

	ObservableList<String> clientList = FXCollections.observableArrayList();
	ObservableList<String> serverList = FXCollections.observableArrayList();
	ListProperty<String> clientListProperty = new SimpleListProperty<>();
	ListProperty<String> serverListProperty = new SimpleListProperty<>();

	private static final String sharedDirectory = "./sharedData";

	public void initialize(){
		clientListView.itemsProperty().bind( clientListProperty );
		serverListView.itemsProperty().bind( serverListProperty );
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
						clientList.add(currentFile.getPath());
					}
				}

				clientListProperty.set(clientList);

			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	public void handleDownloadButton( ActionEvent event ) {
	}

	public void handleUploadButton( ActionEvent event ) {
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
}
