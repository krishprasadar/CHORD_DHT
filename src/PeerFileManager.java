import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
/**
 * class to handle the file download and upload request from the peers
 * @author Srinath Kanna, Krishna Prasad and Ajeeth Kannan
 *
 */
public class PeerFileManager extends Thread {
	//data member of the class
    private static final int messageSize = 1024;
    private File file;
    private static String filePath;
    private long fileSize;
    private int totalPackets;
    private byte[] sendData = new byte[messageSize];
    private byte fileInBytes[];
    private byte fileInPackets[][];

    PeerNode peerNode = new PeerNode();

    Socket socket;

    String sendToIP;
    int sendPortTo = 9495;
    int sendZoneTo;

    String recvIPFrom;
    int recvPortFrom = 9485;
    int recvZoneFrom;
    /**
     * contructor of the class
     */
    public PeerFileManager() {

    }

    /**
     * method to upload the file to the chord
     * @param path-filepath of the file
     * @param share-false to upload a file
     * 			    true to share the file to its peer white join and leave 
     */
    public void uploadFile(String path, boolean share) {
        filePath = path;
        sendZoneTo = peerNode.hash(filePath);
        System.out.println("File sending to zone :" + sendZoneTo);
        if(!share)
            readFileToUpload();
        else
            readFileToShare();
        sendToIP = peerNode.getZoneIP(sendZoneTo, peerNode.nearestPeer(sendZoneTo));
        makePackets();
        sendPackets();

    }
    /**
     * method to download a file from chord
     * @param filePath-name of the file to be downloaded
     * @throws IOException
     */
    public void downloadFile(String filePath) throws IOException {
        System.out.println("Download request formed for file :" + filePath);

        recvZoneFrom = peerNode.hash(filePath);
        System.out.println("Requesting file from zone: " + recvZoneFrom);
        recvIPFrom = peerNode.getZoneIP(recvZoneFrom, peerNode.nearestPeer(recvZoneFrom));
        socket = new Socket(recvIPFrom, recvPortFrom);

        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

        byte recvData[] = new byte[messageSize];

        sendData = filePath.getBytes();
        dataOutputStream.write(sendData);
        dataInputStream.read(recvData, 0, recvData.length);
        String data = new String(recvData);

        if (!data.contains("NoSuchFile")) {
            String[] fileDetails = data.split(" ");
            
            peerNode.addfileNames(fileDetails[0]);
            int totalPackets = Integer.parseInt(fileDetails[1]);
            fileInPackets = new byte[totalPackets][messageSize];
            for (int i = 0; i < totalPackets; i++) {
                dataInputStream.read(recvData, 0, recvData.length);
                System.arraycopy(recvData, 0, fileInPackets[i], 0, messageSize);

            }
            fileWrite(fileDetails[0], totalPackets);
        }
        System.out.println("Download complete.");
        PeerNode.printOptionsMenu();
    }
    /**
     * method write the file from packets
     * @param filepath-name of the file
     * @param totalPackets-number of packets to be written
     * @throws IOException
     */
    private void fileWrite(String filepath, int totalPackets) throws IOException {
        File file = new File(PeerNode.getMyIP());

        if(!file.exists())
            file.mkdirs();

        FileOutputStream fos = new FileOutputStream(String.valueOf(peerNode.hash(filepath)));
        System.out.println("File received.");

        for (int j = 0; j < totalPackets - 1; j++) {
            fos.write((fileInPackets[j]));
        }

        fos.write(new String(fileInPackets[totalPackets - 1]).trim().getBytes());
        fos.close();

    }


    /**
     * method to send the packets to upload
     */
    private void sendPackets() {
        try {
            Socket socket = new Socket(sendToIP, sendPortTo);
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String data = new String(new File(filePath).getName() + " " + totalPackets + " ");
            makeMessage(data);
            dataOutputStream.write(sendData);

            dataOutputStream.flush();
            for (int i = 0; i < totalPackets; i++) {
                dataOutputStream.write(fileInPackets[i]);
            }
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * method to read the file to be uploaded
     */
    private void readFileToUpload() {
        
        file = new File(filePath);
        try {
            fileInBytes = Files.readAllBytes(file.toPath());
           
        } catch (IOException e) {
         
        }

    }
    /**
     * method to read the file that has to be shared among peers
     */
    private void readFileToShare() {
        
        file = new File(PeerNode.getMyIP() + "/" + peerNode.hash(filePath));
        try {
            fileInBytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {

            e.printStackTrace();
        }
        
    }
    /**
     * method to make packets for the file to be sent
     */
    private void makePackets() {

        fileSize = file.length();
        totalPackets = (int) Math.ceil(fileSize / (double) messageSize);
        
        fileInPackets = new byte[totalPackets][messageSize];
        for (int i = 0; i < totalPackets - 1; i++)
            System.arraycopy(fileInBytes, i * messageSize, fileInPackets[i], 0, messageSize);
        System.arraycopy(fileInBytes, (totalPackets - 1) * messageSize, fileInPackets[totalPackets - 1], 0,
                (int) fileSize - (totalPackets - 1) * 1024);

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
