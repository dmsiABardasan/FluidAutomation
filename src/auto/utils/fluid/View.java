package auto.utils.fluid;

import static org.junit.Assert.assertTrue;

import auto.utils.ActivityDefinitionUtils;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.Request;

/**
 * <p>
 * For Fluid utility classes with an activity with a record search
 * functionality - usually any activity service ending in the suffix "View" but
 * may also used in "SRD" activity types as they are both of type
 * <code>View</code> and {@link Main}.
 * </p>
 * 
 * @author vercin
 */
public interface View{
	
	/**
	 * For {@link ViewGenSearch} and {@link ViewAdvSearch}, or anything else that
	 * has a specialized search table. overrides {@link View#getSearchResults()} with a
	 * default method. (As you should not be creating custom search requests when
	 * provided with a search DataSourceDefinition)
	 * 
	 * @author vercin
	 */
	interface ViewSearch extends View {
		@Override
		default DataSourceDefinition getSearchResults() {
			return getActivityDefinition().getResultsDataSourceDefinition();
		}
	}

	/**
	 * @return ActivityDefinition with search functionality
	 */
	public ActivityDefinition getActivityDefinition();
	
	/**
	 * @return DataSourceDefinition of where search results are collected
	 */
	public DataSourceDefinition getSearchResults();
	
	/**
	 * Returns primary key values for all records in the results
	 * DataSourceDefinition.
	 * 
	 * @return String array of primary key values
	 */
	default String[] searchResultsPrimaryKeyValues() {
		return View.getDataSourceDefPrimaryKeyValues(getSearchResults());
	}
	
	/**
	 * Returns a primary key value for the sole record in the results
	 * DataSourceDefinition, if more than one record exists, an exception is thrown.
	 * 
	 * @throws AssertionError
	 *             if the results dataSourceDefinition contained more than one
	 *             record
	 * @return String primary key value for the sole record in the results
	 *         DataSourceDefinition
	 */
	default String searchResultsPrimaryKeyValuesUnique() {
		String[] result = searchResultsPrimaryKeyValues();
		assertTrue("The supplied criteria did not produce a unique result (" + result.length + " results returned)",
				result.length == 1);
		return result[0];
	}
	
	/**
	 * @param activityDef
	 *            ActivityDefinition to search
	 * @param getResultsRequest
	 *            GetResultsRequest containing search criteria
	 * @return GetResultsResponse
	 */
	public static GetResultsResponse search(ActivityDefinition activityDef, GetResultsRequest getResultsRequest) {
		return ActivityDefinitionUtils.getResults(activityDef, getResultsRequest);
	}
	
	/**
	 * @param activityDef
	 *            ActivityDefinition to search
	 * @param loadDataRequest
	 *            LoadDataRequest containing search criteria
	 * @param primaryKeyValue
	 *            String primary key value to add into the loadDataRequest
	 * @return LoadDataResponse
	 */
	public static LoadDataResponse search(ActivityDefinition activityDef, LoadDataRequest loadDataRequest, String primaryKeyValue) {
		return ActivityDefinitionUtils.loadData(activityDef, loadDataRequest, primaryKeyValue);
	}
	
	/**
	 * Parameterizes the passed request with all fields and values in the passed
	 * Record, excluding fields with null values.
	 * 
	 * @param record
	 *            DataSourceDefinition to take parameters from
	 * @param request
	 *            Request to be parameterized
	 */
	public static void parameterizeRequest(Record searchCriteria, Request request) {
		for (String key: searchCriteria.keySet()) {
			Object value = searchCriteria.getFieldValueAsObject(key);
			if (value != null) {
				request.setParam(key, value);
			}
		}
	}

	/**
	 * Gets the primary key value for the current record in the given <code>DataSourceDefinition</code>
	 * 
	 * @param dataSourceDef DataSourceDefinition to retrive the primary key value from
	 * @return String value of the primary key field of the current record 
	 */
	public static String getDataSourceDefPrimaryKeyValue(DataSourceDefinition dataSourceDef) {
		return dataSourceDef.getCurrentRecord().getFieldValueAsString(dataSourceDef.getPrimaryKey());
	}
	
	/**
	 * Gets the primary key values for all records in the given <code>DataSourceDefinition</code>
	 * 
	 * @param dataSourceDef DataSourceDefinition to retrive the primary key value from
	 * @return String array of primary key values for all of the records the DataSourceDefinition contained
	 */
	public static String[] getDataSourceDefPrimaryKeyValues(DataSourceDefinition dataSourceDef) {
		String[] maintRowid = new String [dataSourceDef.getRecordList().size()];
		String primaryKey = dataSourceDef.getPrimaryKey();
		
		for (int i = 0; i < maintRowid.length; i++) {
			maintRowid[i] = dataSourceDef.getRecordList().get(i).getFieldValueAsString(primaryKey);
		}
		
		return maintRowid;
	}
}
