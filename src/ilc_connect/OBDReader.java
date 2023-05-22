package ilc_connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * The OBDReader class represents a reader for OBD-II devices. It allows
 * connecting to a car and retrieving various data from the car's onboard
 * computer.
 * 
 * @author nafdez
 */
public class OBDReader {
	private Socket socket;
	private PrintWriter outputWriter;
	private BufferedReader inputReader;
	private String ipAddress;
	private int port;
	private SimpleDateFormat formatter;
	private Date date;
	private String logFile;

	/*
	 * OBD-II marks the end of a communication by the character '>'.
	 */
	private final int END_COMM = '>';

	/**
	 * Constructs an OBDReader object with the specified IP address and port.
	 * 
	 * @param ipAddress the IP address of the OBD-II device
	 * @param port      the port number to connect to
	 */
	OBDReader(String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		SimpleDateFormat formatterPath = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		date = new Date();
		// create dir and file
		new File("logs").mkdirs();
		logFile = "logs/" + formatterPath.format(date) + "_log.txt";

		createLogFile(logFile);
	}

	/**
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

	/**
	 * Reads character by character the output of the OBD-II device until the
	 * END_COMM char is reached (>).
	 * 
	 * <p>
	 * If inputReader.readLine() is used directly, it would keep reading
	 * indefinitely since it isn't a file. The '>' character indicates the end of
	 * the communication because it is the OBD-II device asking for new input.
	 * </p>
	 * 
	 * @return the response from the OBD-II device
	 * @throws IOException if an I/O error occurs while reading the input
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
	/**
	 * Retrieves the RPM (Revolutions Per Minute) value from the OBD-II device.
	 * 
	 * @return the RPM value
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

	/**
	 * Retrieves all error codes from the OBD-II device.
	 * 
	 * @return an array of error codes, or an empty array if no error codes are
	 *         present
	 */
	public String[] getAllErrorCodes() {
		try {
			// Send the command to retrieve all error codes
			outputWriter.println("01 01");
			outputWriter.flush();

			// Read the response from the OBD-II reader
			String response = parseRawInput();

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

	/**
	 * Creates a log file with the specified filename.
	 * 
	 * @param filename path with filename of the log file
	 */
	private void createLogFile(String filename) {
		try {
			File logFile = new File(filename);

			if (logFile.createNewFile()) {
				System.out.println("File created: " + logFile.getName());
			} else {
				System.out.println("File already exists.");
			}
		} catch (Exception e) {
			System.out.println("An error occurred while creating the file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes a line of text to the specified log file.
	 * 
	 * @param line     the line of text to write
	 * @param filename path with filename of the log file
	 */
	private void writeToLogFile(String line, String filename) {
		try (FileWriter myWriter = new FileWriter(filename, true)) {
			myWriter.write(line);
		} catch (Exception e) {
			System.out.println("An error occurred while writing the file.");
			e.printStackTrace();
		}
	}

}
