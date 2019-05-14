public class Sender {
    public static void main(String[] args) throws Exception {
        TCPSocket tcpSocket = new TCPSocketImpl(10000, 12345);
        tcpSocket.send("src/new.txt");
//        tcpSocket.close();
//        tcpSocket.saveCongestionWindowPlot();
    }
}
