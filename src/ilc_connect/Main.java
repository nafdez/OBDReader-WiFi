package ilc_connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Main {

	public static void main(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String obdIpAddress;
		int obdPort;
		OBDReader obdr = null;
		boolean endProgram = false;

		boolean correct = false;
		while (!correct)
			try {
				System.out.println("Enter the IP Address of the OBD: ");
				obdIpAddress = in.readLine();
				System.out.println("Enter the specified port: ");
				obdPort = Integer.parseInt(in.readLine());
				obdr = new OBDReader(obdIpAddress, obdPort);
				correct = true;
			} catch (IOException e) {
				System.out.println("Please, enter the input correctly. Example\n- IP: 192.168.X.XX\n- Port: 0000");
			}

		if (obdr != null) {
			obdr.connectToCar();
			while (!endProgram) {
				try {
					String command = in.readLine();
					switch (command) {
					case ("RPM"):
						System.out.println(obdr.getRPM());
						break;
					case ("ERROR-C"):
						System.out.println(Arrays.toString(obdr.getAllErrorCodes()));
						break;
					case ("END"):
						endProgram = true;
					default:
						System.out.println("Please, enter a command");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
