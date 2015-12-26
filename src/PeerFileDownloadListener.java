import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
/**
 * class to download the files that are sent the peer
 * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
 *
 */
public class PeerFileDownloadListener extends Thread {
	//data members of the class
	private static final int messageSize = 1024;
	
	private ServerSocket serverSocket;
	
	PeerNode peerNode = new PeerNode();
	private boolean isServerRunning;
	Socket socket;
	private byte fileInPackets[][];
	/**
	 * constructor to handle the server request
	 */
	public PeerFileDownloadListener() {
		
		try {
			serverSocket = new ServerSocket(9495);
			isServerRunning = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    /**
     * method to handle the thread of the class
     */
	public void run() {
		
		while( isServerRunning ) { 
			
			try {
				
				socket = serverSocket.accept();
				
				new FileDownloadHandler(socket).start();
				
			} catch (IOException e) {
				
			}
			
		}
		
		
	}
	/**
	 * method stop the server
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
	 * class to handle the accepted threads of the server
	 * Srinath Kanna, Krishna Prasad and Ajeeth Kannan
	 *
	 */
	private class FileDownloadHandler extends Thread {
		//data members of the class
		Socket socket;
		
		byte sendData[] = new byte[messageSize];
		byte recvData[] = new byte[messageSize];
		/**
		 * constructor of the class
		 * @param socket
		 */
		public FileDownloadHandler(Socket socket) {
			
			this.socket = socket;
			
		}
		/**
		 * method to handle the thread of the class
		 */
		public void run() {
			
			try {
				
				DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
				
				dataInputStream.read(recvData, 0, recvData.length);

				System.out.println("Download Started.");

				String data=new String(recvData);
				String[] fileDetails=data.split(" ");
				
				peerNode.addfileNames(fileDetails[0]);
				int totalPackets=Integer.parseInt(fileDetails[1]);
				fileInPackets=new byte[totalPackets][messageSize];
				for(int i =0;i<totalPackets;i++)
				{
					dataInputStream.read(recvData,0,recvData.length);
					System.arraycopy(recvData, 0 ,fileInPackets[i], 0, messageSize);
					
				}
				fileWrite(fileDetails[0],totalPackets);

				System.out.println("Download Complete.");

				PeerNode.printOptionsMenu();
								
			} catch (IOException e) {
				e.printStackTrace();
			}

		}		
		/**
		 * method to write the file
		 * @param filepath-name of the file 
		 * @param totalPackets-total number of packets in file
		 * @throws IOException
		 */
		private void fileWrite(String filepath,int totalPackets) throws IOException 
		{
			File file = new File(PeerNode.getMyIP());

			if(!file.exists())
				file.mkdirs();


			FileOutputStream fos = new FileOutputStream(PeerNode.getMyIP() + "/" + String.valueOf(peerNode.hash(filepath)));

			byte[] finalFile = extractFileContents(fileInPackets);
			fos.write(finalFile);

			fos.close();
			
		}
		/**
		 * method to extract packets in file as an array
		 * @param fileArray-file as packet array
		 * @return-file as a single array
		 */
		private byte[] extractFileContents(byte[][] fileArray) {
			List<byte[]> data = new ArrayList<>();

			for (int i = 0; i <= fileArray.length - 1; i++) {
				if(i == fileArray.length - 1) {
					data.add(new String(fileArray[i]).trim().getBytes());
					break;
				}
				data.add(fileArray[i]);
			}

			byte[] finalFile = new byte[0];
			for (byte[] b : data) {
				byte[] temp = new byte[finalFile.length + b.length];
				System.arraycopy(finalFile, 0, temp, 0, finalFile.length);
				System.arraycopy(b, 0, temp, finalFile.length, b.length);
				finalFile = temp;
			}
			return finalFile;
		}
		
	}
	
}
