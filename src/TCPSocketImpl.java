import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class TCPSocketImpl extends TCPSocket {

    private EnhancedDatagramSocket socket;
    private InetAddress ip;
    private int port;
    private int initSeqNumber;
    private int timeout;
    private int serverAckNumber;

    public TCPSocketImpl(String ip, int port) throws Exception {
        super(ip, port);
        this.socket = new EnhancedDatagramSocket(12346);
        this.ip = InetAddress.getByName(ip);
        this.port = port;
        this.initSeqNumber = ThreadLocalRandom.current().nextInt(0, (int) Math.pow(2.0, 16.0));
        this.timeout = 1000;

        System.out.println("socket init seq #: " + initSeqNumber);
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new HandShakeTask(), this.timeout, this.timeout);
        while (true) {
            DatagramPacket packet = TCPUtils.receive(socket);
            TCPSegment segment = new TCPSegment(new String(packet.getData()));

            if (segment.isSYN()) {
                this.serverAckNumber = segment.getSeqNumber();
                System.out.println("server ack #: " + serverAckNumber);
                segment = new TCPSegment();
                segment.setSeqNumber(this.initSeqNumber + 1);
                segment.setAckNumber(this.serverAckNumber + 1);
                TCPUtils.send(socket, this.ip, port, segment);
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
            TCPUtils.send(socket, ip, port, segment);
        }
    }
}
