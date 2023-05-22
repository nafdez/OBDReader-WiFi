package ilc_connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class OBDReader {
	private Socket socket;
	private PrintWriter outputWriter;
	private BufferedReader inputReader;
	private String ipAddress;
	private int port;
	private SimpleDateFormat formatter;
	private Date date;
	private String logFile;
	private String logFilePath;
	private boolean isConnected;
	private final byte LF_SEPARATOR = 0x0A;
	private final int END_COMM = '>';

	OBDReader(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		date = new Date();
		String logFile = "log.txt";
		String logFilePath = "./" + logFile;
		createLogFile(logFile);
	}

	/*
	 * Makes the connection to the OBD-II Device.
	 */
	public void connectToCar() {
		try {
			System.out.println("Wait...");
			// Create a socket connection to the OBD-II reader
			socket = new Socket(ipAddress, port);

			// Create output and input streams for communication
			outputWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Connection successful, ready to communicate with the car
			System.out.println("Connected to the car.");
			writeToLogFile("[CONNECTION]" + formatter.format(date) + ": [IP_" + ipAddress + "][PORT_" + port + "]\n",
					logFile);
		} catch (IOException e) {
			System.out.println("Unable to connect to OBD Scanner, please, check IP and port.");
		}
	}

	/*
	 * Read character by character the output of the OBD-II Device until END_COMM
	 * char is reached '>'. If I just use inputReader.readLine wouldn't work because
	 * it keeps reading ad infinitum since it isn't a file. '>' is the end of the
	 * communication because is the OBD-II asking you for new input.
	 */
	private String parseRawInput() throws IOException {
		String response = "";
		int ASCIIResponse;
		while ((ASCIIResponse = inputReader.read()) != END_COMM) {
			response += (char) ASCIIResponse;
		}
		return response;
	}

	/*
	 * Outputs the code "01 0C" to the OBD-II. 01 being the mode (mode 1) 0C meaning
	 * the command (read RPM) It receives 4 bytes in HEX. Example response: 41 01 2C
	 * 50 41: mode (same as before with 01 code) 01: command (get RPM) 2C 50: by
	 * joining the last two codes together we get 2C50, that is 11.344 in decimal
	 * dividing by 4, we obtain a result of 2.836 RPM
	 */
	public int getRPM() {
		try {
			// Send the RPM command to the OBD-II reader
			outputWriter.println("01 0C");
			outputWriter.flush();

			// Read the response from the OBD-II reader
			String response = parseRawInput();

			// Extract and process the RPM value from the response (Only applies to ISO
			// 9141)
			String[] responseParts = response.split(" ");
			String auxHexAppender = responseParts[3] + responseParts[4];
			int decimalCode = Integer.parseInt(auxHexAppender, 16);
			int rpm = decimalCode / 4;
			writeToLogFile("[RPM-RAW]" + formatter.format(date) + ": " + response + "\n", logFile);
			writeToLogFile("[RPM]" + formatter.format(date) + ": " + rpm + "\n", logFile);
			return rpm;
		} catch (IOException e) {
			System.out.println("Error while getting data from OBD-II Reader");
			e.printStackTrace();
		}

		return 0; // Default value in case of errors
	}

	public String[] getAllErrorCodes() {
		try {
			// Send the command to retrieve all error codes
			outputWriter.println("01 01");
			outputWriter.flush();

			// Read the response from the OBD-II reader
			inputReader.readLine();
			String response = inputReader.readLine();

			writeToLogFile("[ERROR_CODE]" + formatter.format(date) + ": " + response + "\n", logFile);
			System.out.println(response);
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
			System.out.println("Error while getting data from OBD-II Reader");
			e.printStackTrace();
		}

		return null; // Return null in case of errors
	}

	/*
	 * Creating a log file in order to keep a track of the info. This gonna be
	 * useful when doing graphs and intelligent things
	 */
	private void createLogFile(String filename) {
		try {
			File myObj = new File(filename);
			if (myObj.createNewFile()) {
				System.out.println("File created: " + myObj.getName());
			} else {
				System.out.println("File already exists.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred while creating the file.");
			e.printStackTrace();
		}
	}

	/*
	 * Null Pointer Exception for some reason
	 */
	private void writeToLogFile(String line, String filename) {
//		try {
//			FileWriter myWriter = new FileWriter(filename);
//			myWriter.write(line);
//			myWriter.close();
//		} catch (Exception e) {
//			System.out.println("An error occurred while writing the file.");
//			e.printStackTrace();
//		}
	}

}
