package auto.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import auto.utils.fluid.ActivityTypeEnum;
import fluid.test.adapter.AppServerAdapter;
import fluid.test.data.ActivityDefinition;
import fluid.test.data.ChildActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.data.ViewDefinition;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.message.ActivityTaskRequest;
import fluid.test.message.ActivityTaskResponse;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;
import fluid.test.message.DataSet;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.SaveDataRequest;
import fluid.test.message.SaveDataResponse;

/**
 * Just some reusable functions for ActivityDefinition and descendants
 * @author vercin
 */
public final class ActivityDefinitionUtils {
	
	public static boolean mlDebugTrace; 
	
	private ActivityDefinitionUtils() {}
	
	@Deprecated
	public static LoadDataResponse responseLoadDataHandling(ActivityDefinition activityDef, PresentationMessage oResponse) {
		
		Map<CtrlPropPair, String> ctrlProps = new LinkedHashMap<>(oResponse.getCtrlProperties());
		
		LoadDataResponse loadDataResponse = null;
		if (oResponse instanceof SaveDataResponse && ctrlProps.isEmpty()) {
			SaveDataResponse saveDataResponse = (SaveDataResponse) oResponse;
			if (saveDataResponse.getSaveSuccess()) {
				String savedRecordId = saveDataResponse.getSaveRowId();
				if (savedRecordId != null && activityDef.getGenSearchDataSourceDefinition() != null) {
					GetResultsRequest getResultsRequest = new GetResultsRequest();
					getResultsRequest.setParam("rowid", savedRecordId);
					getResults(activityDef, getResultsRequest);
					try {
						loadDataResponse = activityDef.loadCurrentResultRecord();
					} catch (Throwable e) {
						e.printStackTrace();
						fail(e.getMessage());
					}
				} else {
					LoadDataModeEnum loadDataMode = savedRecordId != null 
							? LoadDataModeEnum.EDIT
							: LoadDataModeEnum.ADD;
					ActivityDefinitionUtils.loadData(activityDef,
							new LoadDataRequest(loadDataMode),
							savedRecordId != null ? savedRecordId : "");
				}
			}
		}
		
		for (CtrlPropPair ctrlPropPair : ctrlProps.keySet()) {
			switch (ctrlPropPair.getProp()) { //Executes in order
				case LOADDATA:
					loadDataResponse = ActivityDefinitionUtils.validateLoadData(activityDef, oResponse);
					break;
				case RELOADDATA:
					loadDataResponse = ActivityDefinitionUtils.validateReloadData(activityDef, oResponse);
					break;
				default:
					// Do nothing
					break;
			}
		}
		
		return loadDataResponse;
	}
	
	@Deprecated
	public static LoadDataResponse validateReloadData(ActivityDefinition activityDef, PresentationMessage oResponse) {
		LoadDataResponse loadDataResponse = null;
		if (oResponse.getCtrlProperties().containsKey(new CtrlPropPair("", CtrlPropEnum.RELOADDATA))) {
			try {
				AppServerAdapter.getInstance().loadData(activityDef, activityDef.getLastLoadDataRequest());
			} catch (Throwable ex) {
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
		return loadDataResponse;
	}
	
	@Deprecated
	/**
	 * Checks a response for any LOADDATA CtrlProps, and if found, will load the
	 * passed ActivityDefinition that response belongs to with whatever the CtrlProp
	 * value was as the 'loadRowid'
	 * 
	 * @param activityDef
	 *            ActivityDefinition to load and/or the response belongs to
	 * @param oResponse
	 *            PresentationMessage response to be checked
	 * @return LoadDataResponse 
	 */
	public static LoadDataResponse validateLoadData(ActivityDefinition activityDef, PresentationMessage oResponse) {
		LoadDataResponse loadDataResponse = null;
		
		String loadRowid = oResponse.getCtrlProperties().get(new CtrlPropPair("", CtrlPropEnum.LOADDATA));
		if (loadRowid != null) {
			LoadDataModeEnum mode = LoadDataModeEnum.ADD;
			if (loadRowid != null && !loadRowid.isEmpty()) {
				if (ActivityTypeEnum.DASHBOARD_SECTION.getValue()
						.equalsIgnoreCase(activityDef.getModel().getActivityType())) {
					mode = LoadDataModeEnum.VIEW;
				} else {
					mode = LoadDataModeEnum.EDIT;
				}
			}
			loadDataResponse = loadData(activityDef, mode, loadRowid);
		}
		return loadDataResponse; 
	}
	
	public static List<String> validationFunctions(ActivityDefinition activityDef, String ipFunction) {
		List<String> validationFunctions = new ArrayList<String>();
		for (CtrlPropPair ctrlProp : activityDef.getModel().getCtrlProperties().keySet()) {
			if (ctrlProp.getProp() == CtrlPropEnum.VALIDATIONFUNCTION) {
				if (ipFunction.equals(activityDef.getModel().getCtrlProp(ctrlProp.getName(), CtrlPropEnum.VALIDATIONFUNCTION))) {
					validationFunctions.add(ctrlProp.getName());
				}
			}
		}
		return validationFunctions;
	}
	
	
	/**
	 * <p>
	 * Resolves an 'ipFunction' string (As it appears as a query string param) and
	 * attempts to resolve it to its true tableField value by looking through the
	 * passed ActivityDefinition, then creating a String Array containing two
	 * elements representing table and field
	 * </p>
	 *
	 * @param ipFunction
	 *            String representing the ipfunction
	 * @param activityDef
	 *            The ActivityDefinition that contains the tableField
	 * @return A String Array {table, field}
	 */
	public static String[] resolveIpFunction(ActivityDefinition activityDef, String ipFunction) {

		List<String> validationFunctions = validationFunctions(activityDef, ipFunction);
		if (validationFunctions.size() > 1) {
			IllegalArgumentException ex = new IllegalArgumentException(
					"Ambiguous IpFunction, exact field specification required. Possible fields: " + validationFunctions.toString());
			//ex.printStackTrace();
			throw ex;
		}

		String[] tableField = null;
		
		List<String> matches = new ArrayList<String>();
		for (String table : activityDef.getDataSourceDefinitionMap().keySet()) {
			if (ipFunction.startsWith(table)) {
				matches.add(table);
			}
		}
		while (!matches.isEmpty() && tableField == null) {
			int longestIndex = 0;
			int length = 0;
			for (int i = 0; i < matches.size(); i++) {
				if (matches.get(i).length() > length) {
					length = matches.get(i).length();
					longestIndex = i;
				}
			}
			matches.remove(longestIndex);

			String[] potential = new String[] { ipFunction.substring(0, length), ipFunction.substring(length) };
			
			if (activityDef.getDataSourceDefinitionMap().containsKey(potential[0])
					&& activityDef.getDataSourceDefinitionMap().get(potential[0]).getFieldDefinitionMap()
							.containsKey(potential[1])) {
				tableField = potential;
			}
		}

		if (!validationFunctions.isEmpty() && tableField != null) {
			IllegalArgumentException ex = new IllegalArgumentException(
					"Ambiguous IpFunction, both a tablefield and validation function matched the given IpFunction:\r\ntablefield: "
							+ tableField[0] + "." + tableField[1] + "\r\nvalidation function: "
							+ validationFunctions.get(0));
			//ex.printStackTrace();
			throw ex;
		}
		
		if (!validationFunctions.isEmpty()) {
			tableField = validationFunctions.get(0).split("\\.");
			tableField = new String[] { tableField[0], tableField[1] };
		}
		
		if (tableField == null) {
			throw new NoSuchElementException(
					"No ValidationFunction control property or table.field exists for given IpFunction String <"
							+ ipFunction + ">");
		}

		if (mlDebugTrace) {
			System.out.println(
					"IpFunction <" + ipFunction + "> resolved to <" + tableField[0] + "." + tableField[1] + ">");
		}

		return tableField;
	}
	
	/**
	 * Checks a response and the passed ActivityDefinition if fields have been
	 * clicked and have a launchContract, returning a list of
	 * ChildActivityDefinitions. You will have to load the children separately.
	 * 
	 * @param activityDef
	 *            ActivityDefinition containing contracts and data to launch a
	 *            childActivityDefinition
	 * @param oResponse
	 *            PresentationMessage response to check fields
	 * @return List of ChildActivityDefinitions
	 */
	public static Set<ChildActivityDefinition> validateLaunchContract(ActivityDefinition activityDef, PresentationMessage oResponse) {
		
		Set<ChildActivityDefinition> childActivityDefList = new HashSet<ChildActivityDefinition>();
	
		for (CtrlPropPair ctrlPropPair: oResponse.getCtrlProperties().keySet()) {
			
			if (ctrlPropPair.getProp() == CtrlPropEnum.BUTTONCLICK) {
				if (activityDef.getModel().getCtrlProp(ctrlPropPair.getName(), CtrlPropEnum.LAUNCHCONTRACT) != null) {
					String[] tableField = ctrlPropPair.getName().split("\\.");
					ChildActivityDefinition childActivityDef = ChildActivityDefinition.newInstance(activityDef, tableField[0], tableField[1]);
					//loadChildActivity(childActivityDef);
					childActivityDefList.add(childActivityDef);
				}
			}
		}
		
		return childActivityDefList;
	}
	
	/**
	 * This validates a field and checks for any errors
	 * 
	 * @param activityDef
	 * @param table
	 * @param field
	 * @return PresentationMessage response from the validation request
	 */
	public static PresentationMessage validateField(ActivityDefinition activityDef, String table, String field) {
		return validateField(activityDef, table, field, true);
	}
	
	/**
	 * This validates a field with an option to check for errors or not
	 * 
	 * @param activityDef
	 * @param table
	 * @param field
	 * @param checkForErrors
	 * @return	PresentationMessage response from the validation request
	 */
	public static PresentationMessage validateField(ActivityDefinition activityDef, String table, String field,
			boolean checkForErrors) {

		PresentationMessage oResponse = null;
		try {
			
			boolean runSave = false;
			if ("button".equals(table)) {
				final String activitySaveButton = "activitySaveButton";
				final String extraControlButtons = "extraControlButtons";
				
				if (activityDef.getModel().getViewDetails().get(extraControlButtons) != null) {
					JsonArray extras = (JsonArray) activityDef.getModel().getViewDetails().get(extraControlButtons);
					for (Object object : extras) {
						JsonObject extra = (JsonObject) object;
						if (field.equals(extra.get("name")) && Boolean.TRUE.equals(extra.get(activitySaveButton))) {
							runSave = true;
							break;
						}
					}
				} else {
					for (DataSourceDefinition dsDef : activityDef.getDataSourceDefinitionMap().values()) {
						ViewDefinition viewDef = dsDef.getViewDefinition();
						Map<String, Object> fieldViewDef = viewDef.getFieldViewDefinition(field);
						if (fieldViewDef != null) {
							runSave = Boolean.parseBoolean(String.valueOf(fieldViewDef.get(activitySaveButton)));
							break;
						}
					}
				}
			}
			
			if (runSave) {
				SaveDataRequest saveDataRequest = new SaveDataRequest();
				saveDataRequest.setParam("eventCtrlName", "button."+field);
				oResponse = saveData(activityDef, saveDataRequest, false, false);
			} else {
				oResponse = activityDef.validateField(table, field);
			}
			
			if(mlDebugTrace) {
				System.out.println("ValidationResponse: " + oResponse.serializeMessage());
			}
			
			if (checkForErrors) {
				assertFalse("Error returned from validation: " + oResponse.getErrorMsg(), oResponse.hasStopErrors());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return oResponse;
	}

	/**
	 * Initialise an activityDefinition with a service
	 * 
	 * @param activityDef
	 * @param service
	 * @return LoadActivityResponse (ActivityDefinition model)
	 */
	public static LoadActivityResponse loadActivity(ActivityDefinition activityDef, String service) {
		return loadActivity(activityDef, new LoadActivityRequest(), service);
	}

	/**
	 * Load an activity with an option for setting parameters
	 * 
	 * @param activityDef
	 * @param service
	 * @param loadActivityRequest
	 * @return LoadActivityResponse (ActivityDefinition model)
	 */
	public static LoadActivityResponse loadActivity(ActivityDefinition activityDef, LoadActivityRequest loadActivityRequest, String service) {

		activityDef.setService(service);
		
		try {
			
			if(mlDebugTrace) {
				System.out.println("loadActivityRequest: " + loadActivityRequest.serializeMessage());
			}
			
			AppServerAdapter.getInstance().loadActivityDef(activityDef, loadActivityRequest);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return activityDef.getModel();
	}

	/**
	 * Create a LoadDataRequest for a specified ActivityDefinition with options for modes and loadRowIDs
	 * 
	 * @param activityDef
	 * @param loadMode
	 * @param loadRowid
	 * @return LoadDataResponse from the LoadDataRequest
	 */
	public static LoadDataResponse loadData(ActivityDefinition activityDef, LoadDataModeEnum loadMode, String loadRowid) {
		return loadData(activityDef, new LoadDataRequest(loadMode), loadRowid);
	}

	public static LoadDataResponse loadData(ActivityDefinition activityDef, LoadDataRequest loadDataRequest, String loadRowid) {
		
		LoadDataResponse loadDataResponse = null;
		try {
			loadDataRequest.setLoadRowid(loadRowid);
			
			if(mlDebugTrace) {
				System.out.println("loadDataRequest: " + loadDataRequest.serializeMessage());
			}
			
			loadDataResponse = AppServerAdapter.getInstance().loadData(activityDef, loadDataRequest);
			
			if(mlDebugTrace) {
				System.out.println("loadDataResponse: " + loadDataResponse.serializeMessage());
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		return loadDataResponse;
		
	}

	/**
	 * Create a SaveDataRequest for a specified ActivityDefinition with options to set saveData options 'stopOnWarning' and 'validateOnly'
	 * 
	 * @param activityDef
	 * @param stopOnWarning
	 * @param validateOnly
	 * @return	SaveDataResponse object of the response
	 */
	public static SaveDataResponse saveData(ActivityDefinition activityDef, boolean stopOnWarning,
			boolean validateOnly) {
		return saveData(activityDef, new SaveDataRequest(), stopOnWarning, validateOnly);
	}

	/**
	 * Create a SaveDataRequest for a specified ActivityDefinition and set parameters in the request with options to set saveData options 'stopOnWarning' and 'validateOnly'
	 * 
	 * @param activityDef
	 * @param saveDataRequest
	 * @param stopOnWarning
	 * @param validateOnly
	 * @return SaveDataResponse object of the response
	 */
	public static SaveDataResponse saveData(ActivityDefinition activityDef, SaveDataRequest saveDataRequest,
			boolean stopOnWarning, boolean validateOnly) {

		SaveDataResponse saveDataResponse = null;
		try {
			Map<String, DataSourceDefinition> map = new LinkedHashMap<String, DataSourceDefinition>();
			for (String key : activityDef.getDataSourceDefinitionMap().keySet()) {
				DataSourceDefinition dsDef = activityDef.getDataSourceDefinitionMap().get(key);
				if (dsDef != activityDef.getResultsDataSourceDefinition()
						&& dsDef != activityDef.getGenSearchDataSourceDefinition()
						&& dsDef != activityDef.getAdvSearchDataSourceDefinition()) {
					map.put(key, dsDef);
				}
			}
			
			saveDataRequest.buildSaveDataJson(false, map);
			saveDataRequest.setStopOnWarning(stopOnWarning);
			saveDataRequest.setValidateOnly(validateOnly);
			
			if (saveDataRequest.getParamAsString("eventCtrlName") == null) {
				saveDataRequest.setParam("eventCtrlName", "button.Save");
			}
			
			if(mlDebugTrace) {
				System.out.println("saveDataRequest: " + saveDataRequest.serializeMessage());
			}
			
			saveDataResponse = AppServerAdapter.getInstance().saveData(activityDef, saveDataRequest);

			if(mlDebugTrace) {
				System.out.println("saveDataResponse: " + saveDataResponse.serializeMessage());
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return saveDataResponse;
	}
	
	public static SaveDataResponse saveDataDelete(ActivityDefinition activityDef, boolean stopOnWarning,
			boolean validateOnly) {
		SaveDataResponse saveDataResponse = null;
		try {
			SaveDataRequest saveDataRequest = new SaveDataRequest(true, activityDef.getDataSourceDefinitionMap());
			saveDataRequest.setStopOnWarning(stopOnWarning);
			saveDataRequest.setValidateOnly(validateOnly);
			
			saveDataRequest.setParam("eventCtrlName", "button.Delete");
			
			saveDataResponse = AppServerAdapter.getInstance().saveData(activityDef, saveDataRequest);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return saveDataResponse;
	}
	
	/**
	 * Create a delete SaveDataRequest for a specified ActivityDefinition
	 * 
	 * @param activityDef
	 * @return SaveDataResponse of the response
	 */
	public static SaveDataResponse saveDataDelete(ActivityDefinition activityDef) {
		return saveDataDelete(activityDef, false, false);
	}
	
	/**
	 * Create a getResults request for a given ActivityDefinition and set parameters
	 * 
	 * @param activityDef
	 * @param getResultsRequest
	 */
	public static GetResultsResponse getResults(ActivityDefinition activityDef, GetResultsRequest getResultsRequest) {
		GetResultsResponse getResultsResponse = null;
		
		try {
			getResultsResponse = AppServerAdapter.getInstance().getResults(activityDef, getResultsRequest);
		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		return getResultsResponse;
	}
	
	public static ActivityTaskResponse submitActivityTask(ActivityDefinition activityDef, boolean stopOnWarning, boolean validateOnly) {
		return submitActivityTask(activityDef, new ActivityTaskRequest(), stopOnWarning, validateOnly);
	}

	public static ActivityTaskResponse submitActivityTask(ActivityDefinition activityDef,
			ActivityTaskRequest activityTaskRequest, boolean stopOnWarning, boolean validateOnly) {

		ActivityTaskResponse activityTaskResponse = null;
		try {
			Map<String, DataSourceDefinition> map = new LinkedHashMap<String, DataSourceDefinition>();
			for (String key : activityDef.getDataSourceDefinitionMap().keySet()) {
				DataSourceDefinition dsDef = activityDef.getDataSourceDefinitionMap().get(key);
				if (dsDef != activityDef.getResultsDataSourceDefinition()
						&& dsDef != activityDef.getGenSearchDataSourceDefinition()
						&& dsDef != activityDef.getAdvSearchDataSourceDefinition()) {
					map.put(key, dsDef);
				}
			}

			activityTaskRequest.buildSaveDataJson(false, map);
			activityTaskRequest.setStopOnWarning(stopOnWarning);
			activityTaskRequest.setValidateOnly(validateOnly);
			
			activityTaskRequest.setParam("eventCtrlName", "button.Submit");

			if (mlDebugTrace) {
				System.out.println("activityTaskRequest: " + activityTaskRequest.serializeMessage());
			}

			activityTaskResponse = AppServerAdapter.getInstance().submitActivityTask(activityDef, activityTaskRequest);

			if (mlDebugTrace) {
				System.out.println("activityTaskResponse: " + activityTaskResponse.serializeMessage());
			}

		} catch (Throwable e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		return activityTaskResponse;
	}

	/**
	 * Select a record that matches the specified primaryKey values for a
	 * getResultsDataSourceDefinition in an ActivityDefinition.
	 * 
	 * @param activityDef
	 *            ActivityDefinition containing a getResultsDataSourceDefinition
	 * @param primaryKeyField
	 *            String that represents the primary key field
	 * @param primaryKeyValue
	 *            Object value belonging to the desired record
	 * @return Record that matched the primaryKeyField and primaryKeyValue
	 */
	public static Record selectResults(ActivityDefinition activityDef, String searchField, Object searchValue) {
		
		if (activityDef.getResultsDataSourceDefinition() == null) {
			throw new IllegalStateException("getResults was not called for this ActivityDefinition");
		}
		
		Record loadDataRequest = null;
		boolean unique = true;
		for (Record record : activityDef.getResultsDataSourceDefinition().getRecordList()) {
			if (searchValue.equals(record.getFieldValueAsObject(searchField))) {
				
				if (!unique) {
					IllegalArgumentException ex = new IllegalArgumentException("The result search criteria was not unique, multiple value matches were found in the record list");
					ex.fillInStackTrace();
					throw ex;
				}
				
				loadDataRequest = record;
				unique = false;
				
			}
		}
		
		return loadDataRequest;
	}
	
	/**
	 * Creates a Map of record lists where the key is the table name, effectively
	 * acting as a DataSourceDefinition. 'Lazy' as it this does not have a backing
	 * fieldDefinition collection that DataSourceDefinitions have and it was a temp
	 * name that stuck.
	 * 
	 * @param oResponse
	 *            PresentationMessage that contains dataSets
	 * @return Map<String, List<Record>> containing table and record lists;
	 */
	public static Map<String, List<Record>> lazyDataSourceDefinition(PresentationMessage oResponse) {
		Map<String, List<Record>> map = new HashMap<String, List<Record>>();
		for (DataSet dataSet : oResponse.getDataSets()) {
			for (String dataSetName : dataSet.getDataSet().keySet()) {
				
				JsonObject jsDataSet = (JsonObject) dataSet.getDataSet().get(dataSetName);
				for (String tableName : jsDataSet.keySet()) {
					if (!tableName.contains(":")) {
						List<Record> records = new ArrayList<Record>();
						map.put(tableName, records);
						JsonArray jsTable = (JsonArray) jsDataSet.get(tableName);
						if (jsTable != null) {
							//dsDef.setDataSetID(dataSet.getDataSetID());
							for (int i = 0; i < jsTable.size(); i++) {
								JsonObject record = (JsonObject) jsTable.get(i);
								Record newRecord = new Record();
								for (Iterator<String> iterator = record.keySet().iterator(); iterator.hasNext();) {
									String property = (String) iterator.next();
									newRecord.setFieldValue(property, record.get(property));
								}
								records.add(newRecord);
							}
						}
					}
				}
			}
		}
		
		return map;
	}
	
}
