import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TCPServerSocketImpl extends TCPServerSocket {

    private EnhancedDatagramSocket dSocket;
    private int initSeqNumber;
    private int timeout;

    public TCPServerSocketImpl(int port) throws Exception {
        super(port);
        this.dSocket = new EnhancedDatagramSocket(port);
        this.timeout = 1000;
        this.initSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
    }

    @Override
    public TCPSocket accept() throws Exception {
		DatagramPacket packet = TCPUtils.receive(dSocket);

		InetAddress clientIp = packet.getAddress();
		int clientPort = packet.getPort();
        TCPSegment segment = new TCPSegment(new String(packet.getData()));

        if (segment.isSYN()) {
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new HandShakeTask(clientIp, clientPort, segment.getAckNumber()), this.timeout, this.timeout);

            while (true) {
            	packet = TCPUtils.receive(dSocket);
            	segment = new TCPSegment(new String(packet.getData()));
            	if (segment.isACK()) {
            		System.out.println("---3-WAY HANDSHAKE ACCEPTED---");
					timer.cancel();
					break;
				}
			}
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        dSocket.close();
    }

    class HandShakeTask extends TimerTask {

		private final InetAddress ip;
		private final int port;
		private final int ackNumber;

		HandShakeTask(InetAddress ip, int port, int ackNumber) {
    		this.ip = ip;
    		this.port = port;
    		this.ackNumber = ackNumber;
		}

		@Override
		public void run() {
			System.out.println("Server HandShake");
			TCPSegment segment = new TCPSegment();
			segment.setSYN(true);
			segment.setSeqNumber(initSeqNumber);
			segment.setAckNumber(ackNumber + 1);

			TCPUtils.send(dSocket, ip, port, segment);
		}
	}
}
