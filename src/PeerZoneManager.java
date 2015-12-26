import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * class to handle join and leave functionalities in chord
 * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
 *
 */
public class PeerZoneManager extends Thread {
	//data members of the class
	private static final int messageSize = 64;

	private ServerSocket serverSocket;

	private PeerNode peerNode = new PeerNode();

	private boolean isServerRunning;
	Socket socket;
	/**
	 * constructor of the class to accept server request
	 */
	public PeerZoneManager() {

		try {
			serverSocket = new ServerSocket(9991);
			isServerRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/**
	 * method to handle thread of the class
	 */
	public void run() {

		while (isServerRunning) {

			try {

				socket = serverSocket.accept();
				new PeerZoneManagerHandler(socket).start();

			} catch (IOException e) {
				
			}

		}

	}
	/**
	 * method to stop the class thread
	 */
	public void stopServer() {
		try {
				serverSocket.close();
		} catch (IOException e) {
			
		}
		isServerRunning = false;
	}
	/**
	 * method to handle accepted thread of above class
	 * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
	 *
	 */
	private class PeerZoneManagerHandler extends Thread {
		//data member of the class
		Socket socket;

		String clientIP;

		byte sendData[] = new byte[messageSize];
		byte recvData[] = new byte[messageSize];
		/**
		 * constructor to handle the accepted threads
		 * @param socket
		 */
		public PeerZoneManagerHandler(Socket socket) {

			this.socket = socket;
			clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());

		}
		/**
		 * method to handle the thread of the class
		 */
		public void run() {
			try {

				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

				dataInputStream.read(recvData, 0, recvData.length);

				String message = new String(recvData).trim();
				
				if (message.contains("add")) {
					add(dataInputStream, dataOutputStream);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					shareFilesToNewPeer();
				} else if(message.contains("leave")) {

					leave(dataOutputStream);
				}
			
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/**
		 * method  to send the file to new peer 
		 */
		private void shareFilesToNewPeer() {

			PeerFileManager fileManager = new PeerFileManager();
			List<String> deleteList = new ArrayList<>();

			for(String fileName : peerNode.fileNames)
			{
				if(!peerNode.isInMyZone(peerNode.hash(fileName))) {
					fileManager.uploadFile(fileName, true);
					deleteList.add(fileName);
				}

			}

			peerNode.fileNames.removeAll(deleteList);

		}
		/**
		 * method to initiate for node leave
		 * @param dataOutputStream
		 * @throws IOException
		 */
		private void leave(DataOutputStream dataOutputStream) throws IOException {
			
			String peerDetails[] = peerNode.getPeerDetails(clientIP);
			boolean isEntryPoint = Boolean.parseBoolean(peerDetails[4]);
			
			if(isEntryPoint)
			{
				
				PeerMain.isEntryPoint = true;
				PeerNode.isEntryPoint = true;
				sendToBootStrap();
				
				String zone[] = peerDetails[2].split(" ");
				
				PeerNode.setMyZone(  PeerNode.getMyZoneSrt() , PeerNode.getSuccessor().getZoneEnd());
				PeerNode.setSuccessor(zone[0], Integer.parseInt(zone[1]), Integer.parseInt(zone[2]));
				
				if(zone[0].contains(PeerNode.getMyIP()))
				{
					PeerNode.setSuccessor(PeerNode.getMyIP(), PeerNode.getMyZoneSrt(), PeerNode.getMyZoneEnd());
					PeerNode.setPredecessor(PeerNode.getMyIP(), PeerNode.getMyZoneSrt(), PeerNode.getMyZoneEnd());
					
				}

			}
			else {
				
				String zone[] = peerDetails[1].split(" ");
				
				PeerNode.setMyZone(  PeerNode.getPredecessor().getZoneSrt() , PeerNode.getMyZoneEnd());
				PeerNode.setPredecessor(zone[0], Integer.parseInt(zone[1]), Integer.parseInt(zone[2]));
				
				if(zone[0].contains(PeerNode.getMyIP()))
				{
					
					PeerNode.setSuccessor(PeerNode.getMyIP(), PeerNode.getMyZoneSrt(), PeerNode.getMyZoneEnd());
					PeerNode.setPredecessor(PeerNode.getMyIP(), PeerNode.getMyZoneSrt(), PeerNode.getMyZoneEnd());

				}
				
			}
			
			peerNode.sendNeighbourUpdate(PeerNode.getPredecessor().getIP(), 1);
			peerNode.sendNeighbourUpdate(PeerNode.getSuccessor().getIP(), 0);

			PeerNode.peerLookUP.runLookUP();

			PeerNode.peerLookUP.updateMyLookUP();
			
			makeMessage("Confirmed");
			
			dataOutputStream.write(sendData);
			dataOutputStream.flush();
			dataOutputStream.close();
			
		}
		/**
		 * method to send the update information to bootstrap regarding new entry point
		 * @throws UnknownHostException
		 * @throws IOException
		 */
		private void sendToBootStrap() throws UnknownHostException, IOException {
			Socket entrySocket = new Socket(  PeerMain.serverIP, 8881 );
			DataOutputStream entryOutputStream = new DataOutputStream(entrySocket.getOutputStream());
			
			makeMessage(PeerNode.getMyIP());
			entryOutputStream.write(sendData);
			entryOutputStream.flush();
			entrySocket.close();
		}
		/**
		 * method to initiate node join in chord
		 * @param dataInputStream
		 * @param dataOutputStream
		 * @throws IOException
		 */
		private void add(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
			int midPoint = PeerNode.getMyZoneSrt() + ((PeerNode.getMyZoneEnd() - PeerNode.getMyZoneSrt()) / 2);

			if ( PeerNode.getMyIP().equals( PeerNode.getPredecessor().getIP() ) 
					&& PeerNode.getMyIP().equals( PeerNode.getSuccessor().getIP() ) ) {
				
				String message1 = PeerNode.getMyZoneSrt() + " " + midPoint + " ";
				
				PeerNode.setPredecessor(clientIP, PeerNode.getMyZoneSrt(), midPoint);
				PeerNode.setSuccessor(clientIP, PeerNode.getMyZoneSrt(), midPoint);
				
				PeerNode.setMyZone((midPoint + 1), PeerNode.getMyZoneEnd());
				
				String message2 = PeerNode.getMyIP() + " " + PeerNode.getMyZoneSrt() + " " + PeerNode.getMyZoneEnd() + " " +
						PeerNode.getMyIP() + " " + PeerNode.getMyZoneSrt() + " " + PeerNode.getMyZoneEnd();
				
				makeMessage(message1 + message2);
			
			} else {
				
				String message1 = PeerNode.getMyZoneSrt() + " " + midPoint + " " + PeerNode.getPredecessor().getIP() + " "
						+ PeerNode.getPredecessor().getZoneSrt() + " " + PeerNode.getPredecessor().getZoneEnd() + " " ;
				
				PeerNode.setPredecessor(clientIP, PeerNode.getMyZoneSrt(), midPoint);
				PeerNode.setMyZone((midPoint + 1), PeerNode.getMyZoneEnd());
				
				String message2 = PeerNode.getMyIP() + " " + PeerNode.getMyZoneSrt() + " " + PeerNode.getMyZoneEnd();
				
				makeMessage(message1 + message2);

				peerNode.sendNeighbourUpdate(PeerNode.getSuccessor().getIP(), 0);

			}

			dataOutputStream.write(sendData);
			
			dataOutputStream.flush();

		}
		/**
		 * method to wrap the message to be sent to message size
		 * @param message-message to be sent
		 */
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
