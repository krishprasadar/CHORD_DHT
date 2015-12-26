import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * The class hears for finger table updates from the
 * peers. The class is a thread which can support for
 * multiple clients at a given time.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class PeerLookUpUpdate extends Thread {

	private static final int messageSize = 64;

	private ServerSocket serverSocket;

	Socket socket;

	private boolean isServerRunning;

	// constructor
	public PeerLookUpUpdate() {

		try {
			isServerRunning = true;
			serverSocket = new ServerSocket(9994);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// thread starts
	public void run() {

		while (isServerRunning) {

			try {

				socket = serverSocket.accept();

				new PeerLookUpUpdateHandler(socket).start();

			} catch (IOException e) {
				
			}

		}

	}

	/**
	 * The class is a support class for PeerLookUpUpdate
	 */
	private class PeerLookUpUpdateHandler extends Thread {

		Socket socket;

		String clientIP;

		byte recvData[] = new byte[messageSize];

		// constructor
		public PeerLookUpUpdateHandler(Socket socket) {

			this.socket = socket;
			clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());

		}

		// thread starts
		public void run() {
			try {

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

				dataInputStream.read(recvData, 0, recvData.length);

				int message = Integer.parseInt(new String(recvData).trim());

				PeerNode.peerLookUP.setLookUPIP(clientIP, message);

				socket.close();
			} catch (IOException e) {
				
			}
		}

	}

	/**
	 * stops serverSocket
	 */
	public void stopServer() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		isServerRunning = false;
	}

}
