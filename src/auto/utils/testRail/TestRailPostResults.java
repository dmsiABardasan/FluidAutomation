package auto.utils.testRail;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import auto.utils.Reader;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class TestRailPostResults {
	public static final String LOOK_UP_TABLE = "/testrail.ini";
	public static final String RAW_REPORTS_DIRECTORY = "/reports/raw/";
	public static final String REPORT_PREFIX = "TEST-";
	
	// Login credentials, user and password (Preferably a create API Key)
	public static final String USER = "";
	public static final String API_KEY = "";
	
	private static final String STATUS_ID = "status_id",
								CASE_ID = "case_id",
								ACTUAL = "actual",
								FAILURE = "failure",
								SKIPPED = "skipped",
								COMMENT = "comment",
								ELAPSED = "elapsed",
								TIMESTAMP = "timestamp",
								TIME = "time",
								CUSTOM_STEPS_SEPARATED = "custom_steps_separated",
								CUSTOM_STEP_RESULTS = "custom_step_results",
								NAME = "name",
								CONTENT = "content";
	
	
	public static void main(String[] args) {
		
		int testRunTestId;
		String testRailUrl = "";
		if (args.length == 2) {
			if (args[0] != null && !args[0].isEmpty()) {
				testRailUrl = args[0];
			}else {
				throw new NullPointerException("The provided URL was empty or null.");
			}
			
			if (args[1] != null && !args[1].isEmpty()) {
				testRunTestId = Integer.parseInt(args[1]);
			}else {
				throw new NullPointerException("The provided Test Rail run test case number was empty or null.");
			}
		}else {
			throw new IllegalArgumentException("This program requires a Test Rail host URL and a Test Rail run test case number.");
		}
		
		APIClient testRail = new APIClient(testRailUrl);
		testRail.setUser(USER);
		testRail.setPassword(API_KEY);
		
		JsonObject testRailTest = null;
		try {
			testRailTest = (JsonObject) testRail.sendGet("get_test/" + testRunTestId);
		} catch (IOException | APIException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		BigDecimal testCaseId = (BigDecimal) testRailTest.get(CASE_ID);
		
		JsonArray testStepList = (JsonArray) testRailTest.get(CUSTOM_STEPS_SEPARATED);
		
		List<String[]> lookupTable = importTestCases(testCaseId.toPlainString());
		
		if (lookupTable.isEmpty()) {
			throw new NoSuchElementException("The testcase you were trying to find either does not exist, or does not have an automation test associated with it.");
		}
		
		for (Object obj: testStepList) {
			JsonObject testStep = (JsonObject) obj; 
			testStep.put(STATUS_ID, TestStatusEnum.UNTESTED.id());
			testStep.put(ACTUAL, "");
		}
		
		String timeStamp = "";
		String duration = "";
		
		for (String[] testCasePair: lookupTable) {
			NodeList reportTestCases = importReportResults(testCasePair[0]);	
			
			if (timeStamp.isEmpty()) {
				timeStamp = reportTestCases.item(0).getParentNode().getAttributes().getNamedItem(TIMESTAMP).getNodeValue();
			}
			
			if (duration.isEmpty()) {
				duration = reportTestCases.item(0).getParentNode().getAttributes().getNamedItem(TIME).getNodeValue();
			}
			
			for (Object obj: testStepList) {
				JsonObject testStep = (JsonObject) obj; 
			
				for (int i = 0 ; i < reportTestCases.getLength(); i++) {
				
					String name = reportTestCases.item(i).getAttributes().getNamedItem(NAME).getNodeValue();
					name = name.substring(name.indexOf("(") + 1, name.indexOf(")"));
					
					if (name.equalsIgnoreCase((String) testStep.get(CONTENT))) {
						
						String actual = "";
						
						NodeList types = reportTestCases.item(i).getChildNodes();
						
						int status = TestStatusEnum.PASS.id();
						for (int x = 0 ; x < types.getLength() ; x++) {
							try {
								if (FAILURE.equals(types.item(x).getNodeName())) {
									status = TestStatusEnum.FAIL.id();
									actual = types.item(x).getTextContent();
									break;
								}
								
								if (SKIPPED.equals(types.item(x).getNodeName())) {
									status = TestStatusEnum.BLOCKED.id();
									actual = "Automation: Known issue";
									break;
								}
							}finally {
								int previousStatus = (int) testStep.get(STATUS_ID);
								if (previousStatus != TestStatusEnum.UNTESTED.id() && previousStatus != status) {
									status = TestStatusEnum.RETEST.id();
									actual = "Oof! Automation had contradictory results from multiple tests!";
								}
							}
						}
						
						testStep.put(ACTUAL, actual);
						testStep.put(STATUS_ID, status);
						
						break;
					}
				}
			}
		}
		
		JsonObject wrapper = new JsonObject();
		wrapper.put(CUSTOM_STEP_RESULTS, testStepList);
		
		int untested = 0;
		int failed = 0;
		int blocked = 0;
		
		for (Object obj: testStepList) {
			JsonObject testStep = (JsonObject) obj;
			int status = (int) testStep.get(STATUS_ID);
			
			if (status == TestStatusEnum.UNTESTED.id()) {
				untested++;
			}else if (status == TestStatusEnum.FAIL.id()) {
				failed++;
			}else if(status == TestStatusEnum.BLOCKED.id()) {
				blocked++;
			}
		}
		
		if (untested > 0) {
			if (untested == testStepList.size()) {
				wrapper.put(STATUS_ID, TestStatusEnum.UNTESTED.id());
			}else {
				wrapper.put(STATUS_ID, TestStatusEnum.IN_PROGRESS.id());
			}
		}else {
			if (blocked == testStepList.size()) {
				wrapper.put(STATUS_ID, TestStatusEnum.BLOCKED.id());
			}else {
				if (failed > 0) {
					wrapper.put(STATUS_ID, TestStatusEnum.FAIL.id());
				}else {
					wrapper.put(STATUS_ID, TestStatusEnum.PASS.id());
				}
			}
		}
		
		wrapper.put(COMMENT, "Automation (" + timeStamp + " UTC)");
		if (Double.parseDouble(duration) < 1) {
			duration = "1";
		}
		wrapper.put(ELAPSED, duration + "s");
		
		//System.out.println(Jsoner.prettyPrint(Jsoner.serialize(wrapper)));
		
		try {
			testRail.sendPost("add_result/" + testRunTestId, wrapper);
		} catch (IOException | APIException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
	}
	
	
	private static NodeList importReportResults(String testName) {
		File file = new File(Reader.USER_DIRECTORY + RAW_REPORTS_DIRECTORY + REPORT_PREFIX + testName + ".xml");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
		        .newInstance();
		
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
		Document document = null;

		try {
			document = documentBuilder.parse(file);
		} catch (SAXException | IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
		NodeList testCases = document.getElementsByTagName("testcase");
		
		return testCases;
		
	}
	
	private static List<String[]> importTestCases(String testRailCase) {
		List<String> lookup = Reader.bufferedReader(LOOK_UP_TABLE);	
		
		List<String[]> testCases = new ArrayList<String[]>();
		
		for (String line: lookup) {
			String[] testCase = line.split("=");
			
			if (testCase.length == 2) {
				if (testRailCase.equals(testCase[1])) {
					testCases.add(testCase);
				}
			}
		}
		
		return testCases;
	}
}
