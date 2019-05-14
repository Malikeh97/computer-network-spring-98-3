import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
	private int base;
	private int windowSize = 5; // maybe need to change
	private boolean isSender;
	private final int DATA_SIZE = 1000;
	private List<Packet> sendBuffer = new ArrayList<>(100);
	private List<Packet> receiveBuffer = new ArrayList<>(100);

	public TCPSocketImpl(int port, int otherPort) throws Exception {
		this.socket = new EnhancedDatagramSocket(port);
		this.ip = InetAddress.getLocalHost();
		this.port = port;
		this.nextSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
		this.otherPort = otherPort;
		this.base = nextSeqNumber;
		this.isSender = true;

		System.out.println("socket init seq #: " + nextSeqNumber);

		handshake();
	}

	public TCPSocketImpl(EnhancedDatagramSocket socket, int port, int nextSeqNumber, int expectedSeqNumber, int otherPort) throws Exception {
		this.socket = socket;
		this.ip = InetAddress.getLocalHost();
		this.port = port;
		this.nextSeqNumber = nextSeqNumber;
		this.expectedSeqNumber = expectedSeqNumber;
		this.otherPort = otherPort;
		this.base = nextSeqNumber;
		this.isSender = false;
	}

	private void handshake() throws IOException {
		this.sendTimer = new Timer(true);
		this.sendTimer.scheduleAtFixedRate(new HandShakeTask(), 0, this.timeout);
		while (true) {
			DatagramPacket packet = TCPUtils.receive(socket);
			Packet segment = new Packet(packet.getData());

			if (segment.isSYN() && segment.isACK()) {
				this.expectedSeqNumber = segment.getSeqNumber() + 1;
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
			sendBuffer.add(packet);
		}
		System.out.println(packetDatas);
		if (this.sendTimer == null) {
			for (int i = 0; i < windowSize; i++) {
				if (nextSeqNumber < base + windowSize) {
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
				Packet packet = new Packet(buffer);
				if (packet.isACK()) {
					if (packet.getAckNumber() >= this.base) {
						this.base = packet.getAckNumber();
						this.sendTimer.cancel();
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

		// TODO
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
			base++;
		}
	}

	class ResendTask extends TimerTask {
		@Override
		public void run() {
			for (int i = base; i < nextSeqNumber; i++) {
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
