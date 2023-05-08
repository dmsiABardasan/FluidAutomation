package auto.utils.testRail;

import java.io.IOException;

import fluid.test.json.JsonObject;

public class DescriptionFetcher {

	private DescriptionFetcher() {}
	
	public static String getCaseDescription(int caseid) {
		String server = System.getProperty("testRailServer");
		if (server == null || server.isEmpty()) {
			throw new NullPointerException("System property \"testRailServer\" was null");
		}
		
		APIClient testRail = new APIClient(server);
		testRail.setUser(TestRailPostResults.USER);
		testRail.setPassword(TestRailPostResults.API_KEY);
		
		String description;
		try {
			JsonObject testRailTest = (JsonObject) testRail.sendGet("get_case/" + caseid);
			description = String.valueOf(testRailTest.get("title"));
		} catch (IOException | APIException | NullPointerException ex) {
			description = "C" + caseid;
		}
		
		return description; 
	}
	
}
