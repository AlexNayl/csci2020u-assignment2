package server;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.Scanner;

/**
 * handles each individual client request
 */
public class ClientHandler implements Runnable{
	private Socket socket;

	Scanner in;
	PrintWriter out;

	ClientHandler(Socket socket){
		this.socket = socket;
	}

	public void run(){
		try{
			in = new Scanner(socket.getInputStream());
			out = new PrintWriter( socket.getOutputStream() );
			//Controller.log("Handling request from " + socket);

			//Get command and execute appropriate function
			String command = in.next();
			if(command.equals("DIR")){
				handleDir();
			}else if(command.equals("UPLOAD")){
				handleUpload();
			}else if(command.equals( "DOWNLOAD" )){
				handleDownload();
			}


		}catch(Exception e){
			Controller.log( "ERROR: " + e.getMessage() );
			e.printStackTrace();
		} finally {
			try{
				socket.close();
			} catch (Exception e){
				Controller.log("ERROR:" + e.getMessage());
			}
		}
	}

	/**
	 * handle directory command and reply's with list of files
	 */
	private void handleDir() throws Exception{
		Controller.log("Handling directory request from " + socket);
		File file = new File( Controller.getSharedDirectory() );
		if(file.isDirectory()){
			File subFiles[] = file.listFiles();
			for ( File currentFile:subFiles ) {
				if( !currentFile.isDirectory() ) {
					//Convert file to relative path so user cant see the server file structure
					URI filePath = currentFile.toURI();
					URI commonPath = Controller.getSharedDirectory();
					URI relativePath = commonPath.relativize( filePath );

					out.println( relativePath.getPath() );
				}
			}
		}else{
			Controller.log("ERROR: Common directory does not exist");
		}
		out.flush();
	}

	/**
	 * handle upload command, saves incoming text stream as file
	 */
	private void handleUpload(){
		File file = null;
		//The rest of the command line should be the path
		String path = in.nextLine().trim();
		Controller.log("Handling upload request from " + socket + " \"" + path + "\"");

		try {
			//Convert local path to absolute path
			URI localFile = new URI( path);
			file = new File(Controller.getSharedDirectory().resolve(localFile));
		}catch(Exception e){
			Controller.log("ERROR: path inparsable:");
		}

		if(file != null) {
			try {
				PrintWriter fileOut = new PrintWriter( file );

				while ( in.hasNext() ) {
					fileOut.println( in.nextLine() );
				}
				fileOut.flush();
				fileOut.close();
			} catch ( Exception e ) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * handle download command, gets file and returns text stream of file
	 */
	private void handleDownload(){
		File file = new File("");
		//The rest of the command line should be the path
		String path = in.nextLine().trim();

		Controller.log("Handling download request from " + socket + " \"" + path + "\"");
		try {
			//Convert local path to absolute path
			URI localFile = new URI( path );
			localFile = Controller.getSharedDirectory().resolve(localFile);
			file = new File(localFile);
		}catch(Exception e){
			Controller.log("ERROR: path un-parsable");
			e.printStackTrace();
		}

		if(file.exists()){
			try {
				Scanner scanner = new Scanner( file );
				while( scanner.hasNext() ) {
					out.println( scanner.nextLine() );
				}
				out.flush();
			}catch(Exception e){
				e.printStackTrace();
			}
		}else{
			Controller.log("ERROR: File does not exist");
		}
	}

}
