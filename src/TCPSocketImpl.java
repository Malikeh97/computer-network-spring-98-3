import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TCPSocketImpl extends TCPSocket {

    private EnhancedDatagramSocket socket;
    private InetAddress ip;
    private int port;
    private int initSeqNumber;
    private int timeout;
    private int otherAckNumber;
    private int otherPort;
    private Timer timer;

    public TCPSocketImpl(int port, int otherPort) throws Exception {
        this.socket = new EnhancedDatagramSocket(port);
        this.ip = InetAddress.getLocalHost();
        this.port = port;
        this.initSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
        this.timeout = 1000;
        this.otherPort = otherPort;
        this.timer = new Timer(true);

        System.out.println("socket init seq #: " + initSeqNumber);

        handshake();
    }

    public TCPSocketImpl(EnhancedDatagramSocket socket, int port, int initSeqNumber, int otherAckNumber, int otherPort) throws Exception {
        this.socket = socket;
        this.ip = InetAddress.getLocalHost();
        this.port = port;
        this.initSeqNumber = initSeqNumber;
        this.timeout = 1000;
        this.otherAckNumber = otherAckNumber;
        this.otherPort = otherPort;
        this.timer = new Timer(true);

    }

    private void handshake() throws IOException {
        this.timer.scheduleAtFixedRate(new HandShakeTask(), 0, this.timeout);
        while (true) {
            DatagramPacket packet = TCPUtils.receive(socket);
            TCPSegment segment = new TCPSegment(new String(packet.getData()));

            if (segment.isSYN() && segment.isACK()) {
                this.otherAckNumber = segment.getSeqNumber();
                System.out.println("server ack #: " + otherAckNumber);
                segment = new TCPSegment();
                segment.setACK(true);
                segment.setSeqNumber(this.initSeqNumber + 1);
                segment.setAckNumber(this.otherAckNumber + 1);
                TCPUtils.send(socket, this.ip, otherPort, segment);
                timer.cancel();
                break;
            }
        }
    }

    @Override
    public void send(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void receive(String pathToFile) throws Exception {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() throws Exception {
        this.timer.cancel();
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
            TCPSegment segment = new TCPSegment();
            segment.setSYN(true);
            segment.setSeqNumber(initSeqNumber);
            TCPUtils.send(socket, ip, otherPort, segment);
        }
    }
}
