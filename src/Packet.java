public class Packet {
	private int srcPort;
	private int dstPort;
	private int seqNumber;
	private int ackNumber;
	private int headerLength;
	private boolean CWR;
	private boolean ECE;
	private boolean URG;
	private boolean ACK;
	private boolean PSH;
	private boolean RST;
	private boolean SYN;
	private boolean FIN;
	private int windowSize;
	private int checksum;
	private int urgentDataPointer;
	private String data;

	public Packet() {
		this.data = "";
	}

	public Packet(byte[] buffer) {
		String packet = new String(buffer);
		this.srcPort = Integer.parseInt(packet.substring(0, 16), 2);
		this.dstPort = Integer.parseInt(packet.substring(16, 32), 2);
		this.seqNumber = Integer.parseInt(packet.substring(32, 64), 2);
		this.ackNumber = Integer.parseInt(packet.substring(64, 96), 2);
		this.headerLength = Integer.parseInt(packet.substring(96, 100), 2);
		this.CWR = binaryToBoolean(packet.substring(104, 105));
		this.ECE = binaryToBoolean(packet.substring(105, 106));
		this.URG = binaryToBoolean(packet.substring(106, 107));
		this.ACK = binaryToBoolean(packet.substring(107, 108));
		this.PSH = binaryToBoolean(packet.substring(108, 109));
		this.RST = binaryToBoolean(packet.substring(109, 110));
		this.SYN = binaryToBoolean(packet.substring(110, 111));
		this.FIN = binaryToBoolean(packet.substring(111, 112));
		this.windowSize = Integer.parseInt(packet.substring(112, 128), 2);
		this.checksum = Integer.parseInt(packet.substring(128, 144), 2);
		this.urgentDataPointer = Integer.parseInt(packet.substring(144, 160), 2);
		this.data = packet.substring(160, packet.indexOf('\0'));
	}

	@Override
	public String toString() {
		return "Packet{" +
				"srcPort=" + srcPort +
				", dstPort=" + dstPort +
				", seqNumber=" + seqNumber +
				", ackNumber=" + ackNumber +
				", headerLength=" + headerLength +
				", CWR=" + CWR +
				", ECE=" + ECE +
				", URG=" + URG +
				", ACK=" + ACK +
				", PSH=" + PSH +
				", RST=" + RST +
				", SYN=" + SYN +
				", FIN=" + FIN +
				", windowSize=" + windowSize +
				", checksum=" + checksum +
				", urgentDataPointer=" + urgentDataPointer +
				", data='" + data + '\'' +
				'}';
	}

	public byte[] toBytes() {
		return (intToBinary(srcPort, 16) +
				intToBinary(dstPort, 16) +
				intToBinary(seqNumber, 32) +
				intToBinary(ackNumber, 32) +
				intToBinary(headerLength, 4) +
				intToBinary(0, 4) +
				booleanToBinary(CWR) +
				booleanToBinary(ECE) +
				booleanToBinary(URG) +
				booleanToBinary(ACK) +
				booleanToBinary(PSH) +
				booleanToBinary(RST) +
				booleanToBinary(SYN) +
				booleanToBinary(FIN) +
				intToBinary(windowSize, 16) +
				intToBinary(checksum, 16) +
				intToBinary(urgentDataPointer, 16) +
				data).getBytes();
	}

	private static String intToBinary(int x, int bits) {
		String binary = Integer.toBinaryString(x);
		binary = new String(new char[bits - binary.length()]).replace("\0", "0") + binary;
		return binary;
	}

	private static String booleanToBinary(boolean x) {
		return x ? "1" : "0";
	}

	private static boolean binaryToBoolean(String x) {
		return x.equals("1");
	}

	public int getSrcPort() {
		return srcPort;
	}

	public void setSrcPort(int srcPort) {
		this.srcPort = srcPort;
	}

	public int getDstPort() {
		return dstPort;
	}

	public void setDstPort(int dstPort) {
		this.dstPort = dstPort;
	}

	public int getSeqNumber() {
		return seqNumber;
	}

	public void setSeqNumber(int seqNumber) {
		this.seqNumber = seqNumber;
	}

	public int getAckNumber() {
		return ackNumber;
	}

	public void setAckNumber(int ackNumber) {
		this.ackNumber = ackNumber;
	}

	public int getHeaderLength() {
		return headerLength;
	}

	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}

	public boolean isCWR() {
		return CWR;
	}

	public void setCWR(boolean CWR) {
		this.CWR = CWR;
	}

	public boolean isECE() {
		return ECE;
	}

	public void setECE(boolean ECE) {
		this.ECE = ECE;
	}

	public boolean isURG() {
		return URG;
	}

	public void setURG(boolean URG) {
		this.URG = URG;
	}

	public boolean isACK() {
		return ACK;
	}

	public void setACK(boolean ACK) {
		this.ACK = ACK;
	}

	public boolean isPSH() {
		return PSH;
	}

	public void setPSH(boolean PSH) {
		this.PSH = PSH;
	}

	public boolean isRST() {
		return RST;
	}

	public void setRST(boolean RST) {
		this.RST = RST;
	}

	public boolean isSYN() {
		return SYN;
	}

	public void setSYN(boolean SYN) {
		this.SYN = SYN;
	}

	public boolean isFIN() {
		return FIN;
	}

	public void setFIN(boolean FIN) {
		this.FIN = FIN;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public int getChecksum() {
		return checksum;
	}

	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}

	public int getUrgentDataPointer() {
		return urgentDataPointer;
	}

	public void setUrgentDataPointer(int urgentDataPointer) {
		this.urgentDataPointer = urgentDataPointer;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
