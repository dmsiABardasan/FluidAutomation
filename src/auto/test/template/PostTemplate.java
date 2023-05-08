package auto.test.template;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import auto.utils.ApiUtils;
import auto.utils.Reader;
import fluid.test.json.JsonException;
import fluid.test.json.JsonObject;
import fluid.test.json.Jsoner;

@RunWith(Parameterized.class)
public class PostTemplate {

	public static final String URI = "/api/v1/customer/create";
	public static final String INPUT_DATA_DIRECTORY = "/api/Debtors/Customer/create/";

	@Parameter(0)
	public String dataTestID;
	@Parameter(1)
	public String dataCustomer;
	@Parameter(2)
	public String dataValidateOnly;
	@Parameter(3)
	public String dataRequest;
	@Parameter(4)
	public String dataExpResponse;
	@Parameter(5)
	public String dataAssertionResponse;
	@Parameter(6)
	public String dataAssertionResponseExceptions;

	@Parameters(name = "{index} ({0})")
	public static Iterable<Object[]> data() {
		List<Object[]> testList = new ArrayList<Object[]>();
		testList.add(new Object[] { "Scenario No. 1", "PostCustomerCreateV1-C1", "false",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Request1.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Response1.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Assertion.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_AssertionExceptions.txt"});
		testList.add(new Object[] { "Scenario No. 2", "PostCustomerCreateV1-C2", "false",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Request2.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Response1.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Assertion.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_AssertionExceptions.txt"});
		testList.add(new Object[] { "Scenario No. 3", "PostCustomerCreateV1-ValOnly", "true",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Request1.json",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_Response2.json",
				"",
				INPUT_DATA_DIRECTORY+"PostCustomerCreateV1_AssertionExceptions.txt"});
		return testList;
	}

	@BeforeClass
	public static void setup() {
		//if (!AllSuites.isDefault()) AllSuites.setup();
	}
	
	@AfterClass
	public static void tearDown() {
		//CustomerSuiteV1.teardown();
	}
	
	@Test
	public void test_PostCustomerCreateV1() {
		
		String url = /*AllSuites.HOST +*/ URI;
		
		StringBuilder requestBldr = new StringBuilder(Reader.readFile(dataRequest));
		ApiUtils.replace(requestBldr, "${validateOnly}", dataValidateOnly);
		ApiUtils.replace(requestBldr, "${customerName}", dataCustomer);
		String request = requestBldr.toString();
		
		//System.out.println(Jsoner.prettyPrint(request));
		
		String expResponse = Reader.readFile(dataExpResponse);
		String actResponse = null;
		//try {
			actResponse = ""; //RestAdapter.getInstance().post(MediaType.JSON, url, request, AllCollectionsSuiteV1.headers(CustomerSuiteV1.API_KEY)); //TODO Add your implementation here!
/*		} catch (IOException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}*/
		
/*		System.out.println(Jsoner.prettyPrint(expResponse));
		System.out.println(Jsoner.prettyPrint(actResponse));*/
		
		JsonObject expResponseJson = null;
		JsonObject actResponseJson = null;
		try {
			expResponseJson = (JsonObject) Jsoner.deserialize(expResponse);
			actResponseJson = (JsonObject) Jsoner.deserialize(actResponse);
		} catch (JsonException ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
		
		Set<String> exceptions = new HashSet<String>();
		exceptions.add("idCust");
		assertTrue("Value was unequal.", ApiUtils.jsonCompare(expResponseJson, actResponseJson, exceptions));
		
		if (Boolean.parseBoolean(dataValidateOnly)) {
			//FIXME
			//assertFalse("Customer created despite being validate only.", CustomerSuiteV1.doesCustomerExist(null, dataCustomer));
		} else {
			String custID = String.valueOf(actResponseJson.get("idCust"));
			StringBuilder expAssertionResponseBldr = new StringBuilder(Reader.readFile(dataAssertionResponse));
			ApiUtils.replace(expAssertionResponseBldr, "${idCust}", custID);
			ApiUtils.replace(expAssertionResponseBldr, "${customerName}", dataCustomer);
			ApiUtils.replace(expAssertionResponseBldr, "${dateAcctOpened}", LocalDate.now().toString());
			String expAssertionResponse = expAssertionResponseBldr.toString();
			
			String actAssertionResponse = "";//CustomerSuiteV1.resolveMaintRowid(dataCustomer, custID); //TODO Add your implementation here!
			
	/*		System.out.println(Jsoner.prettyPrint(expAssertionResponse));
			System.out.println(Jsoner.prettyPrint(actAssertionResponse));*/
			
			JsonObject expAssertionResponseJson = null;
			JsonObject actAssertionResponseJson = null;
			try {
				expAssertionResponseJson = (JsonObject) Jsoner.deserialize(expAssertionResponse);
				actAssertionResponseJson = (JsonObject) Jsoner.deserialize(actAssertionResponse);
			} catch (JsonException ex) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
			
			assertTrue("Value was unequal.", ApiUtils.jsonCompare(expAssertionResponseJson, actAssertionResponseJson,
					Reader.bufferedReader(dataAssertionResponseExceptions)));
		}
		

	}
	
}
