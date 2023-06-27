package fluid.test.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fluid.test.adapter.AppServerAdapter;
import fluid.test.json.JsonObject;
import fluid.test.message.AbstractMessage;
import fluid.test.message.CtrlProp;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.SaveDataResponse;

/**
 * <p>
 * This class takes a (Parent)ActivityDefinition and
 * {@link #createLaunchContract(String, String) reads a specified field}
 * LAUNCHCONTRACT, creating a new LoadActivityRequest and a possible
 * LoadDataRequest to which this object can be initialised and used as an
 * ActivityDefinition for the required functions and then saved to the Parent
 * ActivityDefinition when finished.
 * </p>
 * <p>
 * The <code>ChildActivityDefinition</code> class was made for the purpose of
 * replicating/automating the Fluid sub-activity process where an
 * <code>ActivityDefinition</code> would call and pass parameters from itself to
 * a separate ActivityDefinition and potentially expect a specific response all
 * based on the calling ActivityDefinition control property type LAUNCHCONTRACT.
 * </p>
 * <p>
 * The calling ActivityDefinition can be referred to as a "Parent"
 * ActivityDefinition while the called "sub-activity" is referenced as a "Child"
 * ActivityDefinition, this "ChildActivityDefinition" functions the same as an
 * ActivityDefinition but with some exceptions that it is set with parameters
 * and may or may not return data back to the Parent ActivityDefinition.
 * </p>
 * 
 * <p>
 * A field may have a LAUNCHCONTRACT CtrlProp attached, defining what request
 * type it is (launchContractType) and what parameters to pass onto a generated
 * ActivityDefinition as well as what to return.
 * </p>
 * 
 * <p>
 * The values this LAUNCHCONTRACT variable contains is parameters to this
 * request and in the case of LOADDATA, the <code>LoadActivityRequest</code> as
 * well. Optionally the LAUNCHCONTRACT may be followed with a "RETURN" section,
 * this section contains the values to be returned to the Parent
 * ActivityDefinition.
 * </p>
 *
 * @author vercin
 */
public class ChildActivityDefinition extends ActivityDefinitionResponseCascader {
	
	/**
	 * <p>An Enum listing the different LAUNCHCONTRACT types, 'RETURN' is included
	 * because it's visually on the same level as 'SEARCH' and potentially 'LOADDATA'
	 * can include it.</p>
	 * 
	 * @author vercin
	 */
	public enum LaunchContractType{
		LOADDATA,
		SEARCH,
		RETURN,
		ADVSEARCH,
		LOADACTIVITYDEF;
	}
	
	public static final String VALUE_OF_FUNCTION = "valueOf(";
	public static final String PASS_THROUGH_FUNCTION = "passThrough[";
	public static final String RELOAD_SEARCH_CRITERIA = "reloadSearchCriteria";
	
	private ActivityDefinition parentActivityDef;
	private LaunchContractType type;
	private String returnProperties;
	private String parentActivityName;
	private String tableField;
	
	private boolean disabled = false;
	
	public static ChildActivityDefinition newInstance(ActivityDefinition parentActivityDef) {
		ChildActivityDefinition childActivityDef = new ChildActivityDefinition(parentActivityDef);
		return childActivityDef;
	}
	
	/**
	 * Initialises the object, sets a parent ActivityDefinition and immediately
	 * attempts to {@link #createLaunchContract(String, String) create the launch
	 * contract}
	 * 
	 * @param parentActivityDef
	 * @param table String representing a table name
	 * @param field String representing a field name
	 */
	public static ChildActivityDefinition newInstance(ActivityDefinition parentActivityDef, String table, String field) {
		ChildActivityDefinition childActivityDef = newInstance(parentActivityDef);
		childActivityDef.createLaunchContract(table, field);
		return childActivityDef;
	}
	
	/**
	 * <p>
	 * Initialises the object and sets a parent ActivityDefinition.
	 * </p>
	 * <p>
	 * Requires calling of method <code>createLaunchContract</code> separately.
	 * </p>
	 * 
	 * @see #createLaunchContract(String, String)
	 * @param parentActivityDef
	 */
	private ChildActivityDefinition(ActivityDefinition parentActivityDef) {
		super();
		this.parentActivityName = parentActivityDef.getModel().getActivity();
		this.parentActivityDef = parentActivityDef;
	}
	
	private boolean isDisabled() {
		return this.disabled;
	}
	
	private void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	/**
	 * @return String value representing the name of the parent Activity
	 */
	public String getParentActivityName() {
		return this.parentActivityName;
	}

	/**
	 * @return LaunchContractType Enum value representing the type of this child activity, for example: SEARCH is a getResults request, LOADDATA is a loadData request.
	 */
	public LaunchContractType getType() {
		return this.type;
	}

	/**
	 * @return String value representing what specific values are to be returned to the parent activity from this child activity
	 */
	public String getReturnProperties() {
		return this.returnProperties;
	}

	/**
	 * @return String value representing what the 'table.field' was to find the applicable 'LAUNCHCONTRACT'
	 */
	public String getTableField() {
		return this.tableField;
	}
	
	/**
	 * @return The parent activity
	 */
	public ActivityDefinition getParentActivityDefinition() {
		return parentActivityDef;
	}
	
	private void setParentActivityDefinition(ActivityDefinition activityDef) {
		this.parentActivityDef = activityDef;
		this.parentActivityName = activityDef.getModel().getActivity();
	}
	
	/**
	 * @param tableField Set this object's tableField value
	 */
	private void setTableField(String tableField) {
		this.tableField = tableField;
	}

	/**
	 * @param type	Set this object's type
	 */
	public void setType(LaunchContractType type) {
		this.type = type;
	}

	/**
	 * @param returnProperties	Set this object's returnProperties
	 */
	private void setReturnProperties(String returnProperties) {
		this.returnProperties = returnProperties;
	}
	
	@Override
	public LoadDataResponse loadCurrentResultRecord() throws Throwable {
		LoadDataResponse loadDataResponse;
		switch (getType()) {
			case ADVSEARCH:
			case SEARCH:
				String crudOverride = getLastLoadActivityRequest().getParamAsString("loadDataMode");
				String primaryKeyValue = getResultsDataSourceDefinition().getCurrentRecord()
						.getFieldValueAsString(getResultsDataSourceDefinition().getPrimaryKey());
				
				if (crudOverride != null) {
					LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.valueOf(crudOverride));
					
					loadDataRequest.setLoadRowid(primaryKeyValue);
					
					loadDataResponse = AppServerAdapter.getInstance().loadData(getParentActivityDefinition(),
							loadDataRequest);
					
					if (loadDataResponse.hasStopErrors()) {
						Throwable e = new Throwable("Error in Result LoadDataResponse: " + loadDataResponse.getErrorMsg());
						e.fillInStackTrace();
						throw e;
					} else {
						setClosed(true);
					}
				} else {
					LoadDataRequest loadDataRequest = new LoadDataRequest(getLastLoadActivityRequest().serializeMessage());
					
					loadDataRequest.setParam("selectedList", primaryKeyValue);
					loadDataRequest.setLoadMode(LoadDataModeEnum.EDIT);
					loadDataRequest.setLoadRowid(primaryKeyValue);
					
					loadDataResponse = AppServerAdapter.getInstance().loadData(this, loadDataRequest);
	
					if (loadDataResponse.hasStopErrors()) {
						Throwable e = new Throwable("Error in Result LoadDataResponse: " + loadDataResponse.getErrorMsg());
						e.fillInStackTrace();
						throw e;
					} else {
						setClosed(true);
						saveResponseToParent(loadDataResponse);
					}
				}
				break;
			default:
				loadDataResponse = super.loadCurrentResultRecord();
				break;
		}

		return loadDataResponse;
	}
	
	/**
	 * <p>After initialising the activity, this goes through the loadActivityRequest
	 * and adds any SCREENVALUE CtrlProps (Excluding "reloadSearchCriteria") that do
	 * not exist in the initialised model field definitions, and adds them as a new
	 * field definition with "TEXT" type.</p>
	 * 
	 * @param String
	 *            Serialised LoadActivityResponse from a LoadActivityRequest
	 */
	@Override
	public void initializeModel(LoadActivityResponse loadActivityResponse) throws Throwable {
		if (getType() == LaunchContractType.LOADDATA) {
			setDisabled(true); //Disable buttons being clicked on loadActivityDef when contract is loadData
		}
		
		super.initializeModel(loadActivityResponse);
		
		if (getType() == LaunchContractType.SEARCH) {
			for (CtrlPropPair ctrlPropPair: getLastLoadActivityRequest().getCtrlProperties().keySet()) {
				if (ctrlPropPair.getProp() == CtrlPropEnum.SCREENVALUE) {
					
					String[] tableField = ctrlPropPair.getName().split("\\.");
					
					if (!tableField[1].equals(RELOAD_SEARCH_CRITERIA)) {
						if (!getGenSearchDataSourceDefinition().getFieldDefinitionMap().containsKey(tableField[1])) {
							if (getGenSearchDataSourceDefinition() == getDataSourceDefinitionMap().get(tableField[0])) {
								getGenSearchDataSourceDefinition().addFieldDefinition(new FieldDefinition(tableField[1], "TEXT"));
							}else if (getAdvSearchDataSourceDefinition() == getDataSourceDefinitionMap().get(tableField[0])) {
								getAdvSearchDataSourceDefinition().addFieldDefinition(new FieldDefinition(tableField[1], "TEXT"));
							}
						}
					}
				}
			}
			processResponse(getLastLoadActivityRequest());
		}
	}
	
	@Override
	protected void applyClick(List<CtrlProp> buttonClickEventForList) throws Throwable {
		try {
			if (!isDisabled()) {
				super.applyClick(buttonClickEventForList);
			}			
		} finally {
			setDisabled(false);
		}
	}
	
	/**
	 * Checks the response if the parent of this activity has been closed, if so,
	 * sets {@link #setClosed(boolean)} to true
	 * 
	 * @param beforeSaveDataResponse
	 *            SaveDataResponse from a SaveDataRequest this activity sent
	 */
	@Override
	public void processSaveDataResponse(SaveDataResponse saveDataResponse) throws Throwable {
		if (saveDataResponse.getCtrlProp("window.parent", CtrlPropEnum.ACTIVITYCLOSE) != null) {
			getParentActivityDefinition().setClosed(true);
		}
		super.processSaveDataResponse(saveDataResponse);
		
		if (isClosed() && !getParentActivityDefinition().isClosed()) {
			
			SaveDataResponse saveDataResponseDeepClone = new SaveDataResponse(saveDataResponse.serializeMessage());
			Map<CtrlPropPair, String> ctrlPropCopy = new LinkedHashMap<CtrlPropPair, String>(saveDataResponse.getCtrlProperties());
			
			for (Map.Entry<CtrlPropPair, String> entry : ctrlPropCopy.entrySet()) {
				saveDataResponseDeepClone.getCtrlProperties().put(entry.getKey(), entry.getValue());
				if (entry.getKey().getProp() == CtrlPropEnum.ACTIVITYCLOSE && "window.dummy".equals(entry.getKey().getName())) {
					saveDataResponseDeepClone.getCtrlProperties().clear();
				}
			}
			saveResponseToParent(saveDataResponseDeepClone);
		}
	}
	
	/**
	 * <p>
	 * Creates a <code>LoadActivityRequest</code> and a <code>Request</code>
	 * containing either a <code>LoadDataRequest</code> or
	 * <code>GetResultsRequest</code> based on a field's LAUNCHCONTRACT CtrlProp
	 * value:
	 * </p>
	 * <ul>
	 * <li>LOADDATA<br>
	 * The launchContract string format is comma separated, each value of that is
	 * then separated by "equals" (=) with the left hand side being the
	 * LoadDataRequest param key and the right hand side being a table.key string
	 * that is used to fetch the value from the parent
	 * <code>ActivityDefinition</code> (If the value could not be found, the
	 * searching value is now the value). If the comma separated value is not equals
	 * separated, it is assumed to be the loadRowId, in which if a loadRowId exists,
	 * the <code>LoadDataModeEnum</code> for the <code>LoadDataRequest</code> is
	 * <strong>EDIT</strong>, and not <strong>ADD</strong>. Terminating stage is
	 * <code>SaveDataRequest</code> where the data needed will be returned in a
	 * <code>SaveDataResponse</code>.</li>
	 * <li>GETRESULTS<br>
	 * <code>LoadActivityRequest</code> has the launchContract comma separated
	 * values as control properties (CtrlProps) instead of parameters. "RETURN"
	 * section specifies what keys values are to be returned to when the
	 * activityDefinition terminating stage is called, if absent the return value
	 * (if any) will be in a blank named control property. Terminating stage is
	 * <code>LoadDataRequest</code> where the data needed will be returned in a
	 * <code>LoadDataResponse</code>.</li>
	 * </ul>
	 * <p>
	 * This saves the <code>LoadActivityRequest</code> and <code>Request</code> to
	 * object variables to be used and initialised conventionally.
	 * </p>
	 * <p>
	 * There is a possibility of a nested 'valueOf()' function where the
	 * launchContract should be, as well as inside any of the LaunchContract Headers
	 * (SEARCH, LOADDATA). This function points to a table value that contains a
	 * referenced launchContract and returns that in replacement.
	 * </p>
	 *
	 * @param table
	 *            String of the table that contains the specified field
	 * @param field
	 *            String of the field that contains the value required
	 */
	public final void createLaunchContract(final String table, final String field) {
		
		String tableField = table + "." + field;
		setTableField(tableField);
		setReturnProperties(null);
		
		final String launchActivity;
		if ("button".equals(table) && !"Edit".equalsIgnoreCase(field)) {
			launchActivity = getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.BUTTONACTIVITY);
		} else {
			setType(LaunchContractType.SEARCH);
			launchActivity = getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.LOOKUPFINDER);
		}
		
		LoadActivityRequest subActivityRequest = new LoadActivityRequest();
		
		String launchContractString = getParentActivityDefinition().getModel().getCtrlProp(tableField, CtrlPropEnum.LAUNCHCONTRACT);
		
		if (launchContractString != null && !launchContractString.isEmpty()) {
			
			if (launchContractString.startsWith(VALUE_OF_FUNCTION)) {
				launchContractString = getValueOf(launchContractString);
				
				if (launchContractString == null || launchContractString.isEmpty()) {
					 NullPointerException ex = new NullPointerException("LaunchContract 'valueOf()' function for \"" + tableField + "\" returned nothing");
					 ex.fillInStackTrace();
					 throw ex;
				}
			}
			
			Pattern pattern = Pattern.compile("(?<=\\[)(?:[^\\[\\]]+|\\[[^\\]]+\\]|)+"); // The last pipe character (3rd last char) determines if empty values are allowed in the square brackets
			Matcher matcher = pattern.matcher(launchContractString);
	    	
			while(matcher.find()) {
	    		
	    		String contract = matcher.group();
	    		String contractType;
	    		
	    		if (contract.isEmpty()) {
	    			contractType = launchContractString.substring(0, launchContractString.indexOf("[")).toUpperCase();
	    			launchContractString = launchContractString.substring(launchContractString.indexOf("]") + 1);
	    		}else {
	    			contractType = launchContractString.substring(0, launchContractString.indexOf(contract) - 1).toUpperCase();
	    			launchContractString = launchContractString.replace(launchContractString.substring(0, launchContractString.indexOf(contract) + contract.length() + 1), "");
	    			contract = resolveCriteriaString(contract);
	    		}
	    		
	    		String searchTable = null;
				switch (LaunchContractType.valueOf(contractType)) {
					case RETURN:
						subActivityRequest.setParam("returnCriteria", contract);
						setReturnProperties(contract);
						break;
					case ADVSEARCH:
						searchTable = "advSearch";
					case SEARCH:
						setType(LaunchContractType.SEARCH);
						
						if (searchTable == null) {
							searchTable = "search";
						}
						
						activityCtrlProps(searchTable ,contract, subActivityRequest);
						
						resolveLaunchActivity(subActivityRequest, launchActivity);
						break;
					case LOADACTIVITYDEF:
						setType(LaunchContractType.LOADACTIVITYDEF);
						
						String ipService = null; 
						for (String entry: contract.split("\\,")) {
							entry = entry.trim();
							if (entry.split("\\=").length == 1) {
								if (entry.split("\\.").length >= 2) {
									Object value = findParentActivityValue(entry);
									if (value == null) {
										ipService = "";
									}else{
										ipService = String.valueOf(value);
									}
								} else {
									ipService = entry;
								}
							}
						}
						
						LoadDataRequest loadActivityDefRequest = loadDataRequest(null);
						buildLaunchContract(contract, loadActivityDefRequest);
						buildLaunchContract(contract, subActivityRequest);
						
						resolveLaunchActivity(subActivityRequest, launchActivity);
						setService(ipService);
						
						loadActivityDefRequest.setLoadRowid(getLastLoadDataRequest().getLoadRowid());
						
						setLastLoadDataRequest(loadActivityDefRequest);
						break;
					case LOADDATA:
						setType(LaunchContractType.LOADDATA);
						
						String loadRowid = null;
						for (String entry: contract.split("\\,")) {
							entry = entry.trim();
							if (entry.split("\\=").length == 1) {
								if (entry.split("\\.").length >= 2) {
									Object value = findParentActivityValue(entry);
									if (value == null) {
										loadRowid = "";
									}else{
										loadRowid = String.valueOf(value);
									}
								}else {
									loadRowid = entry;
								}
							}
						}
						
						LoadDataRequest loadDataRequest = loadDataRequest(loadRowid);
						buildLaunchContract(contract, loadDataRequest);
						buildLaunchContract(contract, subActivityRequest);
						
						resolveLaunchActivity(subActivityRequest, launchActivity);
						
						setLastLoadDataRequest(loadDataRequest);
						break;
					default: 
						throw new IllegalArgumentException("Unhandled launch contract type");
				}
	    	}
			
			if (!launchContractString.isEmpty()) {
				throw new StringIndexOutOfBoundsException(
						"Launch contract was incorrectly consumed for <" + tableField + ">.");
			}
			
		} else {
			resolveLaunchActivity(subActivityRequest, launchActivity);
		}
		
		subActivityRequest.setParam("parentActivity", getParentActivityName());
		
    	if (getParentActivityDefinition().getModel().isPasswordReq()) {
    		setMessageKeyPin(getParentActivityDefinition().getMessageKeyPin());
    	}

    	setLastLoadActivityRequest(subActivityRequest);
	}

	private LoadDataRequest loadDataRequest(String loadRowid) {
		
		final LoadDataModeEnum loadDataMode;
		//String activityType = getParentActivityDefinition().getModel().getActivityType();
		if (loadRowid == null) {
			loadDataMode = LoadDataModeEnum.VIEW;
		} else if (!loadRowid.isEmpty()) {
			loadDataMode = LoadDataModeEnum.EDIT;
		} else {
			loadDataMode = LoadDataModeEnum.ADD;
		} 
		
		LoadDataRequest loadDataRequest = new LoadDataRequest(loadDataMode);
		loadDataRequest.setParam("parentActivity", getParentActivityName());
		loadDataRequest.setLoadRowid(loadRowid);
		
		return loadDataRequest;
	}
	
	/**
	 * Resolves a CtrlProp value String that is of either
	 * {@link CtrlPropEnum#BUTTONACTIVITY} or {@link CtrlPropEnum#LOOKUPFINDER} to
	 * the correct ipService and activity reference.
	 * 
	 * @param launchActivity
	 *            String CtrlProp value of either
	 *            {@link CtrlPropEnum#BUTTONACTIVITY} or
	 *            {@link CtrlPropEnum#LOOKUPFINDER}
	 */
	public void resolveLaunchActivity(LoadActivityRequest loadActivityRequest, String launchActivity) {	
		
		if (launchActivity.startsWith(VALUE_OF_FUNCTION)) {
			launchActivity = getValueOf(launchActivity);
		}
		
		String[] split = launchActivity.split("\\,");
		//String activityType = split[0]; // Ain't nobody need this
		String tabId = split[1];
		String fluidService = split[2];
		
		ActivityDefinition pointActivity = resolveTarget(tabId, fluidService);
		
		if (this != pointActivity) {
			pointToActivity(pointActivity);
		} else {
			setService(resolveService(tabId, fluidService));
			getParentActivityDefinition().getChildSet().add(this);  //if it isn't pointing to anything, it's a legitimate child
		}
		loadActivityRequest.setActivity(tabId);
	}
	
	@SuppressWarnings("unchecked")
	public String resolveService(String tabId, String fluidService) {
		if (fluidService.startsWith(PASS_THROUGH_FUNCTION)) {
			String title = fluidService.substring(PASS_THROUGH_FUNCTION.length(), fluidService.lastIndexOf(']')).replaceAll("_", " ");
			
			Map<String, Object> viewDetails = resolveTarget(tabId).getModel().getViewDetails(); 
			List<Object> dashboardLayout = (List<Object>) viewDetails.get("dashboardLayout");
			for (Object layoutObj: dashboardLayout) {
				Map<String, Object> layout = (Map<String, Object>) layoutObj;
				
				List<Object> sections = (List<Object>) layout.get("sections");
				for (Object sectionObj: sections) {
					Map<String, Object> section = (Map<String, Object>) sectionObj;
					if (title.equals((String) section.get("title"))) {
						fluidService = (String) section.get("service");
					}
				}
			}
		}
		return fluidService;
	}
	
	/**
	 * @param tabId
	 * @param fluidService
	 * @return ActivityDefinition
	 */
	private ActivityDefinition resolveTarget(String tabId) {
		ActivityDefinition activityDef;
		if (tabId != null) {
			switch (tabId) {
				case "self":
					activityDef = getParentActivityDefinition();
					break;
				case "parent":
					activityDef = ((ChildActivityDefinition) getParentActivityDefinition()).getParentActivityDefinition();
					break;
				default:
					activityDef = this;
					break;
			}
		} else {
			activityDef = this;
		}
		
		return activityDef;
	}
	
	public ActivityDefinition resolveTarget(String tabId, String fluidService) {
		ActivityDefinition activityDef = resolveTarget(tabId);
		if (fluidService.startsWith(PASS_THROUGH_FUNCTION)) {
			String passThroughService = resolveService(tabId, fluidService);
			ActivityDefinition passThroughActivity = null;
			for (ChildActivityDefinition child : activityDef.getChildSet()) {
				if (child.getHistory().contains(passThroughService)) {
					passThroughActivity = child;
					//break; //No break gets the last (latest) activity if there's duplicates
				}
			}
			if (passThroughActivity != null) {
				activityDef = passThroughActivity;
			} else {
				throw new NullPointerException("There was no children in the activity for the given service: " + getService());
			}
		}
		return activityDef;
	}
	
	@Override
	void pointToActivity(ActivityDefinition activityDef) {
		super.pointToActivity(activityDef);
		if (activityDef instanceof ChildActivityDefinition) {
			setParentActivityDefinition(((ChildActivityDefinition) activityDef).getParentActivityDefinition());
		}
	}
	
	/**
	 * Puts the values that are in the given response to the parent ActivityDefinition
	 * as stated in "returnProperties" or otherwise.
	 * 
	 * @param response
	 * @throws Throwable
	 */
	public final void saveResponseToParent(PresentationMessage response) throws Throwable {
		if (mlDebugTrace) {
			System.err.println("ChildActivityDefinition (incoming) response: " + response.serializeMessage());
		}
		ActivityDefinition parentActivityDef = getParentActivityDefinition();
		String returnProps = getReturnProperties();
		
		if (getType() == LaunchContractType.SEARCH) {
			LoadDataResponse loadDataResponse = (LoadDataResponse) response;
			
			String[] tableField = getTableField().split("\\.");
			
			if (returnProps != null && !returnProps.isEmpty()) {
				String[] pairs = returnProps.split("\\,");
				
				String[][] asyncTableFields = new String[pairs.length][2];
				
				for (int i = 0 ; i < pairs.length ; i ++) {
					String pair = pairs[i];
					String[] tableKey = pair.split("=");
					
					if (tableKey[0].equals("saveSearchCriteria") && tableKey[1].startsWith("y")) {
						String table = tableField[0];
						String field = tableField[1];
						
						Object value = loadDataResponse.getParamAsObject("finderReturn");
						parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
						asyncTableFields[i][0] = table;
						asyncTableFields[i][1] = field;
					} else {
						String[] tableKeySplit = tableKey[0].split("\\.");
						String table = tableKeySplit[0];
						String field = tableKeySplit[1];
						
						Object value = loadDataResponse.getParamAsObject(tableKey[1]);
						parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
						asyncTableFields[i][0] = table;
						asyncTableFields[i][1] = field;
					}
				}
				parentActivityDef.validateAsynchronously(asyncTableFields);
			} else {
				if (response.hasCtrlProps()) {
					String table = tableField[0];
					String field = tableField[1];
					
					Object value = loadDataResponse.getCtrlProp("dummy", CtrlPropEnum.SCREENVALUE);
					parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
					parentActivityDef.validateField(table, field);
					parentActivityDef.validateAsynchronously(new String[][] {{table,field}}); // Despite just being one validation, still needs the method for potential implementations
				}
			}
		} else if (getType() == LaunchContractType.LOADDATA) {
			SaveDataResponse saveDataResponse = (SaveDataResponse) response;
			
			if (returnProps != null && !returnProps.isEmpty()) {
				String[] pairs = returnProps.split("\\,");
				
				String[][] asyncTableFields = new String[pairs.length][2];
				
				for (int i = 0 ; i < pairs.length ; i++ ) {
					String pair = pairs[i];
					String[] tableKey = pair.split("=");
					String[] tableKeySplit = tableKey[0].split("\\.");
					
					String table = tableKeySplit[0];
					String field = tableKeySplit[1];
					Object value = saveDataResponse.getParamAsObject(tableKey[1]);
					
					parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
					parentActivityDef.validateField(table, field);
					asyncTableFields[i][0] = table;
					asyncTableFields[i][1] = field;
				}
				parentActivityDef.validateAsynchronously(asyncTableFields);
			} else {
				parentActivityDef.processResponse(saveDataResponse); 
			}
		}
	}

	/**
	 * Adds parameters to an AbstractMessage based on the LaunchContract
	 * 
	 * @param launchContract
	 * @param valRequest
	 */
	private void buildLaunchContract(String launchContract, AbstractMessage valRequest) {
	
		Object value;
	
		if (launchContract != null && launchContract.length() > 0) {
			// Split comma separated string into an array
			String[] requiredCtrlsArray = launchContract.split("\\,");
	
			if (requiredCtrlsArray != null && requiredCtrlsArray.length > 0) {
				// Iterated over all required controls
				for (int index = 0; index < requiredCtrlsArray.length; index++) {
					String req = requiredCtrlsArray[index].trim();
					
					String[] keyValue = req.split("=");
					
					if (keyValue.length == 2) {
						if (keyValue[0].split("\\.").length != 3 && !keyValue[0].equalsIgnoreCase(RELOAD_SEARCH_CRITERIA)) {
							
							if (keyValue[1].matches("\\[.*\\(.*\\)\\]")) { // Temp-Table required
								String table = keyValue[1].substring(1, keyValue[1].indexOf("("));
								String fieldList = keyValue[1].substring(keyValue[1].indexOf("(") + 1, keyValue[1].indexOf(")"));
								String[] fieldArray = fieldList.split("\\|");
								JsonObject requiredDataTable = getParentActivityDefinition().getRequiredDataTable(table, fieldArray);
								valRequest.setParam(keyValue[0], requiredDataTable);
							}else { // single field value required
								value = findParentActivityValue(keyValue[1]);
								valRequest.setParam(keyValue[0], value);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Adds control properties onto a LoadActivityRequest based on the "LAUNCHCONTRACT".
	 * 
	 * @param contract
	 * @param loadActivityRequest
	 * @return LoadActivityRequest with control properties
	 */
	private void activityCtrlProps(String table, String contract, LoadActivityRequest loadActivityRequest) {
		
		String[] contractKeyStrings = contract.split("\\,");
		
		for (String key : contractKeyStrings) {
			String[] keyArray = key.split("=");
			if (keyArray[0].split("\\.").length >= 2) {
				if (keyArray[1].startsWith("y")) {
					loadActivityRequest.setCtrlProp(keyArray[0].substring(0, keyArray[0].lastIndexOf(".")), CtrlPropEnum.BUTTONCLICK, "");
				}
			}else {
				if (keyArray.length == 2) {
	    			Object ctrlPropValue = findParentActivityValue(keyArray[1]);
			    	loadActivityRequest.setCtrlProp(table + "." + keyArray[0], CtrlPropEnum.SCREENVALUE, Objects.toString(ctrlPropValue, ""));
				}
			}
		}
	}
	
	/**
	 * Gets a value from the parentActivity
	 * 
	 * @param tableFieldName
	 * @return Object value of the requested field
	 */
	public Object findParentActivityValue(String tableFieldName) {
		
		Object fieldValue = tableFieldName;
		
		if (tableFieldName.split("\\.").length >= 2) {

			String tableName = tableFieldName.split("\\.")[0];
			String fieldName = tableFieldName.split("\\.")[1];
			
			if (!getParentActivityDefinition().getDataSourceDefinitionMap().containsKey(tableName)) {
				return fieldValue;
			}
			if (getParentActivityDefinition().getDataSourceDefinitionMap().get(tableName).getRecordList().isEmpty()) {
				return fieldValue;
			}
			
			Record record = getParentActivityDefinition().getDataSourceDefinitionMap().get(tableName).getCurrentRecord();

			if ("button".equalsIgnoreCase(tableName)) {
				return getParentActivityDefinition().getModel().getCtrlProp(tableFieldName, CtrlPropEnum.SCREENVALUE);
			}
			if (record == null) {
				return fieldValue;
			}
			fieldValue = record.getFieldValueAsObject(fieldName);

		}
		return fieldValue;
	}
	
	/**
	 * (Taken from source code)
	 * Find the actual value held in the data reference contained in the valueOf() function
	 * 
	 * @param valueOfDef
	 * @return
	 */
	public String getValueOf(String valueOfDef) {
		String returnValue = "";
		// e.g. valueOf(ttResults.activityDef)
		if (valueOfDef.substring(0, VALUE_OF_FUNCTION.length()).equalsIgnoreCase(VALUE_OF_FUNCTION) && valueOfDef.indexOf(")") >= VALUE_OF_FUNCTION.length() + 1) {
			String criteria = valueOfDef.substring(VALUE_OF_FUNCTION.length(), valueOfDef.indexOf(")"));
			returnValue = String.valueOf(findParentActivityValue(criteria));
			if (returnValue == null)
				returnValue = "";
		}
		return returnValue;
	}
	
	/**
	 * (Taken from source code)
	 * Searches inside launchContract headers to see if valueOf() functions exist, processes them, then returns the result
	 * 
	 * @param criteria
	 * @return
	 */
	public String resolveCriteriaString(String criteria) {
		// if the criteria contains valueOf() functions then resolve them first
		if (criteria != null && criteria.contains(VALUE_OF_FUNCTION)) {
			// e.g.
			// FromCustNum=ttCustomer.CustNum,valueOf(ttCust.contract1),valueOf(ttCust.contract2),ToCustNum=ttCustomer.CustNum,button.find.click=yes
			String[] criteriaArray = criteria.split("\\,");
			if (criteriaArray != null && criteriaArray.length > 0) {
				String newCriteria = "";
				for (int index = 0; index < criteriaArray.length; index++) {
					String entry = criteriaArray[index];
					if (entry.startsWith(VALUE_OF_FUNCTION) && entry.endsWith(")")) {
						entry = getValueOf(entry);
					}
					if (index > 0)
						newCriteria = newCriteria + ",";
					newCriteria = newCriteria + entry;
				}
				criteria = newCriteria;
			}
		}
		return criteria;
	}
	
	@Override
	public String toString() {
		return "{\"childActivityDefinition\":\"" + getModel().getActivity() + "\",\t\"service\":\"" + getService()
				+ "\",\t\"parentActivityDefinition\":\"" + getParentActivityName() + "\",\t\"service\":\""
				+ getParentActivityDefinition().getService() + "\",\t\"launchedBy\":\"" + getTableField() + "\"}";
	}
}
