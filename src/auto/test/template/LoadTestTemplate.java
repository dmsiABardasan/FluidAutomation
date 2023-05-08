package auto.test.template;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LoadTestTemplate {

	@Before
	public void before() {
		//LoadTests.setup();
	}
	
	@After
	public void after() {
		//LoadTests.verifyRetryAttempts();
	}
	
	@Test
	public void test_CustomerView() {
/*		LazyProcedure procedure = new LazyProcedure(true);
		
		ActivityDefinition activityDef = procedure.lazyLoadActivity(utils.frameworks.receivables.customer.CustomerView.IP_SERVICE);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.myBrandOnly", false);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.excludePermanentStopAcct", false);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.FIND.getValue());
		assertTrue(activityDef.getResultsDataSourceDefinition().getRecordList().size() != 0);
		
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.batchPage", "last");
		Step step = procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.FIND.getValue());
		assertTrue(activityDef.getResultsDataSourceDefinition().getRecordList().size() != 0);
		
		//System.out.println(response.getResponse().getParamAsObject("batchPage"));
		final int halfPage = ((BigDecimal) step.getResponse().getParamAsObject("batchPage")).intValue() / 2;
		
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.batchPage", halfPage);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.FIND.getValue());
		assertTrue(activityDef.getResultsDataSourceDefinition().getRecordList().size() != 0);
		
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.batchPage", 1);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.myBrandOnly", true);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.excludePermanentStopAcct", true);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.showHiddenCustomers", true);
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, "search.NameWord", "Fred");
		procedure.step(activityDef, ValidationTypeEnum.VALIDATE_ALL, CrudButtonEnum.FIND.getValue());
		assertTrue(activityDef.getResultsDataSourceDefinition().getRecordList().size() != 0);*/
	
	}
}
