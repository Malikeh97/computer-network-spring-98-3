import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TCPSocketImpl extends TCPSocket {

	private EnhancedDatagramSocket socket;
	private InetAddress ip;
	private int port;
	private int nextSeqNumber;
	private int timeout = 1000;
	private int expectedSeqNumber;
	private int otherPort;
	private Timer sendTimer;
	private Timer receiveTimer;
	private int sendBase;
	private int receiveBase;
	private int windowSize = 5; // maybe need to change
	private boolean isSender;
	private final int DATA_SIZE = 1024;
	private Map<Integer, Packet> sendBuffer = new HashMap<>();
	private Map<Integer, Packet> receiveBuffer = new HashMap<>();

	public TCPSocketImpl(int port, int otherPort) throws Exception {
		this.socket = new EnhancedDatagramSocket(port);
		this.ip = InetAddress.getLocalHost();
		this.port = port;
		this.nextSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
		this.otherPort = otherPort;
		this.sendBase = nextSeqNumber;
		this.isSender = true;

		System.out.println("socket init seq #: " + nextSeqNumber);
		System.out.println(port);
		System.out.println(otherPort);
		handshake();
	}

	public TCPSocketImpl(EnhancedDatagramSocket socket, int port, int nextSeqNumber, int expectedSeqNumber, int otherPort) throws Exception {
		this.socket = socket;
		this.ip = InetAddress.getLocalHost();
		this.port = port;
		this.nextSeqNumber = nextSeqNumber;
		this.expectedSeqNumber = expectedSeqNumber;
		this.otherPort = otherPort;
		this.sendBase = nextSeqNumber;
		this.isSender = false;

		System.out.println(port);
		System.out.println(otherPort);
	}

	private void handshake() throws IOException {
		this.sendTimer = new Timer(true);
		this.sendTimer.scheduleAtFixedRate(new HandShakeTask(), 0, this.timeout);
		while (true) {
			DatagramPacket packet = TCPUtils.receive(socket);
			Packet segment = new Packet(packet.getData());

			if (segment.isSYN() && segment.isACK()) {
				this.expectedSeqNumber = segment.getSeqNumber() + 1;
				this.receiveBase = this.expectedSeqNumber;
				System.out.println("server ack #: " + expectedSeqNumber);
				segment = new Packet();
				segment.setACK(true);
				segment.setSeqNumber(this.nextSeqNumber);
				segment.setAckNumber(this.expectedSeqNumber);
				TCPUtils.send(socket, this.ip, otherPort, segment);
				this.sendTimer.cancel();
				this.sendTimer = null;
				break;
			}
		}
	}

	@Override
	public void send(String pathToFile) throws Exception {
		List<String> packetDatas = TCPUtils.splitFile(pathToFile, DATA_SIZE);
		for (int i = 0; i < packetDatas.size(); i++) {
			Packet packet = new Packet();
			packet.setData(packetDatas.get(i));
			packet.setSeqNumber(nextSeqNumber + i);
			sendBuffer.put(nextSeqNumber + i, packet);
		}
		Packet packet = new Packet();
		packet.setData("EOF");
		packet.setSeqNumber(nextSeqNumber + packetDatas.size());
		sendBuffer.put(nextSeqNumber + packetDatas.size(), packet);
		System.out.println(sendBuffer);
		System.out.println(sendBuffer.size());
		if (this.sendTimer == null) {
			for (int i = 0; i < windowSize; i++) {
				if (sendBuffer.size() <= i) break;
				if (nextSeqNumber < sendBase + windowSize) {
					this.sendOnePacket(nextSeqNumber);
				} else {
					System.out.println("refuse packet");
				}
			}
			this.sendTimer = new Timer(true);
			this.sendTimer.scheduleAtFixedRate(new ResendTask(), this.timeout, this.timeout);
			while (true) {
				byte[] buffer = new byte[1408];
				DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
				this.socket.receive(datagramPacket);
				packet = new Packet(buffer);
//				System.out.println("### " + packet);
				if (packet.isACK()) {
					if (packet.getAckNumber() >= this.sendBase) {
//						System.out.println(this.sendBase);
//						System.out.println();
						for (int i = this.sendBase; i <= packet.getAckNumber(); i++)
							this.sendBuffer.remove(i);
						this.sendBase = packet.getAckNumber() + 1;
						this.sendTimer.cancel();
						System.out.println(sendBuffer.size());
						System.out.println("!!!!!!! 1");
						if (this.sendBuffer.isEmpty()) break;
						System.out.println("!!!!!!! 2");
						if (this.sendBase == this.nextSeqNumber) {
							for (int i = 0; i < windowSize; i++) {
								if (sendBuffer.size() <= i) break;
								if (nextSeqNumber < sendBase + windowSize) {
									this.sendOnePacket(nextSeqNumber);
								} else {
									System.out.println("refuse packet");
								}
							}
						}
						this.sendTimer = new Timer(true);
						this.sendTimer.scheduleAtFixedRate(new ResendTask(), this.timeout, this.timeout);
					}
					// TODO: restart timer after 3 dup ack(maybe?)
				}
			}
		}
	}

	private void sendOnePacket(int seqNumber) throws IOException {
		System.out.println("sendOnePacket seq #: " + seqNumber);
		Packet packet = this.sendBuffer.get(seqNumber);
		byte[] buffer = packet.toBytes();
		DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, this.ip, this.otherPort);
		this.socket.send(datagramPacket);
		nextSeqNumber++;
	}

	@Override
	public void receive(String pathToFile) throws Exception {
		FileOutputStream dest = new FileOutputStream(pathToFile,true);
		while (true) {
			byte[] buffer = new byte[1408];
			DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
			this.socket.receive(datagramPacket);
			Packet packet = new Packet(buffer);
//			System.out.println(packet);
//			System.out.println(expectedSeqNumber);
			if (!packet.isACK()) {
				if (packet.getSeqNumber() == this.expectedSeqNumber) {
//					System.out.println("here");
					if (!packet.getData().equals("EOF"))
						dest.write(packet.getData().getBytes());
					Packet ack = new Packet();
					ack.setACK(true);
					ack.setAckNumber(this.expectedSeqNumber);
					buffer = ack.toBytes();
					datagramPacket = new DatagramPacket(buffer, buffer.length, this.ip, this.otherPort);
					this.socket.send(datagramPacket);
					this.expectedSeqNumber++;
					if (packet.getData().equals("EOF"))
						break;
				} else {
					Packet ack = new Packet();
					ack.setACK(true);
					ack.setAckNumber(this.expectedSeqNumber - 1);
					buffer = packet.toBytes();
					datagramPacket = new DatagramPacket(buffer, buffer.length, this.ip, this.otherPort);
					this.socket.send(datagramPacket);
				}
			}
		}
		dest.close();
	}

	@Override
	public void close() throws Exception {
		this.sendTimer.cancel();
		this.sendTimer = null;
		this.receiveTimer.cancel();
		this.receiveTimer = null;
		this.socket.close();
	}

	@Override
	public long getSSThreshold() {
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public long getWindowSize() {
		throw new RuntimeException("Not implemented!");
	}

	class HandShakeTask extends TimerTask {
		@Override
		public void run() {
			Packet segment = new Packet();
			segment.setSYN(true);
			segment.setSeqNumber(nextSeqNumber);
			TCPUtils.send(socket, ip, otherPort, segment);
			nextSeqNumber++;
			sendBase++;
		}
	}

	class ResendTask extends TimerTask {
		@Override
		public void run() {
			System.out.println("%%%%%%");
			System.out.println(sendBase);
			System.out.println(nextSeqNumber);
			for (int i = sendBase; i < nextSeqNumber; i++) {
				if (sendBuffer.size() <= i - sendBase) break;
				try {
					sendOnePacket(i);
				} catch (IOException e) {
					System.out.println("exception on send packet with seq #" + i);
					e.printStackTrace();
				}
			}
		}
	}
}
