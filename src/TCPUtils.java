import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class TCPUtils {
	public static void send(EnhancedDatagramSocket socket, InetAddress ip, int port, TCPSegment segment) {
		byte[] buffer = segment.toBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			System.out.println("send packet exception");
			e.printStackTrace();
		}
	}

	public static DatagramPacket receive(EnhancedDatagramSocket socket) throws IOException {
		byte[] buffer = new byte[1408];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);

		System.out.println("----------------------");
		System.out.println(new String(buffer));
		System.out.println(new String(packet.getData()));
		System.out.println("----------------------");
		return packet;
	}
}
