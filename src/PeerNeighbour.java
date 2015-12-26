
/**
 *
 * The class stores the successor and predecessor details.
 *
 * @author Srinath Kanna, Krishna Prasad, Ajeeth Kannan
 */
public class PeerNeighbour {

	private String ip;
	private int zoneSrt;
	private int zoneEnd;

	// constructor
	public PeerNeighbour(String ip, int zoneSrt, int zoneEnd) {

		this.ip = ip;
		this.zoneSrt = zoneSrt;
		this.zoneEnd = zoneEnd;

	}

	/**
	 *
	 * @param ip neighbor IP
	 * @param zoneSrt start zone of the neighbor
	 * @param zoneEnd end zone of the neighbor
     */
	public void updateZone(String ip, int zoneSrt, int zoneEnd) {
		this.ip = ip;
		this.zoneSrt = zoneSrt;
		this.zoneEnd = zoneEnd;
	}

	// getter for IP
	public String getIP() {
		return ip;
	}

	// getter for start zone
	public int getZoneSrt() {
		return zoneSrt;
	}

	// geter for end zone
	public int getZoneEnd() {
		return zoneEnd;
	}

}
