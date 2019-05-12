public class Main {

	public static void main(String[] args) {
		int x = 1302;
		String strX = String.valueOf(x);
		String binary = Integer.toBinaryString(x);
		binary = new String(new char[16 - binary.length()]).replace("\0", "0") + binary;
		System.out.println(strX);
		System.out.println(Integer.parseInt(binary, 2));
	}
}
