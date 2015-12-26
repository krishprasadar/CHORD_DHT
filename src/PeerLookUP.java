import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class PeerLookUP {
	
	private static final int messageSize = 64;
	
	private byte sendData[] = new byte[messageSize];
	
	private LookUPPeerDetails[] lookUPPeerDetails;
	
	private PeerNode peerNode = new PeerNode();
	
	public PeerLookUP() {
		
		lookUPPeerDetails = new LookUPPeerDetails[PeerMain.m];
		
		for( int i = 0; i < lookUPPeerDetails.length; i++ ) {
			lookUPPeerDetails[i] = new LookUPPeerDetails(i);
		}
		
	}
	
	public void updateMyLookUP() {
		
		for( int i = 0; i < PeerMain.m; i++ ) {
			lookUPPeerDetails[i].getIP();
		}
		
	}
	
	 
	public void runLookUP() {
		
		for( int i = 0; i < PeerMain.m; i++ ) {
			
			int zoneUpdatingFrom = PeerNode.getMyZoneSrt() - ( (int) Math.pow(2, i) );
			if( zoneUpdatingFrom < 0 ) {
				zoneUpdatingFrom = PeerMain.n + zoneUpdatingFrom;
			}
			
			int zoneUpdatingTill = PeerNode.getMyZoneEnd() - ( (int) Math.pow(2, i) );
			if( zoneUpdatingTill < 0 ) {
				zoneUpdatingTill = PeerMain.n + zoneUpdatingTill;
			}
			
			int times;
			
			if( zoneUpdatingFrom <= zoneUpdatingTill ) {
				times = zoneUpdatingTill - zoneUpdatingFrom;
			}
			else {
				int times1 = PeerMain.n - zoneUpdatingFrom + 1;
				int times2 = zoneUpdatingTill - 0;
				
				times = times1 + times2;
				
			}
			
			int zoneUpdating = zoneUpdatingFrom;
			
			while( times >= 0 ) {
				
				if( zoneUpdating >= PeerNode.getMyZoneSrt() && zoneUpdating <= PeerNode.getMyZoneEnd() ) {
					
				}
				else {	
		
					String peerIP = peerNode.getZoneIP(zoneUpdating, PeerNode.getSuccessor().getIP() );
					String message[] = peerNode.getPeerDetails(peerIP);
					
					String zone[] = message[0].split(" ");
					int srtZone = Integer.parseInt(zone[0]);
					int endZone = Integer.parseInt(zone[1]);
					
					if( zoneUpdating != endZone ) {
						
						while( zoneUpdating != endZone ) {
							zoneUpdating++;
							if( zoneUpdating > PeerMain.n - 1 ) {
								zoneUpdating = zoneUpdating % PeerMain.n;
							}
							
							times--;
							
							if( times < 0 ) {
								break;
							}
							
						}
						
					}
					
					if( times < 0 ) {
						break;
					}
				 
					try {
						Socket socket = new Socket( peerIP, 9994);
						
						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
						
						makeMessage(i + "");
						dataOutputStream.write(sendData);
						dataOutputStream.flush();
						
						socket.close();
						
					} catch (IOException e) {
						e.printStackTrace();
					} 
					
				}
				
				zoneUpdating++;
				if( zoneUpdating > PeerMain.n - 1 ) {
					zoneUpdating = zoneUpdating % PeerMain.n;
				}
				
				times--;
				
			}
			
		}
		
		
	}
	
	public void setLookUPIP( String ip, int i ) {
		lookUPPeerDetails[i].peerIP = ip;
	}

	public int getZonePos( int i ) {
		return lookUPPeerDetails[i].zonePos;
	}
	
	public String getPeerIP( int i ) {
		return lookUPPeerDetails[i].peerIP;
	}
	
	private class LookUPPeerDetails {
		
		int tableID;
		int zone;
		int zonePos;
		String peerIP;
		
		public LookUPPeerDetails( int ID ) {
			
			this.tableID = ID;
			
			zonePos = (int) Math.pow(2, tableID);
			
			getIP();
			
		}
				
		public void getIP() {
						
			zone = PeerNode.getMyZoneEnd() + zonePos;
			
			if( zone > PeerMain.n - 1 ) {
				zone = zone % PeerMain.n;
			}
			
			if( zone >= PeerNode.getMyZoneSrt() && zone <= PeerNode.getMyZoneEnd() ) {
				peerIP = PeerNode.getMyIP();
			}
			else {
				peerIP = peerNode.getZoneIP(zone, PeerNode.getSuccessor().getIP() );	
			}
			
		}
		
	}
	
	private void makeMessage(String message) {
		
		Arrays.fill(sendData, 0, messageSize, (byte) 0);
		byte messageByte[] = message.getBytes();
		ByteBuffer byteBuffer = ByteBuffer.wrap(sendData);
		byteBuffer.position(0);
		byteBuffer.put(messageByte);
		sendData = byteBuffer.array();
	
	}
	
}
