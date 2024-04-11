package fluid.test.data;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fluid.test.adapter.AppServerAdapter;
import fluid.test.json.JsonObject;
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
	
	private String contract;
	private String returnProperties;
	private String tableField;
	
	private String passThroughName;
	
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
		this.parentActivityDef = parentActivityDef;
	}
	
	public String getContract() {
		return this.contract;
	}
	
	private void setContract(String contract) {
		this.contract = contract;
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
		return this.parentActivityDef.getModel().getActivity();
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
	public void close() {
		super.close();
		getParentActivityDefinition().getChildSet().remove(this);
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
						close();
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
						close();
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
	 * sets {@link #close()} to true
	 * 
	 * @param beforeSaveDataResponse
	 *            SaveDataResponse from a SaveDataRequest this activity sent
	 */
	@Override
	public void processSaveDataResponse(SaveDataResponse saveDataResponse) throws Throwable {
		if (saveDataResponse.getCtrlProp("window.parent", CtrlPropEnum.ACTIVITYCLOSE) != null) {
			getParentActivityDefinition().close();
		}
		
		if (saveDataResponse.getCtrlProp("window.dummy", CtrlPropEnum.ACTIVITYCLOSE) != null
				&& !getParentActivityDefinition().isClosed()) {
			
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
		
		super.processSaveDataResponse(saveDataResponse);
	}

	private boolean isAncestorProperty(final String ctrlName, final CtrlPropEnum ctrlProp, final String ctrlValue,
			PresentationMessage responseMsg,
			Map<ActivityDefinition, PresentationMessage> propsForAncestors) throws Throwable {
		
		final String ancestorStr = "fldAncestor-";

		boolean forAncestor = false;

		if (ctrlName.regionMatches(true, 0, ancestorStr, 0, ancestorStr.length())) { // Ignorecase startswith

			int firstSeparatorIndex = ctrlName.length(); // Assume entire string if no separators found.
			for (int i = ancestorStr.length() - 1; i < ctrlName.length(); i++) {
				if (ctrlName.charAt(i) == '.') {
					firstSeparatorIndex = i;
					break;
				}
			}

			String stackIndexStr = ctrlName.substring(ancestorStr.length(), firstSeparatorIndex);
			int stackIndex;
			try {
				stackIndex = Integer.parseInt(stackIndexStr);
			} catch (NumberFormatException ex) {
				throw new Throwable("Cannot convert <" + stackIndexStr + "> from ancestor CtrlProp name <" + ctrlName + "> into an integer.");
			}
			
			// stackIndex == 0 is the current presenter, so forAncestor = false
			ActivityDefinition ancestor = this;
			for (int i = 0; i < stackIndex; i++) {
				try {
					ancestor = ((ChildActivityDefinition) ancestor).getParentActivityDefinition();
				} catch (ClassCastException ex){
					throw new Throwable("There was no ancestor with a depth of <" + stackIndex + "> for <" + ctrlName + ">, reached depth of <" + i + ">.");
				}
			}

			if (ancestor != this) {
				forAncestor = true;

				PresentationMessage props = propsForAncestors.get(ancestor);
				if (props == null) {
					props = new PresentationMessage();
					propsForAncestors.put(ancestor, props);
				}
				
				String newName;
				if (firstSeparatorIndex == ctrlName.length()) {
					// Name only contained an ancestor reference
					newName = "";
				} else {
					// Get Everything after the first separator
					newName = ctrlName.substring(firstSeparatorIndex + 1, ctrlName.length());
				}
				
				props.setCtrlProp(newName, ctrlProp, ctrlValue);
				
				switch (ctrlProp) {
					case UPDATETABLE:
					case REFRESHTABLE:
						// Get the referenced object in the response
						props.setParam(ctrlValue, responseMsg.getParamAsObject(ctrlValue));
						break;
					default:
						// Do nothing
						break;
				}
			}
		}
		return forAncestor;
	}

	@Override
	protected void processResponseCtrlProps(Entry<CtrlPropPair, String> ctrlPropEntry,
			PresentationMessage responseMessage,
			Set<RunnableThrows> deferred,
			List<UpdateTableDefinition> refreshTables,
			List<UpdateTableDefinition> updateTables,
			List<CtrlProp> buttonClickEventForList,
			Map<ActivityDefinition, PresentationMessage> propsForAncestors) throws Throwable {
		
		String cCtrlName = ctrlPropEntry.getKey().getName();
		CtrlPropEnum oCtrlProp = ctrlPropEntry.getKey().getProp();
		String cPropValue = ctrlPropEntry.getValue();
		
		if (isAncestorProperty(cCtrlName, oCtrlProp, cPropValue, responseMessage, propsForAncestors)) {
			return;
		} else if (CtrlPropEnum.ACTIVITYINJECTPARENT == oCtrlProp) {
			final ActivityDefinition parent = this.getParentActivityDefinition();
			final ActivityDefinition getValuesFrom;
			if ("grandparent".equalsIgnoreCase(cCtrlName)) {
				getValuesFrom = ((ChildActivityDefinition) parent).getParentActivityDefinition();
			} else {
				getValuesFrom = parent;
			}
			
			final ChildActivityDefinition dummy = new ChildActivityDefinition(getValuesFrom);
			dummy.buildInjectedParams(cPropValue);
			parent.setInjectedParams(dummy.getInjectedParams());
			
			String loadRowid = null;
			switch (getType()) {
				case SEARCH:
					deferred.add(() -> parent.processResponse(dummy.getInjectedParams()));
					break;
				case LOADDATA:
					loadRowid = dummy.returnContractIdentifier(dummy.getContract());
				case LOADACTIVITYDEF:
					final LoadDataRequest loadDataRequest = loadDataRequest(loadRowid);
					deferred.add(() ->  {
						AppServerAdapter.getInstance().loadData(parent, loadDataRequest);
					});
					break;
				default:
					throw new IllegalArgumentException("Invalid ChildActivityDefinition type");
			}
		} else {
			super.processResponseCtrlProps(ctrlPropEntry,
					responseMessage,
					deferred,
					refreshTables,
					updateTables,
					buttonClickEventForList,
					propsForAncestors);
		}
	}
	
	protected void buildInjectedParams(String launchContractString) {
		
		setInjectedParams(new PresentationMessage());
		setReturnProperties(null);
		
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
					setReturnProperties(contract);
					break;
				case ADVSEARCH:
					searchTable = "advSearch";
				case SEARCH:
					setType(LaunchContractType.SEARCH);
					if (searchTable == null) {
						searchTable = "search";
					}
					activityCtrlProps(searchTable, contract);
					break;
				case LOADACTIVITYDEF:
					setType(LaunchContractType.LOADACTIVITYDEF);
					buildLaunchContract(contract);
					break;
				case LOADDATA:
					setType(LaunchContractType.LOADDATA);
					buildLaunchContract(contract);
					break;
				default: 
					throw new IllegalArgumentException("Unhandled launch contract type");
			}
    	}
		
		if (!launchContractString.isEmpty()) {
			throw new StringIndexOutOfBoundsException(
					"Launch contract was incorrectly consumed for <" + tableField + ">.");
		}
	}
	
	public String returnContractIdentifier(String contract) {
		String identifier = null; 
		for (String entry: contract.split("\\,")) {
			entry = entry.trim();
			if (entry.split("\\=").length == 1) {
				if (entry.split("\\.").length >= 2) {
					Object value = findParentActivityValue(entry);
					if (value == null) {
						identifier = "";
					}else{
						identifier = String.valueOf(value);
					}
				} else {
					identifier = entry;
				}
			}
		}
		return identifier;
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
		setInjectedParams(new PresentationMessage());
		setReturnProperties(null);
		
		final String launchActivity;
		if (getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.BUTTONACTIVITY) != null) {
			launchActivity = getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.BUTTONACTIVITY);
		} else if (getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.LOOKUPFINDER) != null) {
			setType(LaunchContractType.SEARCH);
			launchActivity = getParentActivityDefinition().getModel().getCtrlProp(getTableField(), CtrlPropEnum.LOOKUPFINDER);
		} else {
			launchActivity = null;
		}
		
		LoadActivityRequest subActivityRequest = new LoadActivityRequest();
		
		String launchContractString = getParentActivityDefinition().getModel().getCtrlProp(tableField, CtrlPropEnum.LAUNCHCONTRACT);
		
		if (launchContractString != null && !launchContractString.isEmpty()) {
			
			buildInjectedParams(launchContractString);
			
			if (getReturnProperties() != null) {
				subActivityRequest.setParam("returnCriteria", contract);
			}
			
			final String contract = getContract();
			
			switch (getType()) {
				case SEARCH:
					subActivityRequest.getCtrlProperties().putAll(getInjectedParams().getCtrlProperties());
					resolveLaunchActivity(subActivityRequest, launchActivity);
					break;
				case LOADACTIVITYDEF:
					String ipService = returnContractIdentifier(contract);
					
					LoadDataRequest loadActivityDefRequest = loadDataRequest(null);
					loadActivityDefRequest.getMoMsgParams().putAll(getInjectedParams().getMoMsgParams());
					subActivityRequest.getMoMsgParams().putAll(getInjectedParams().getMoMsgParams());
					
					resolveLaunchActivity(subActivityRequest, launchActivity);
					setService(ipService);
					
					loadActivityDefRequest.setParam("parentActivity", getParentActivityName());
					loadActivityDefRequest.setLoadRowid(getLastLoadDataRequest().getLoadRowid());
					
					setLastLoadDataRequest(loadActivityDefRequest);
					break;
				case LOADDATA:
					String loadRowid = returnContractIdentifier(contract);
					
					LoadDataRequest loadDataRequest = loadDataRequest(loadRowid);
					subActivityRequest.setCtrlProp("dummy", CtrlPropEnum.LOADDATA, loadRowid);
					loadDataRequest.getMoMsgParams().putAll(getInjectedParams().getMoMsgParams());
					subActivityRequest.getMoMsgParams().putAll(getInjectedParams().getMoMsgParams());
					
					resolveLaunchActivity(subActivityRequest, launchActivity);
					
					loadDataRequest.setParam("parentActivity", getParentActivityName());
					
					setLastLoadDataRequest(loadDataRequest);
					break;
				default: 
					throw new IllegalArgumentException("Invalid ChildActivityDefinition type");
			}
		} else {
			setType(LaunchContractType.LOADDATA);
			resolveLaunchActivity(subActivityRequest, launchActivity);
		}
		
		subActivityRequest.setParam("parentActivity", getParentActivityName());
		
    	if (getParentActivityDefinition().getModel().isPasswordReq()) {
    		setMessageKeyPin(getParentActivityDefinition().getMessageKeyPin());
    	}

    	setLastLoadActivityRequest(subActivityRequest);
    	
    	if (getActivityReference() != null) {
    		getReferenceStack().forEach((activityDef) -> activityDef.setLastLoadActivityRequest(getLastLoadActivityRequest()));
    		getReferenceStack().forEach((activityDef) -> activityDef.setLastLoadDataRequest(getLastLoadDataRequest()));
    	}
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
			
			// The title passThrough can contain multiple activity references (Separated by a semi-colon),
			// we need to catch those others by creating a new child instances with those specific extra names (passThroughName)
			final String title; 
			if (this.passThroughName != null && !this.passThroughName.isEmpty()) { // Only called if explicitly specified by the 'else' block
				title = this.passThroughName;
				this.passThroughName = null;
			} else {
				String titles = fluidService.substring(PASS_THROUGH_FUNCTION.length(), fluidService.lastIndexOf(']')).replaceAll("_", " ");
				String[] titlesArr = titles.split(";");
				
				title = titlesArr[0]; // 'this' current ChildActivityDefinition will handle the first title (Most typically only one entry)
				
				String[] tableField = this.tableField.split("\\.");
				// For potential other entries, create new ChildActivityDefinition and specify name, build their contracts as is currently.
				Set<ChildActivityDefinition> others = new HashSet<ChildActivityDefinition>();
				for (int i = 1 ; i < titlesArr.length ; i++) { // Start after index i, if it exists
					
					ChildActivityDefinition childActivityDef = newInstance(this.parentActivityDef);
					childActivityDef.passThroughName = titlesArr[i];
					childActivityDef.createLaunchContract(tableField[0], tableField[1]);
					
					others.add(childActivityDef);
				}
				
				// After contract has been made, load them
				others.forEach((other) -> loadChildActivity(other));
			}
			
			
			Map<String, Object> viewDetails = resolveTarget(tabId).getModel().getViewDetails(); 
			List<Object> dashboardLayout = (List<Object>) viewDetails.get("dashboardLayout");
			for (Object layoutObj: dashboardLayout) {
				Map<String, Object> layout = (Map<String, Object>) layoutObj;
				
				List<Object> sections = (List<Object>) layout.get("sections");
				for (Object sectionObj: sections) {
					Map<String, Object> section = (Map<String, Object>) sectionObj;
					if (title.equals((String) section.get("title"))) {
						fluidService = (String) section.get("service");
						break;
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
			ChildActivityDefinition childActivityDef = (ChildActivityDefinition) activityDef; 
			setParentActivityDefinition(childActivityDef.getParentActivityDefinition());
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
			
			if (loadDataResponse.hasParam("finderReturn")) {
				String table = tableField[0];
				String field = tableField[1];
				
				Object value = loadDataResponse.getParamAsObject("finderReturn");
				parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
				parentActivityDef.validateAsynchronously(new String[][] {{table,field}});
			} else
			
			if (returnProps != null && !returnProps.isEmpty()) {
				String[] pairs = returnProps.split("\\,");
				
				String[][] asyncTableFields = new String[pairs.length][2];
				
				for (int i = 0 ; i < pairs.length ; i ++) {
					String pair = pairs[i];
					String[] tableKey = pair.split("=");
					
					if (tableKey[0].equals("saveSearchCriteria") && tableKey[1].startsWith("y")) {
						// Implementation of finders remembering assigned values
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
			} 
			
			// Fluid's old style of the finder result process, with the same logic in all it's glory.
			if (loadDataResponse.hasCtrlProps()) {
				String table = tableField[0];
				String field = tableField[1];
				for (Map.Entry<CtrlPropPair, String> entry : loadDataResponse.getCtrlProperties().entrySet()) {
					if (CtrlPropEnum.SCREENVALUE == entry.getKey().getProp()) {
						Object value = entry.getValue();
						parentActivityDef.getDataSourceDefinitionMap().get(table).getCurrentRecord().setFieldValue(field, value);
						parentActivityDef.validateAsynchronously(new String[][] {{table,field}});
					}
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
	private void buildLaunchContract(String launchContract) {
	
		setContract(launchContract);
		
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
								getInjectedParams().setParam(keyValue[0], requiredDataTable);
							}else { // single field value required
								value = findParentActivityValue(keyValue[1]);
								getInjectedParams().setParam(keyValue[0], value);
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
	private void activityCtrlProps(String table, String contract) {
		
		setContract(contract);
		
		String[] contractKeyStrings = contract.split("\\,");
		
		for (String key : contractKeyStrings) {
			String[] keyArray = key.split("=");
			if (keyArray[0].split("\\.").length >= 2) {
				if (keyArray[1].startsWith("y")) {
					getInjectedParams().setCtrlProp(keyArray[0].substring(0, keyArray[0].lastIndexOf(".")), CtrlPropEnum.BUTTONCLICK, "");
				}
			}else {
				if (keyArray.length == 2) {
	    			Object ctrlPropValue = findParentActivityValue(keyArray[1]);
	    			getInjectedParams().setCtrlProp(table + "." + keyArray[0], CtrlPropEnum.SCREENVALUE, Objects.toString(ctrlPropValue, ""));
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

	/**
	 * Use a new ChildActivityDefinition and supply the table and field for that launch contract
	 * 
	 * @param childActivityDef
	 * @param table
	 * @param field
	 * @return Response of the ChildTypeRequest
	 */
	public static void loadChildActivity(ChildActivityDefinition childActivityDef, String table, String field) {
		childActivityDef.createLaunchContract(table, field);
		
		loadChildActivity(childActivityDef);
	}

	/**
	 * Use a ChildActivityDefinition that has already been supplied the table and field for the desired launch contract
	 * 
	 * @param childActivityDef
	 * @return Response of the ChildTypeRequest
	 */
	public static void loadChildActivity(ChildActivityDefinition childActivityDef) {
		boolean pointer = childActivityDef.getActivityReference() != null;
		
		if (childActivityDef.getModel() == null) {
			try {
				LoadActivityResponse loadActivityResponse = AppServerAdapter.getInstance().loadActivityDef(childActivityDef, childActivityDef.getLastLoadActivityRequest());
				if (pointer) {
					childActivityDef.getReferenceStack().forEach((activityDef) -> activityDef.setModel(loadActivityResponse));
				}
			} catch (Throwable e) {
				e.printStackTrace();
				throw new IllegalStateException(e);
			}
		}
		
		try {
			if (childActivityDef.getType() == LaunchContractType.LOADDATA){
				LoadDataResponse loadDataResponse = AppServerAdapter.getInstance().loadData(childActivityDef, childActivityDef.getLastLoadDataRequest());
				if (pointer) {
					childActivityDef.getReferenceStack().forEach((activityDef) -> activityDef.setLastLoadDataResponse(loadDataResponse));
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		
		try {
			childActivityDef.nextTableFieldCascadeDive();
		} catch (Throwable ex) {
			ex.printStackTrace();
			throw new IllegalStateException("Validation error while cascading buttons in Child LoadDataResponse");
		}
	}
	
}