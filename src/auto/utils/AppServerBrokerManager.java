package auto.utils;

public class AppServerBrokerManager {
	
	private static final String INJECTION_REGEX = "^[a-z0-9]*$";
	
	public static boolean trim(int serversToTrim) {
		return executeAsbMan("-all", ("-trimservers " + serversToTrim));
	}
	
	public static boolean trim(String brokerName, int serversToTrim) {
		checkForInjection(brokerName);
		return executeAsbMan(("-name " + brokerName), ("-trimservers " + serversToTrim));
	}
	
	private static void checkForInjection(String... params) {
		for (String param : params) {
			if (!param.matches(INJECTION_REGEX)) {
				throw new SecurityException("Security violation: command injection");
			}
		}
	}
	
	private static boolean executeAsbMan(String name, String command) {
		int exitCode = -1;
		Process process;

		try {
			process = Runtime.getRuntime().exec("asbman " + name + " " + command);
			//asbman -all -trim 100
/*			BufferedReader reader = new BufferedReader(new
			InputStreamReader(process.getInputStream()));
			String output = "";
			while ((output = reader.readLine()) != null) System.out.println(output);*/

			exitCode = process.waitFor();
			process.destroy();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return exitCode == 0;
	}
	
	@Deprecated
	public static int getBrokerServerAmount(String brokerName) {
		return 10; //TODO: Either call and process the command response or wait for REST interfacing
	}
	
	@Deprecated
	public static int getTotalServersAmount() {
		return 100; //TODO: Either call and process the command response or wait for REST interfacing
	}
	
}
