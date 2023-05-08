package auto.utils.fluid;

import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;

/**
 * For activities that do not use general or advanced search implementations and
 * instead mimic them with custom tables
 * 
 * @author vercin
 */
public interface ViewLoadDataSearch extends View {
	
	/**
	 * @return String name of the DataSourceDefinition map that represents the
	 *         search criteria table
	 */
	public String getSearchTableName();
	
	/**
	 * @return String primary key value to be added to load data search requests
	 */
	public String getMaintRowid();
	
	default LoadDataResponse loadDataSearch() {
		return View.search(getActivityDefinition(), searchRequest(), getMaintRowid());
	}
	
	default Record loadDataSearchRecord() {
		return loadDataSearchDataSourceDef().getCurrentRecord();
	}
	
	default DataSourceDefinition loadDataSearchDataSourceDef() {
		return getActivityDefinition().getDataSourceDefinitionMap().get(getSearchTableName());
	}
	
	default String[] loadDataResolveCriteriaToPrimaryKeyValues(Record searchCriteria) {
		loadDataSearchDataSourceDef().getRecordList().add(0, searchCriteria);
		loadDataSearch();
		return searchResultsPrimaryKeyValues();
	}
	
	default String loadDataResolveCriteriaToPrimaryKeyValuesUnique(Record searchCriteria) {
		loadDataResolveCriteriaToPrimaryKeyValues(searchCriteria);
		return searchResultsPrimaryKeyValuesUnique();
	}
	
	/**
	 * For View activities that do not use the search, genSearch or AdvSearch
	 * searching functionalities and instead use a parameterized LoadDataRequest for
	 * a specific record to search.
	 * 
	 * @param activityDef	ActivityDefinition 
	 * @param table			The custom table used for parameterizing
	 * @param maintRowid	Record primaryKey value relevant to the search
	 * @return Parameterized LoadDataRequest for the given maintRowid
	 */
	default LoadDataRequest searchRequest() {
		LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.EDIT);
		
		View.parameterizeRequest(loadDataSearchDataSourceDef().getCurrentRecord(), loadDataRequest);
		//System.out.println("CustomSearch LoadDataRequest: " + loadDataRequest.serializeMessage());
		//LoadDataResponse loadDataResponse = ActivityDefinitionUtils.loadData(activityDef, loadDataRequest, LoadDataModeEnum.EDIT, maintRowid);
		//System.out.println("CustomSearch LoadDataResponse: " + loadDataResponse.serializeMessage());
		return loadDataRequest;
	}
	
}
