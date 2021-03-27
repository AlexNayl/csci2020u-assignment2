package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Listens for requests from a client and instantiates other threads to handle them
 */
public class ListenServer implements Runnable{

	private int port;
	private boolean running = true;
	ExecutorService pool = Executors.newFixedThreadPool( 50 );

	ServerSocket serverSocket;

	ListenServer(int port){
		this.port = port;
	}

	public void run(){

		Controller.log("Listen server started.");
		try {
			serverSocket = new ServerSocket( port );
		} catch ( Exception e ){
			Controller.log( "Failed to open port " + port );
			e.printStackTrace();
		}

		if(serverSocket != null){
			while(running){
				try {
					Socket clientSocket = serverSocket.accept();
					pool.execute( new ClientHandler(clientSocket) );
				}catch ( Exception e ){
					Controller.log("ERROR:" + e.getMessage());
				}
			}
		}
	}
}
