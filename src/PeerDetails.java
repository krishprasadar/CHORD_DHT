
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 * The class gets the details of the peer. The sends the start zone,
 * end zone, successor, predecessor and entrypoint details. The class is
 * a thread which listens for peers.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class PeerDetails extends Thread {

	private static final int messageSize = 1024;

	private ServerSocket serverSocket;
	private boolean isServerRunning;
	Socket socket;

	// constructor
	public PeerDetails() {

		try {
			isServerRunning = true;
			serverSocket = new ServerSocket(9993);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// thread starts
	public void run() {

		while (isServerRunning) {

			try {

				socket = serverSocket.accept();

				new PeerDetailsHandler(socket).start();

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

	/**
	 *
	 * The class is a supporter class for PeerDetails. The class gives response
	 * to the peer by giving details of this peer
	 *
	 *
	 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
	 */
	private class PeerDetailsHandler extends Thread {

		Socket socket;

		String clientIP;

		byte sendData[] = new byte[messageSize];

		// // constructor
		public PeerDetailsHandler(Socket socket) {

			this.socket = socket;
			clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());

		}

		// thread starts
		public void run() {
			try {

				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

				String message1 = PeerNode.getMyZoneSrt() + " " + PeerNode.getMyZoneEnd() + "\n";
				String message2 = PeerNode.getPredecessor().getIP() + " " + PeerNode.getPredecessor().getZoneSrt() + " " + 
						PeerNode.getPredecessor().getZoneEnd() + "\n";
				String message3 = PeerNode.getSuccessor().getIP() + " " + PeerNode.getSuccessor().getZoneSrt() + " " + 
						PeerNode.getSuccessor().getZoneEnd() + "\n";
				String message4 = new String("");	
				String message5 = PeerNode.isEntryPoint.toString() + "\n";

				for (int i = 0; i < PeerMain.m; i++) {
					if (i != PeerMain.m - 1) {
						message4 = message4 + PeerNode.peerLookUP.getPeerIP(i) + " ";
					} else {
						message4 = message4 + PeerNode.peerLookUP.getPeerIP(i) + "\n";
					}
				}
				
				makeMessage(message1 + message2 + message3 + message4 + message5);
				
				dataOutputStream.write(sendData);
				dataOutputStream.flush();

				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		// wraps message into the sendByte
		private void makeMessage(String message) {

			Arrays.fill(sendData, 0, messageSize, (byte) 0);
			byte messageByte[] = message.getBytes();
			ByteBuffer byteBuffer = ByteBuffer.wrap(sendData);
			byteBuffer.position(0);
			byteBuffer.put(messageByte);
			sendData = byteBuffer.array();

		}

	}

}
