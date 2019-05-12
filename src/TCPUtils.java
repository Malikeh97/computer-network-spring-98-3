import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class TCPUtils {
	public static void send(EnhancedDatagramSocket dSocket, InetAddress ip, int port, TCPSegment segment) {
		byte[] buffer = segment.toString().getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
		try {
			dSocket.send(packet);
		} catch (IOException e) {
			System.out.println("send packet exception");
			e.printStackTrace();
		}
	}

	public static DatagramPacket receive(EnhancedDatagramSocket dSocket) throws IOException {
		byte[] buffer = new byte[1408];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		dSocket.receive(packet);

		System.out.println("----------------------");
		System.out.println(new String(buffer));
		System.out.println(new String(packet.getData()));
		System.out.println("----------------------");
		return packet;
	}
}
