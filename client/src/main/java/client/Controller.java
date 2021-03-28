package client;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Controller {
	@FXML
	TextField ipField;
	@FXML
	TextField portField;

	public void handleConnectButton( ActionEvent event ){
		int port = Integer.parseInt( portField.getText() );
		String ipAddress = ipField.getText();

		try {
			Socket socket = new Socket( ipAddress, port );
			if(socket.isConnected()) {
				PrintWriter out = new PrintWriter( socket.getOutputStream() );
				out.println("DIR ");
				out.flush();
				Scanner in = new Scanner( socket.getInputStream() );
				while(in.hasNext()) {
					System.out.println( in.nextLine() );
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


}
