import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TCPServerSocketImpl extends TCPServerSocket {

    private EnhancedDatagramSocket socket;
    private int initSeqNumber;
    private int timeout;
    private InetAddress clientIP;
    private int clientPort;
    private int clientAckNumber;

    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.socket = new EnhancedDatagramSocket(port);
        this.timeout = 1000;
        this.initSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
    }

    @Override
    public TCPSocket accept() throws Exception {
		Timer timer = new Timer(true);
		while (true) {
			DatagramPacket packet = TCPUtils.receive(socket);
			System.out.println("### accept packet: " + packet);
			TCPSegment segment = new TCPSegment(new String(packet.getData()));
			if (segment.isSYN()) {
				this.clientIP = packet.getAddress();
				this.clientPort = packet.getPort();
				this.clientAckNumber = segment.getSeqNumber();
				timer.cancel();
				timer.scheduleAtFixedRate(new HandShakeTask(), this.timeout, this.timeout);
			} else if (segment.isACK()) {
				System.out.println("---3-WAY HANDSHAKE ACCEPTED---");
				timer.cancel();
				break;
			}
		}


        return new TCPSocketImpl(this.clientIP.getHostAddress(), clientPort);
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    class HandShakeTask extends TimerTask {

		@Override
		public void run() {
			System.out.println("Server HandShake");
			TCPSegment segment = new TCPSegment();
			segment.setSYN(true);
			segment.setSeqNumber(initSeqNumber);
			segment.setAckNumber(clientAckNumber + 1);

			TCPUtils.send(socket, clientIP, clientPort, segment);
		}
	}
}
