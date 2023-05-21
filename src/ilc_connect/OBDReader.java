package ilc_connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class OBDReader {
	private Socket socket;
	private PrintWriter outputWriter;
	private BufferedReader inputReader;
	private String ipAddress;
	private int port;

	OBDReader(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public void connectToCar() {
		try {
			// Create a socket connection to the OBD-II reader
			socket = new Socket(ipAddress, port);

			// Create output and input streams for communication
			outputWriter = new PrintWriter(socket.getOutputStream(), true);
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Perform any initialization or configuration here

			// Connection successful, ready to communicate with the car
			System.out.println("Connected to the car.");
		} catch (IOException e) {
			System.out.println("Unable to connect to OBD Scanner, please, check IP and port.");
			// Handle connection errors
		}
	}

	public int getRPM() {
		try {
			// Send the RPM command to the OBD-II reader
			outputWriter.println("01 0C");

			// Read the response from the OBD-II reader
			String response = inputReader.readLine();

			// Extract and process the RPM value from the response
			String[] responseParts = response.split(" ");
			int rpm = Integer.parseInt(responseParts[2], 16) * 256 + Integer.parseInt(responseParts[3], 16);
			return rpm;
		} catch (IOException e) {
			e.printStackTrace();
			// Handle communication errors
		} catch (NumberFormatException e) {
			e.printStackTrace();
			// Handle parsing errors
		}

		return 0; // Default value in case of errors
	}

	public String[] getAllErrorCodes() {
		try {
			// Send the command to retrieve all error codes
			outputWriter.println("03");

			// Read the response from the OBD-II reader
			String response = inputReader.readLine();

			// Check if the response indicates that no error codes are present
			if (response.contains("NO DATA")) {
				return new String[0]; // Return an empty array if no error codes are found
			}

			// Extract the error codes from the response
			String[] errorCodes = response.split(" ");
			// Remove the first element, which is the command sent
			errorCodes = Arrays.copyOfRange(errorCodes, 1, errorCodes.length);
			return errorCodes;
		} catch (IOException e) {
			e.printStackTrace();
			// Handle communication errors
		}

		return null; // Return null in case of errors
	}

	// Other methods for sending/receiving OBD commands and processing data can be
	// implemented here
}
