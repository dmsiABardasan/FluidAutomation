package auto.utils;

import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import auto.utils.step.LazyProcedure;

@RunWith(Parameterized.class)
public class JunitTemplate {

	public static final String TEST_ID = "SUITE-TESTCASE";
	//public static final String INPUT_DATA_DIRECTORY = "/path/to/InputFile.csv";
	
	@Parameter(0)
	public String dataTestID;
	@Parameter(1)
	public String dataCustomer;
	@Parameter(2)
	public String dataCustID;
	@Parameter(3)
	public String dataProduct;

	@Parameters(name = "{index} ({0})")
	public static Iterable<Object[]> data() {
		List<Object[]> testList = new ArrayList<Object[]>();
		testList.add(new Object[] { "Scenario No. 1", "testCust", "12345", "ATP999" });
		testList.add(new Object[] { "Scenario No. 2", "testCust", "12345", "ATP999" });
		return testList;
		//return Reader.inputData(INPUT_DATA_DIRECTORY);
		//return Collections.singleton(new Object[] {"Scenario No. 1", "testCust", "12345", "ATP999"});
	}

	@BeforeClass
	public static void setup() {
		// Login to server here.
	}

	@Test
	public void test_JunitTemplate() {

		LazyProcedure procedure = new LazyProcedure();
		final BigDecimal expValue;
		final BigDecimal actValue;
		try {

			expValue = BigDecimal.ZERO;
			actValue = BigDecimal.TEN;

		} finally {
			System.out.println(procedure.prettyPrintProcedureTest(dataTestID));
		}

		Assertions.assertGroup(() -> assertThat(actValue, Matchers.comparesEqualTo(expValue)));
	}

}
