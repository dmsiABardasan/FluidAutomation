package auto.test.template;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

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

// Complete
@RunWith(Parameterized.class)
public class GetTemplate {

	public static final String URI = "/api/v1/customer/details";
	public static final String INPUT_DATA_DIRECTORY = "/api/Debtors/Customer/details/";

	@Parameter(0)
	public String dataTestID;
	@Parameter(1)
	public String dataCustomer;
	@Parameter(2)
	public String dataCustID;
	@Parameter(3)
	public String dataQueryString;
	@Parameter(4)
	public String dataExpResponse;

	@Parameters(name = "{index} ({0})")
	public static Iterable<Object[]> data() {
		List<Object[]> testList = new ArrayList<Object[]>();
		testList.add(new Object[] { "Scenario No. 1", "GetCustomerDetailsV1-C1", "55626",
				"?idCust=55626&externalCustomerNumber=",
				INPUT_DATA_DIRECTORY+"GetCustomerDetailsV1_Response.json" });
		return testList;
	}

	@BeforeClass
	public static void setup() {
		//if (!AllSuites.isDefault()) AllSuites.setup();
	}

	@Test
	public void test_GetCustomerDetailsV1() {
		
		String url = /*AllSuites.HOST +*/ URI + dataQueryString;
		
		String expResponse = Reader.readFile(dataExpResponse);
		String actResponse = null;
		//try {
			actResponse = "";//RestAdapter.getInstance().get(url, AllCollectionsSuiteV1.headers(CustomerSuiteV1.API_KEY)); //TODO Add your implementation here!
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
		
		ApiUtils.jsonCompare(expResponseJson, actResponseJson);
	}

}
