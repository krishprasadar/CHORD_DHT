import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
/**
 * Class to send files that are requested by other peers in the network
 * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
 *
 */
public class PeerTransmitterListener extends Thread {
	//data members to read and send the file 
    private static final int messageSize = 1024;

    private ServerSocket serverSocket;

    PeerNode peerNode = new PeerNode();
    private boolean isServerRunning;
    Socket socket;

    private byte fileInBytes[];
    private byte fileInPackets[][];
    private File file;

    private static String filePath;
    private long fileSize;
    private int totalPackets;

    /**
     * constructor of the class
     */
    public PeerTransmitterListener() {

        try {
            serverSocket = new ServerSocket(9485);
            isServerRunning = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * method to handle the thread of the class
     */
    public void run() {

        while (isServerRunning) {

            try {

                socket = serverSocket.accept();

                new FileUploadHandler(socket).start();

            } catch (IOException e) {

            }

        }


    }
    /**
     * method to stop the thread 
     */
    public void stopServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {

        }
        isServerRunning = false;
    }
    /**
     * class to handle the thread of the above class
     * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
     *
     */
    private class FileUploadHandler extends Thread {

        Socket socket;

        byte sendData[] = new byte[messageSize];
        byte recvData[] = new byte[messageSize];
        /**
         * constructor for the class
         * @param socket-socket to send and receive request
         */
        public FileUploadHandler(Socket socket) {

            this.socket = socket;

        }
        /**
         * method to handle the thread of the class
         */
        public void run() {

            try {

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

                dataInputStream.read(recvData, 0, recvData.length);

                String data = new String(recvData).trim();
                System.out.println("File name requested :" + data);
                if (peerNode.isFileOwner(data)) {
                    filePath = data;
                    readFileToTransmit();
                    makePackets();
                    sendPackets();

                } else {
                    data = "NoSuchFile";
                    sendData = data.getBytes();
                    dataOutputStream.write(sendData);
                }
                PeerNode.printOptionsMenu();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        /**
         * method to send the file packets
         */
        private void sendPackets() {
            try {

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                System.out.println(new String(filePath + " " + totalPackets));
                String data = (filePath + " " + totalPackets + " ");
                makeMessage(data);
                dataOutputStream.write(sendData);
                for (int i = 0; i < totalPackets; i++) {
                    dataOutputStream.write(fileInPackets[i]);
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /**
         * method to read the file to be transmitted
         */
        private void readFileToTransmit() {
            
            file = new File(String.valueOf(PeerNode.getMyIP() + "/" + peerNode.hash(filePath)));
            try {
                fileInBytes = Files.readAllBytes(file.toPath());
            } catch (IOException e) {

                e.printStackTrace();
            }

        }


        /**
         * method to make file to be sent as packets
         */
        private void makePackets() {
            
            fileSize = file.length();
            totalPackets = (int) Math.ceil(fileSize / (double) messageSize);
          
            fileInPackets = new byte[totalPackets][messageSize];
            for (int i = 0; i < totalPackets - 1; i++)
                System.arraycopy(fileInBytes, i * messageSize, fileInPackets[i], 0, messageSize);
            System.arraycopy(fileInBytes, (totalPackets - 1) * messageSize, fileInPackets[totalPackets - 1], 0, (int) fileSize - (totalPackets - 1) * 1024);

        }
        /**
         * method to wrap the message to be sent to appropriate message length
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
