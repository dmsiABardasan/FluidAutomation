package auto.utils.fluid;

import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;

/**
 * Provides a default method for the advanced searches methods for an instance
 * 
 * @author vercin
 */
@FunctionalInterface
public interface ViewAdvSearch extends View.ViewSearch {
	default GetResultsResponse advSearch() {
		GetResultsRequest getResultsRequest = searchRequest(getActivityDefinition());
		return View.search(getActivityDefinition(), getResultsRequest);
	}
	
	default Record advSearchRecord() {
		return advSearchDataSourceDef().getCurrentRecord();
	}
	
	default DataSourceDefinition advSearchDataSourceDef() {
		return getActivityDefinition().getAdvSearchDataSourceDefinition();
	}
	
	default String[] advResolveCriteriaToPrimaryKeyValues(Record searchCriteria) {
		advSearchDataSourceDef().getRecordList().add(0, searchCriteria);
		advSearch();
		return searchResultsPrimaryKeyValues();
	}
	
	default String advResolveCriteriaToPrimaryKeyValuesUnique(Record searchCriteria) {
		advResolveCriteriaToPrimaryKeyValues(searchCriteria);
		return searchResultsPrimaryKeyValuesUnique();
	}
	
	/**
	 * Generates and parameterizes a <code>GetResultsRequest</code> based on the
	 * current record of the advanced search DataSourceDefinition.
	 * 
	 * @param activityDef
	 *            ActivityDefinition containing the advanced search
	 *            DataSourceDefinition to parameterize from
	 * @return Parameterized GetResultsRequest
	 */
	public static GetResultsRequest searchRequest(ActivityDefinition activityDef) {
		final GetResultsRequest getResultsRequest;
		if (activityDef.getAdvSearchDataSourceDefinition() != null) {
			getResultsRequest = new GetResultsRequest();
			View.parameterizeRequest(activityDef.getAdvSearchDataSourceDefinition().getCurrentRecord(), getResultsRequest);
			//System.out.println("AdvSearch GetResultsRequest: " + getResultsRequest.serializeMessage());
			//GetResultsResponse getResultsResponse = ActivityDefinitionUtils.getResults(activityDef, getResultsRequest);
			//System.out.println("AdvSearch GetResultsResponse: " + getResultsResponse.serializeMessage());
		} else {
			getResultsRequest = null;
		}
		
		return getResultsRequest;
	}
}
