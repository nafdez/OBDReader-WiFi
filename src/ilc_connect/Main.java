package ilc_connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

	public static void main(String[] args) {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		// PC Development: 127.0.0.1
		// Real OBD-II testing: 192.168.0.10
		String obdIpAddress = "127.0.0.1";
		int obdPort = 35000;
		OBDReader obdr = null;
		boolean endProgram = false;

		boolean correct = false;
		while (!correct) {
			try {
				if (obdIpAddress == null && obdPort == 0) {
					System.out.println("Enter the IP Address of the OBD: ");
					obdIpAddress = in.readLine();
					System.out.println("Enter the specified port: ");
					obdPort = Integer.parseInt(in.readLine());
				}
				obdr = new OBDReader(obdIpAddress, obdPort);
				correct = true;
			} catch (IOException e) {
				System.out.println("Please, enter the input correctly. Example\n- IP: 192.168.X.XX\n- Port: 0000");
			}
		}

		if (obdr != null) {
			obdr.connectToCar();
			try {
				// Waits till user enter "END" command.
				while (!commandInterpreter(obdr, in.readLine()))
					;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Given a command, if the command exists execute it. Also takes care about
	 * extra parameters.
	 */
	private static boolean commandInterpreter(OBDReader obdr, String cmd) {
		boolean endProgram = false;
		String[] commandTrimmed = cmd.split(" ");
		switch (commandTrimmed[0]) {
		case ("RPM"):
			// If a parameter "LOOP" encountered on RPM command, keep reading RPM on a new
			// Thread
			if (commandTrimmed.length > 1 && commandTrimmed[1].equals("LOOP")) {
				OBDReader obdr2 = obdr;
				new Thread(() -> {
					while (true) {
						System.out.println("RPM: " + obdr2.getRPM());
					}
				}).start();
			} else {
				System.out.println("RPM: " + obdr.getRPM());
			}
			break;
		case ("SPEED"):
			System.out.println("Speed: " + obdr.getSpeed());
			break;
		case ("ERROR-C"):
			String[] codes = obdr.getAllErrorCodes();
			for (String code : codes) {
				System.out.println(code);
			}
			break;
		case ("END"):
			endProgram = true;
			break;
		default:
			System.out.println("Please, enter a command");
		}
		return endProgram;
	}

	private static void loopInThread() {
		// TODO
	}

}
