import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 *
 * The class is checks whether the requested zone number from a peer
 * is this peer's zone number. The class is a thread which can
 * support multiple clients at a given point.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class PeerCheckZone extends Thread {
	
	private static final int messageSize = 64;
	
	private ServerSocket serverSocket;
	
	PeerNode peerNode = new PeerNode();
	private boolean isServerRunning;
	Socket socket;
	
	// constructor
	public PeerCheckZone() {
		
		try {
			serverSocket = new ServerSocket(9990);
			isServerRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	// thread starts
	public void run() {
		
		while( isServerRunning ) { 
			
			try {
				
				socket = serverSocket.accept();
				
				new PeerZoneCheckHandler(socket).start();
				
			} catch (IOException e) {
				
			}
			
		}
		
		
	}

	/**
	 * stops the serverSocket
	 */
	public void stopServer()
	{
		try {
			serverSocket.close();
		} catch (IOException e) {
			
		}
		isServerRunning = false;
	}

	/**
	 *
	 * The class is a supporter class for PeerCheckZone. The class gives response
	 * to the peer by checking its zone.
	 *
	 *
	 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
	 */

	private class PeerZoneCheckHandler extends Thread {
		
		Socket socket;
		
		byte sendData[] = new byte[messageSize];
		byte recvData[] = new byte[messageSize];

		// constructor
		public PeerZoneCheckHandler(Socket socket) {
			
			this.socket = socket;
			
		}

		// thread starts
		public void run() {
			
			try {
				
				int messagePos = 0;
				
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
			
				dataInputStream.read(recvData, 0, recvData.length);
				
				int zoneQuery = Integer.parseInt( new String( recvData ).trim() );
				
				if( zoneQuery >= PeerNode.getMyZoneSrt() && zoneQuery <= PeerNode.getMyZoneEnd() ) {
					makeMessage("isMyZone", messagePos);
					dataOutputStream.write(sendData);
					dataOutputStream.flush();
				}
				else {
					
					String nearestIP;
					
					if( zoneQuery >= PeerNode.getPredecessor().getZoneSrt() && 
							zoneQuery <= PeerNode.getPredecessor().getZoneEnd() ) {
						nearestIP = PeerNode.getPredecessor().getIP();
					}
					else {
						nearestIP = peerNode.nearestPeer(zoneQuery);
					}
					
					makeMessage(nearestIP, messagePos);
					dataOutputStream.write(sendData);
					dataOutputStream.flush();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		// wraps message into the sendByte
		private void makeMessage(String message, int pos) {
			
			Arrays.fill(sendData, 0, messageSize, (byte) 0);
			byte messageByte[] = message.getBytes();
			ByteBuffer byteBuffer = ByteBuffer.wrap(sendData);
			byteBuffer.position(pos);
			byteBuffer.put(messageByte);
			sendData = byteBuffer.array();
		
		}
		
	}
	
}
