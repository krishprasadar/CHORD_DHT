import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * The class BootstrapEntryListener listens for new entrypoint IP
 * from the peers of the chord. The class is a server which can
 * support multiple clients at any point. The class is a thread.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class BootstrapEntryListener extends Thread {
	
	private static final int messageSize = 64;
	
	private ServerSocket serverSocket ;
	private Socket socket;

	// constructor
	public BootstrapEntryListener() {
		
		try {
			
			serverSocket = new ServerSocket(8881);
			
		}
		catch( Exception e ) {
			e.printStackTrace();
		}
	
	}

	// runs the thread
	public void run() {
		
		try {
			
			while( true ) {
				System.out.println("Bootstrap Entry Point listener running");
				socket = serverSocket.accept();
			
				new BootstrapEntryHandler( socket ).start();
			}
			
		}
		catch( Exception e ) {
			
		}
	}

	/**
	 *
	 * The class is a supporting class for BootstrapEntryListener. It responses
	 * to the peers that sends its IP as entry point.
	 *
	 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
	 */
	private class BootstrapEntryHandler extends Thread {

		Socket socket;
		byte[] recvMessage =new byte[messageSize];
		String clientIP;

		// constructor
		public BootstrapEntryHandler( Socket socket ) {
			
			this.socket = socket;
			clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());
			
		}

		// thread started
		public void run() {
			
			try {
				
				DataInputStream dataInputStream = new DataInputStream( socket.getInputStream() );
				dataInputStream.read(recvMessage, 0, recvMessage.length);
				
				BootstrapListener.setChordIP(clientIP);
				System.out.println("New Chord Entry Point set - " + clientIP);

			} 
			catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

}
