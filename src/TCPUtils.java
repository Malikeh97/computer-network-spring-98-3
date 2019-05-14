import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TCPUtils {
	public static void send(EnhancedDatagramSocket socket, InetAddress ip, int port, Packet segment) {
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

	public static List<String> splitFile(String path, int packetSize) throws FileNotFoundException {
		List<String> packetDatas = new ArrayList<>();

		File file = new File(path);
		Scanner scanner = new Scanner(file);

		StringBuilder fileStr = new StringBuilder(scanner.nextLine());
		while (scanner.hasNextLine()) {
			fileStr.append("\n").append(scanner.nextLine());
		}
		int numOfPackets = (int) Math.ceil((double) fileStr.length() / packetSize);
		for (int i = 0; i < numOfPackets; i++) {
			int start = i * packetSize;
			if (i < numOfPackets - 1)
				packetDatas.add(fileStr.substring(start, start + packetSize));
			else
				packetDatas.add(fileStr.substring(start));
		}

		scanner.close();
		return packetDatas;
	}
}
