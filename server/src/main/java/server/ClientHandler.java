package server;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * handles each individual client request
 */
public class ClientHandler implements Runnable{
	private Socket socket;

	ClientHandler(Socket socket){
		this.socket = socket;
	}

	public void run(){
		try{
			Scanner in = new Scanner(socket.getInputStream());
			PrintWriter out = new PrintWriter( socket.getOutputStream() );
			Controller.log( "Handled new socket " + socket );
			out.println("test reply");
			out.flush();

		}catch(Exception e){
			Controller.log( "ERROR: " + e.getMessage() );
		} finally {
			try{
				socket.close();
			} catch (Exception e){
				Controller.log("ERROR:" + e.getMessage());
			}
		}
	}

}
