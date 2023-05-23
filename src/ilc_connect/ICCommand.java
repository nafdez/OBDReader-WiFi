package ilc_connect;

public record ICCommand() {
	static String RESET = "ATZ";
	static String VIN = "09 02";
	static String ECU_NAME = "09 0A";
}
