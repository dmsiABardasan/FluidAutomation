package auto.utils.fluid;

import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;

/**
 * Provides a default method for the search/genSearch method for an instance
 * 
 * @author vercin
 */
@FunctionalInterface
public interface ViewGenSearch extends View.ViewSearch {
	default GetResultsResponse genSearch() {
		GetResultsRequest getResultsRequest = searchRequest(getActivityDefinition());
		return View.search(getActivityDefinition(), getResultsRequest);
	}
	
	default Record genSearchRecord() {
		return genSearchDataSourceDef().getCurrentRecord();
	}
	
	default DataSourceDefinition genSearchDataSourceDef() {
		return getActivityDefinition().getGenSearchDataSourceDefinition();
	}
	
	default String[] genResolveCriteriaToPrimaryKeyValues(Record searchCriteria) {
		genSearchDataSourceDef().getRecordList().add(0, searchCriteria);
		genSearch();
		return searchResultsPrimaryKeyValues();
	}
	
	default String genResolveCriteriaToPrimaryKeyValuesUnique(Record searchCriteria) {
		genResolveCriteriaToPrimaryKeyValues(searchCriteria);
		return searchResultsPrimaryKeyValuesUnique();
	}

	/**
	 * Generates and parameterizes a <code>GetResultsRequest</code> based on the
	 * current record of the general search DataSourceDefinition.
	 * 
	 * @param activityDef
	 *            ActivityDefinition containing the general search
	 *            DataSourceDefinition to parameterize from
	 * @return Parameterized GetResultsRequest
	 */
	public static GetResultsRequest searchRequest(ActivityDefinition activityDef) {
		final GetResultsRequest getResultsRequest;
		if (activityDef.getGenSearchDataSourceDefinition() != null) {
			getResultsRequest = new GetResultsRequest();
			View.parameterizeRequest(activityDef.getGenSearchDataSourceDefinition().getCurrentRecord(), getResultsRequest);
			//System.out.println("GenSearch GetResultsRequest: " + getResultsRequest.serializeMessage());
			//GetResultsResponse getResultsResponse = ActivityDefinitionUtils.getResults(activityDef, getResultsRequest);
			//System.out.println("GenSearch GetResultsResponse: " + getResultsResponse.serializeMessage());
		} else {
			getResultsRequest = null;
		}
		
		return getResultsRequest;
	}
}
