import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * The class gets the successor and predecessor updates
 * from the peers. The class is a thread which can listen for
 * multiple clients at a given point.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class PeerNeighbourUpdate extends Thread {

	private static final int messageSize = 64;

	private ServerSocket serverSocket;
	Socket socket;
	private boolean isServerRunning;

	// constructor
	public PeerNeighbourUpdate() {

		try {
			serverSocket = new ServerSocket(9992);
			isServerRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// thread starts
	public void run() {

		

		while (isServerRunning) {

			try {

				socket = serverSocket.accept();

				new PeerNeighbourUpdateHandler(socket).start();

			} catch (IOException e) {
			
			}

		}

		// socket.close();

	}

	/**
	 * closes serverSocket
	 */
	public void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
	
		}
		isServerRunning = false;
	}

	/**
	 *
	 * The class is a supporter class for PeerNeighbourUpdate.
	 *
	 *
	 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
	 */
	private class PeerNeighbourUpdateHandler extends Thread {

		Socket socket;

		String clientIP;

		byte recvData[] = new byte[messageSize];

		// constructor
		public PeerNeighbourUpdateHandler(Socket socket) {

			this.socket = socket;
			clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());

		}

		// thread starts
		public void run() {
			try {

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

				dataInputStream.read(recvData, 0, recvData.length);

				String message[] = new String(recvData).trim().split(" ");

				if (new String(recvData).trim().contains("predecessor")) {
					PeerNode.setPredecessor(clientIP, Integer.parseInt(message[1]), Integer.parseInt(message[2]));
				} else if (new String(recvData).trim().contains("successor")) {
					PeerNode.setSuccessor(clientIP, Integer.parseInt(message[1]), Integer.parseInt(message[2]));
				}

				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
