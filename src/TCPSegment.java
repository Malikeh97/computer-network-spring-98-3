public class TCPSegment {
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
	private int urgentDataPointer;
	private String data;

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
