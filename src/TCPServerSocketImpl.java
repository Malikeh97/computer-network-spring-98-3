import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TCPServerSocketImpl extends TCPServerSocket {

    private EnhancedDatagramSocket socket;
    private int port;
    private int nextSeqNumber;
    private int timeout;
    private InetAddress clientIP;
    private int clientPort;
    private int expectedSeqNumber;
	private Timer timer;

	public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.socket = new EnhancedDatagramSocket(port);
        this.port = port;
        this.timeout = 1000;
        this.nextSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
        System.out.println(nextSeqNumber);
    }

    @Override
    public TCPSocket accept() throws Exception {
		this.timer = new Timer(true);
		while (true) {
			DatagramPacket packet = TCPUtils.receive(socket);
			System.out.println("### accept packet: " + new String(packet.getData()));
			Packet segment = new Packet(packet.getData());
			System.out.println(segment.toString());
			if (segment.isSYN()) {
				this.clientIP = packet.getAddress();
				this.clientPort = packet.getPort();
				this.expectedSeqNumber = segment.getSeqNumber() + 1;
				timer.cancel();
				timer = new Timer(true);
				timer.scheduleAtFixedRate(new HandShakeTask(), 0, this.timeout);
			} else if (segment.isACK()) {
				System.out.println("---3-WAY HANDSHAKE ACCEPTED---");
				timer.cancel();
				break;
			}
		}
        return new TCPSocketImpl(socket, port, nextSeqNumber, expectedSeqNumber, clientPort);
    }

    @Override
    public void close() throws Exception {
    	this.timer.cancel();
        socket.close();
    }

    class HandShakeTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("Server HandShake");
			Packet segment = new Packet();
			segment.setSYN(true);
			segment.setACK(true);
			segment.setSeqNumber(nextSeqNumber);
			segment.setAckNumber(expectedSeqNumber);

			TCPUtils.send(socket, clientIP, clientPort, segment);
			nextSeqNumber++;
		}
	}
}
