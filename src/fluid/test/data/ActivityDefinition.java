package fluid.test.data;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import auto.utils.fluid.FluidEvent;
import fluid.test.adapter.AppServerAdapter;
import fluid.test.data.ChildActivityDefinition.LaunchContractType;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.message.AbstractMessage;
import fluid.test.message.CtrlProp;
import fluid.test.message.CtrlPropEnum;
import fluid.test.message.CtrlPropPair;
import fluid.test.message.Error;
import fluid.test.message.ErrorTypeEnum;
import fluid.test.message.GetResultsRequest;
import fluid.test.message.GetResultsResponse;
import fluid.test.message.LoadActivityRequest;
import fluid.test.message.LoadActivityResponse;
import fluid.test.message.LoadDataModeEnum;
import fluid.test.message.LoadDataRequest;
import fluid.test.message.LoadDataResponse;
import fluid.test.message.PresentationMessage;
import fluid.test.message.Response;
import fluid.test.message.SaveDataResponse;

public class ActivityDefinition {

   public static final Set<String> IGNORE_REPEAT_BUTTONS = new HashSet<String>();
   static {
	   IGNORE_REPEAT_BUTTONS.add("button.checkForNotice");
   }
   private Set<String> serviceHistory = new HashSet<String>();
   private Set<ChildActivityDefinition> childSet = new LinkedHashSet<ChildActivityDefinition>();
   private Map<String, DataSourceDefinition> dataSourceDefinitionMap = new LinkedHashMap<String, DataSourceDefinition>();
   private LoadActivityResponse model;
   private DataSourceDefinition resultsDataSourceDefinition;
   private DataSourceDefinition genSearchDataSourceDefinition;
   private DataSourceDefinition advSearchDataSourceDefinition;
   private PresentationMessage injectedParams;
   private boolean closed;
   private String service;
   private boolean stopOnWarning = false;
   private boolean validateOnly = false;
   
   private boolean asyncResponses = false;
   
   private String messageKeyPin;
   private String operatorID;
   private String pin;
   
   private LoadActivityRequest lastLoadActivityRequest = new LoadActivityRequest();  
   private LoadDataRequest lastLoadDataRequest = new LoadDataRequest(LoadDataModeEnum.ADD);
   private LoadDataResponse lastLoadDataResponse;
   
   private Map<String, Integer> repeatButtons = new HashMap<String, Integer>();
   
   public boolean mlDebugTrace;
   
   public ActivityDefinition() {}
   
   public boolean isValidateOnly() {
	   return this.validateOnly;
   }
   
   public void setValidateOnly(boolean validateOnly) {
	   this.validateOnly = validateOnly;
   }
   
   public boolean isStopOnWarnining() {
	   return this.stopOnWarning;
   }
   
   public void setStopOnWarning(boolean stopOnWarning) {
	   this.stopOnWarning = stopOnWarning;
   }
   
   public PresentationMessage getInjectedParams() {
	   return this.injectedParams;
   }
   
   public void setInjectedParams(PresentationMessage injectedParams) {
	   this.injectedParams = injectedParams;
   }
   
   public Set<String> getHistory() {
	   return Collections.unmodifiableSet(this.serviceHistory);
   }
   
   public Map<String, Integer> getRepeatButtons(){
	   return this.repeatButtons;
   }
   
   protected void setRepeatButtons(Map<String, Integer> repeatButtons) {
	   this.repeatButtons = repeatButtons;
   }
   
   public String getPin() {
	   return this.pin;
   }
   
   public void setPin(String pin) {
	   this.pin = pin;
   }
   
   public String getOperatorID() {
	   return this.operatorID;
   }
   
   public void setOperatorID(String operatorID) {
	   this.operatorID = operatorID;
   }
   
   public boolean isClosed() {
	   return this.closed;
   }
   
   public void close() {
	   this.closed = true;
   }
   
   public void setClosed(boolean closed) {
	   this.closed = closed;
   }
   
   public Map<String, DataSourceDefinition> getDataSourceDefinitionMap() {
      return dataSourceDefinitionMap;
   }

   protected void setDataSourceDefinitionMap(Map<String, DataSourceDefinition> dataSourceDefinitionMap) {
      this.dataSourceDefinitionMap = dataSourceDefinitionMap;
   }
   
   public Set<ChildActivityDefinition> getChildSet(){
	   return this.childSet;
   }
   
   protected void setChildSet(Set<ChildActivityDefinition> childSet) {
	   this.childSet = childSet;
   }
   
   public LoadActivityRequest getLastLoadActivityRequest() {
	   return this.lastLoadActivityRequest;
   }
   
   public void setLastLoadActivityRequest(LoadActivityRequest loadActivityRequest) {
	   this.lastLoadActivityRequest = loadActivityRequest;
   }
   
   public LoadDataRequest getLastLoadDataRequest() {
	   return this.lastLoadDataRequest;
   }
   
   public void setLastLoadDataRequest(LoadDataRequest loadDataRequest) {
	   this.lastLoadDataRequest = loadDataRequest;
   }

   public LoadDataResponse getLastLoadDataResponse() {
	   return this.lastLoadDataResponse;
   }
   
   protected void setLastLoadDataResponse(LoadDataResponse loadDataResponse) {
	   this.lastLoadDataResponse = loadDataResponse;
   }
   
   /**
    * @return the service
    */
   public String getService() {
      return service;
   }

   /**
    * @param service the service to set
    */
   public void setService(String service) {
      this.service = service;
   }

   public LoadActivityResponse getModel() {
      return model;
   }

   protected void setModel(LoadActivityResponse model) {
	   this.model = model;
   }
   
   public ChildActivityDefinition launchRecentSelectionService() {
	   ChildActivityDefinition recentSelectionServiceActivity = null;
	   
	   String recentSectionService = Objects.toString(this.model.getViewDetails().get("recentSectionService"), null);
	   if (recentSectionService != null) {
		   recentSelectionServiceActivity = ChildActivityDefinition.newInstance(this);
		   recentSelectionServiceActivity.setType(LaunchContractType.LOADDATA);
		   recentSelectionServiceActivity.setService(recentSectionService);
		   
		   LoadActivityRequest loadActivityRequest = new LoadActivityRequest();
		   loadActivityRequest.setParam("activity", this.model.getActivity());
		   loadActivityRequest.setParam("parentActivity", this.model.getActivity());
		   
		   LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.VIEW);
		   loadDataRequest.setParam("activity", this.model.getActivity());
		   loadDataRequest.setParam("parentActivity", this.model.getActivity());
		   
		   recentSelectionServiceActivity.setLastLoadActivityRequest(loadActivityRequest);
		   recentSelectionServiceActivity.setLastLoadDataRequest(loadDataRequest);
		   
		   ChildActivityDefinition.loadChildActivity(recentSelectionServiceActivity);
	   }
	   
	   return recentSelectionServiceActivity;
   }
   
   public void initializeModel(LoadActivityResponse loadActivityResponse) throws Throwable  {
	  setClosed(false);
	  this.serviceHistory.add(service);
	  
      this.model = loadActivityResponse;
      if (this.model.hasStopErrors()) {
         Throwable e = new Throwable("Error in LoadActivityResponse: " + this.model.getErrorMsg());
         e.fillInStackTrace();
         throw e;
      }
      
      //Discover the datasource definitions and field definitions
      JsonArray dsDefs = this.model.getDataSourceDefs();
      if(dsDefs != null && !dsDefs.isEmpty()) {
         boolean hasDefaultRecord = false;
         DataSourceDefinition potentialResultsDef = null;
       for (@SuppressWarnings("rawtypes")
       Iterator iterator = dsDefs.iterator(); iterator.hasNext();) {
          JsonObject jdsDef = (JsonObject) iterator.next();
          
          DataSourceDefinition dsDef = new DataSourceDefinition();
          dsDef.setTableName(((String) jdsDef.get("tableName")));
          dsDef.setDataSetName(((String) jdsDef.get("dataSetName")));
 
          addFieldDefinition((JsonArray) jdsDef.get("dsFields"), dsDef);

          if (Boolean.TRUE.equals(jdsDef.get("defaultRecord"))) {
             Record defaultRec = new Record();
             for (String field : dsDef.getFieldDefinitionMap().keySet()) {
                defaultRec.setFieldValue(field, (Object) null);
             }
             dsDef.getRecordList().add(defaultRec);
             this.genSearchDataSourceDefinition = dsDef;
             hasDefaultRecord = true;
          } else if (potentialResultsDef == null) {
             potentialResultsDef = dsDef;
          }

          this.dataSourceDefinitionMap.put(dsDef.getTableName(), dsDef);
       }
         if (hasDefaultRecord && potentialResultsDef != null) {
            this.resultsDataSourceDefinition = potentialResultsDef;
         }
      }
      
      //Discover viewDetail information
      ViewDefinition.setViewDefinitions(this.dataSourceDefinitionMap, this.model.getViewDetails());
	  
		if (this.model.getResultsDSPath() != null && !this.model.getResultsDSPath().isEmpty()) {
			String[] dsPath = this.model.getResultsDSPath().split("\\/");
			if (dsPath.length < 2) {
				Throwable e = new Throwable("resultsDSPath has not been defined correctly in the UI definition ["
						+ this.model.getResultsDSPath() + "]");
				e.fillInStackTrace();
				throw e;
			}
			
			this.resultsDataSourceDefinition = new DataSourceDefinition();
			this.resultsDataSourceDefinition.setDataSetName(dsPath[0]); // "dsResults"	
			this.resultsDataSourceDefinition.setTableName(dsPath[1]); // "ttResults"
			this.genSearchDataSourceDefinition = null;
			this.advSearchDataSourceDefinition = null;
			
			//These two jsonObject FieldDefinitions are shared between all three search types
			JsonObject batchSize = new JsonObject();
			batchSize.put("name", "batchSize");
			batchSize.put("type", "text");
			JsonObject batchPage = new JsonObject();
			batchPage.put("name", "batchPage");
			batchPage.put("type", "text");
			
			// Search/GenSearch handling
			JsonArray jsSearchFormDSFields = (JsonArray) this.model.getViewDetails().get("searchFormDSFields");
			JsonArray jsGenSearchFormDSFields = (JsonArray) this.model.getViewDetails().get("genSearchFormDSFields");
			JsonArray genSearchForm = jsGenSearchFormDSFields != null && !jsGenSearchFormDSFields.isEmpty()
					? jsGenSearchFormDSFields //Overwrites/Priority
					: jsSearchFormDSFields;	  //Use if GenSearch is not defined
			if ((genSearchForm != null && !genSearchForm.isEmpty())) { //If both are null/empty, do not bother
				genSearchForm.add(batchSize);
				genSearchForm.add(batchPage);
				this.genSearchDataSourceDefinition = createSearchDataSourceDefinition(genSearchForm);
				getDataSourceDefinitionMap().put("search", this.genSearchDataSourceDefinition);
			}
			
			// AdvSearch handling
			JsonArray jsAdvSearchFormDSFields = (JsonArray) this.model.getViewDetails().get("advSearchFormDSFields");
			if (jsAdvSearchFormDSFields != null && !jsAdvSearchFormDSFields.isEmpty()){
				jsAdvSearchFormDSFields.add(batchSize);
				jsAdvSearchFormDSFields.add(batchPage);
				this.advSearchDataSourceDefinition = createSearchDataSourceDefinition(jsAdvSearchFormDSFields);
				getDataSourceDefinitionMap().put("advSearch", this.advSearchDataSourceDefinition);
			}
			
			// Results handling
			JsonArray jsResultsFormDSFields = (JsonArray) this.model.getViewDetails().get("resultsDSFields");
			if (jsResultsFormDSFields != null && !jsResultsFormDSFields.isEmpty()) {
				addFieldDefinition((JsonArray) jsResultsFormDSFields, resultsDataSourceDefinition);
			}
			getDataSourceDefinitionMap().put("results", resultsDataSourceDefinition);
		}
      
      if (this.model.isPasswordReq()) {
    	  PresentationMessage oRequest = new PresentationMessage();
    	  oRequest.setParam("operatorID", getOperatorID());
    	  oRequest.setParam("pin", getPin());
    	  oRequest.setParam("pinRequired", Boolean.TRUE);
    	  oRequest.setParam("activity", this.model.getActivity());
    	  PresentationMessage oResponse = AppServerAdapter.getInstance().presenter(this, "checkPassword", oRequest);
    	  
    	  if (oResponse.hasStopErrors()) {
 			 Throwable e = new Throwable("Pin was invalid: " + oResponse.getErrorMsg());
 			 e.fillInStackTrace();
 			 throw e;
    	  }else {
    		  setMessageKeyPin(oResponse.getParamAsString("_password"));
    	  }
      }
      
      processResponse(model);
   }

   private DataSourceDefinition createSearchDataSourceDefinition(JsonArray fieldDefinitions) {
		DataSourceDefinition newDsDef = new DataSourceDefinition();
		addFieldDefinition(fieldDefinitions, newDsDef);
		
		Record newRecord = new Record();
		for (String field: newDsDef.getFieldDefinitionMap().keySet()) {
			newRecord.setFieldValue(field, (Object) null);
		}
		newRecord.setFieldValue("batchSize", 50);
		
		newDsDef.getRecordList().add(newRecord);
		
		return newDsDef;
   }
   
   private void addFieldDefinition(JsonArray dsFields, DataSourceDefinition dsDef) {
      for (@SuppressWarnings("rawtypes")
      
      Iterator iterator2 = dsFields.iterator(); iterator2.hasNext();) {
    	  JsonObject jdsField = (JsonObject) iterator2.next();

         FieldDefinition fieldDef = new FieldDefinition(((String) jdsField.get("name")), ((String) jdsField.get("type")));
         if (Boolean.TRUE.equals(((Boolean) jdsField.get("primaryKey")))) {
            fieldDef.setPrimaryKey(true);
            dsDef.setPrimaryKey(fieldDef.getName());
         }
         dsDef.addFieldDefinition(fieldDef);
      }
      
   }

   public DataSourceDefinition getResultsDataSourceDefinition() {
      return resultsDataSourceDefinition;
   }
   
   public DataSourceDefinition getGenSearchDataSourceDefinition() {
	  return genSearchDataSourceDefinition;
   }
   
   public DataSourceDefinition getAdvSearchDataSourceDefinition() {
	  return advSearchDataSourceDefinition;
   }

   public void setResultsDataSourceDefinition(
         DataSourceDefinition resultsDataSourceDefinition) {
      this.resultsDataSourceDefinition = resultsDataSourceDefinition;
   }
   
   public void setGenSearchDataSourceDefinition(DataSourceDefinition genSearchDataSourceDefinition) {
	   this.genSearchDataSourceDefinition = genSearchDataSourceDefinition;
   }
   
   public void setAdvSearchDataSourceDefinition(DataSourceDefinition advSearchDataSourceDefinition) {
	   this.advSearchDataSourceDefinition = advSearchDataSourceDefinition;
   }

	public LoadDataResponse loadCurrentResultRecord() throws Throwable {
		
		if (getResultsDataSourceDefinition().getRecordList().isEmpty()) {
			throw new IllegalStateException("Cannot load current result record, there are no records in the \"result\" DataSourceDefinition.");
		}
		
		LoadDataRequest loadDataRequest = new LoadDataRequest(LoadDataModeEnum.EDIT);
		loadDataRequest.setLoadRowid(getResultsDataSourceDefinition().getCurrentRecord()
				.getFieldValueAsString(getResultsDataSourceDefinition().getPrimaryKey()));

		return AppServerAdapter.getInstance().loadData(this, loadDataRequest);
	}
   
   public void processGetResultsResponse(GetResultsResponse getResultsResponse) throws Throwable {
      if (!getResultsResponse.hasStopErrors()) {
    	  getResultsResponse.populateRecordList(getResultsDataSourceDefinition());

          processResponse(getResultsResponse);
      }
   }

   public void processLoadDataResponse(LoadDataResponse loadDataResponse) throws Throwable {
	  this.lastLoadDataResponse = loadDataResponse;
	  
/*      if (loadDataResponse.hasStopErrors()) {
 Throwable e = new Throwable("Error in LoadDataResponse: " + loadDataResponse.getErrorMsg());
 e.fillInStackTrace();
 throw e;
      }*/
      
      processResponse(loadDataResponse);
   }
   
   public void processSaveDataResponse(SaveDataResponse saveDataResponse) throws Throwable {
		if (saveDataResponse.getCtrlProp("window.dummy", CtrlPropEnum.ACTIVITYCLOSE) != null) {
			saveDataResponse = new SaveDataResponse(saveDataResponse.serializeMessage());
			
			LinkedHashMap<CtrlPropPair, String> newCtrlProps = new LinkedHashMap<>();
			for (Map.Entry<CtrlPropPair, String> ctrlProp : saveDataResponse.getCtrlProperties().entrySet()) {
				newCtrlProps.put(ctrlProp.getKey(), ctrlProp.getValue());
				if ("window.dummy".equals(ctrlProp.getKey().getName()) && CtrlPropEnum.ACTIVITYCLOSE == ctrlProp.getKey().getProp()) {
					break;
				}
			}
			
			saveDataResponse.setCtrlProperties(newCtrlProps);
		}
		
		if (!saveDataResponse.hasCtrlProps() && saveDataResponse.getSaveSuccess()) {
			String savedRecordId = saveDataResponse.getSaveRowId();
			if (savedRecordId != null && !savedRecordId.isEmpty() && getGenSearchDataSourceDefinition() != null) {
				GetResultsRequest getResultsRequest = new GetResultsRequest();
				getResultsRequest.setParam("rowid", savedRecordId);
				AppServerAdapter.getInstance().getResults(this, getResultsRequest);
				loadCurrentResultRecord();
			} else {
				LoadDataRequest loadDataRequest = new LoadDataRequest(savedRecordId != null 
						? LoadDataModeEnum.EDIT
						: LoadDataModeEnum.ADD);
				loadDataRequest.setLoadRowid(savedRecordId != null
						? savedRecordId 
						: "");
				AppServerAdapter.getInstance().loadData(this, loadDataRequest);
			}
		}
		
		processResponse(saveDataResponse);
		
   }
   
   @FunctionalInterface
   static interface RunnableThrows {
       public abstract void run() throws Throwable;
   }
   
   public void processResponse(PresentationMessage responseMessage) throws Throwable {
	   
       if (responseMessage.hasDataSets()) {
          for (String tableName : this.dataSourceDefinitionMap.keySet()) {
             DataSourceDefinition dsDef = this.dataSourceDefinitionMap.get(tableName);
             if (dsDef != getGenSearchDataSourceDefinition() &&
                   dsDef != getAdvSearchDataSourceDefinition() &&
                   dsDef != getResultsDataSourceDefinition()) {
                responseMessage.populateRecordList(dsDef);
             }
          }
	   }
	   
	   if (this.asyncResponses) {
		   return;
	   }
	   
	  Map<ActivityDefinition, PresentationMessage> propsForAncestors = new HashMap<ActivityDefinition, PresentationMessage>();
      List<UpdateTableDefinition> refreshTables = new ArrayList<UpdateTableDefinition>();
      List<UpdateTableDefinition> updateTables = new ArrayList<UpdateTableDefinition>();
      List<CtrlProp> buttonClickEventForList = new ArrayList<CtrlProp>();
      Set<RunnableThrows> deferred = new LinkedHashSet<RunnableThrows>();
      
       if (mlDebugTrace) {
          System.err.println("processResponse hasErrors: " + responseMessage.hasStopErrors() + " hasCtrlProps: " + responseMessage.hasCtrlProps());
       }
    
       if (responseMessage.hasErrors()) {
          
          /*a blank error means that the UI will clear the error icon from the field*/
			if (responseMessage != this.model) {
				List<Error> errors = responseMessage.getErrorList();
				for (Error error : errors) {
					if (error.getMsg().trim().isEmpty() && (ErrorTypeEnum.ERROR.getValue().equals(error.getType()))) {
						this.model.removeError(error.getField());
					}
				}
			}
          
          responseMessage.removeBlankErrorMessages();
          this.model.appendErrorSetFrom(responseMessage);
       }
       
       for (Map.Entry<CtrlPropPair, String> ctrlPropEntry : responseMessage.getCtrlProperties().entrySet()) {
    	   processResponseCtrlProps(ctrlPropEntry, responseMessage, deferred, refreshTables, updateTables, buttonClickEventForList, propsForAncestors);
       }
       
       for (RunnableThrows runnable : deferred) {
    	   runnable.run();
       }
       
       refreshTables(refreshTables);   
       updateTables(updateTables);
       
       applyClick(buttonClickEventForList);
       
       for (Map.Entry<ActivityDefinition, PresentationMessage> entry : propsForAncestors.entrySet()){
           entry.getKey().processResponse(entry.getValue());
       }
   }
   
	protected void processResponseCtrlProps(Map.Entry<CtrlPropPair, String> ctrlPropEntry,
			PresentationMessage responseMessage,
			Set<RunnableThrows> deferred,
			List<UpdateTableDefinition> refreshTables,
			List<UpdateTableDefinition> updateTables,
			List<CtrlProp> buttonClickEventForList,
			Map<ActivityDefinition, PresentationMessage> propsForAncestors) throws Throwable { 
	
		String cCtrlName = ctrlPropEntry.getKey().getName();
		CtrlPropEnum oCtrlProp = ctrlPropEntry.getKey().getProp();
		String cPropValue = ctrlPropEntry.getValue();
		
		if (mlDebugTrace) {
			System.err.println("Ctrl Prop: " + cCtrlName + " " + oCtrlProp.getValue() + " " + cPropValue);
		}
		
		switch (oCtrlProp) {
			case ACTIVITYCLOSE:
				if ("window.dummy".equals(cCtrlName)) close();
				break;
			case SCREENVALUE:
				setScreenValue(cCtrlName, cPropValue);
				break;
			case BUTTONCLICK:
				buttonClickEventForList.add(new CtrlProp(cCtrlName, oCtrlProp, cPropValue));
				break;
			case REFRESHTABLE:
				refreshTables.add(new UpdateTableDefinition(cCtrlName, responseMessage.getParamAsDataSet(cPropValue)));
				break;
			case UPDATETABLE:
				updateTables.add(new UpdateTableDefinition(cCtrlName, responseMessage.getParamAsDataSet(cPropValue)));
				break;
			case RELOADDATA:
				deferred.add(() -> AppServerAdapter.getInstance().loadData(this, getLastLoadDataRequest()));
				break;
			case LOADDATA:
				final String loadRowid = cPropValue;
				deferred.add(() ->  {
					if (loadRowid != null) {
						LoadDataRequest loadDataRequest = getLastLoadDataRequest();
						loadDataRequest.setLoadMode(loadRowid.isEmpty() 
								? LoadDataModeEnum.ADD 
								: LoadDataModeEnum.EDIT);
						loadDataRequest.setLoadRowid(loadRowid);
						AppServerAdapter.getInstance().loadData(this, loadDataRequest);
			       }
				});
				break;
			default:
				this.model.setCtrlProp(cCtrlName, oCtrlProp, cPropValue);
				break;
		}
	}

   protected void applyClick(List<CtrlProp> buttonClickEventForList) throws Throwable {
	   for (CtrlProp ctrlProp : buttonClickEventForList) {
           String[] tableField;
    	   if ((tableField = ctrlProp.getName().split("\\.")).length >= 2) {
              
             String tableName = tableField[0];
             String fieldName = tableField[1];
             //if ("button".equalsIgnoreCase(tableName)) {
            	 
	           	if (!ctrlProp.getValue().trim().isEmpty()) {
	           		
	        		final Integer secondDelay = Integer.valueOf(ctrlProp.getValue());
	        		if (secondDelay > 0 && !IGNORE_REPEAT_BUTTONS.contains(ctrlProp.getName())) {
					    getRepeatButtons().put(ctrlProp.getName(), secondDelay);
	        		} else {
	        			getRepeatButtons().remove(ctrlProp.getName());
	        		}
	        		
	        	} else {
      			  validateField(tableName, fieldName);
      		    }
	           	
             //}
          }
	   }
	   
	   applyRepeatingClick();
   }
   
   protected void applyRepeatingClick() throws Throwable {
       // This separate block ensures that non-repeating buttons are handled before entering an infinite
       // validateField(String, String) loop for repeating buttons that's at the mercy of the server to exit.
       int secondsElapsed = 0;
       if (!getRepeatButtons().isEmpty()) {
    	   Set<String> repeatButtonsCopy = new HashSet<String>(getRepeatButtons().keySet()); // Copy to protect against possible concurrent modification exception
           for (String repeatButton : repeatButtonsCopy) { 
        	   
        	   Integer secondDelay = getRepeatButtons().get(repeatButton);
        	   if (secondDelay != null) {
        		   
            	   String[] tableField;
            	   if ((tableField = repeatButton.split("\\.")).length >= 2) {
            		   
            		   // In the case there's multiple repeating buttons, don't delay for each separately but instead together.
            		   // TODO: Some repeatButtons may execute later than they usually would depending on button order, but the wait is the same regardless in terms of automation.
            		   if (secondDelay > secondsElapsed) {
            			   secondDelay -= secondsElapsed;
            			   Thread.sleep(secondDelay * 1000);
            			   secondsElapsed += secondDelay;
            		   }
            		   
                       String tableName = tableField[0];
                       String fieldName = tableField[1];
                       
                       validateField(tableName, fieldName);
            	   }
        	   }
           }
       }
   }
   
   public PresentationMessage validateField(String tableName, String fieldName) throws Throwable {
	   return validateField(tableName, fieldName, FluidEvent.LEAVE);
   }
   
   public PresentationMessage validateField(String tableName, String fieldName, FluidEvent fluidEvent) throws Throwable {
      
      String cList;
      
      String tableFieldName = tableName + "." + fieldName;
      
      cList = this.model.getCtrlProp(tableFieldName, CtrlPropEnum.DROPDOWNLIST);
      if (cList != null) {
         checkFieldValueInList(tableName, fieldName, cList);
      }
      
      cList = this.model.getCtrlProp(tableFieldName, CtrlPropEnum.DROPDOWNPAIRSLIST);
      if (cList != null) {
         checkFieldValueInPairsList(tableName, fieldName, cList);
      }
      
      String valReq = this.model.getCtrlProp(tableFieldName, CtrlPropEnum.VALIDATIONREQ);
      boolean lValidationReq = (valReq != null) ? ("yes".equalsIgnoreCase(valReq.trim())) : (false);
      if (!lValidationReq) {
         return new PresentationMessage();
      }
      
      String cValidationContract = this.model.getCtrlProp(tableFieldName, CtrlPropEnum.VALIDATIONCONTRACT);
      if (cValidationContract == null) {
         cValidationContract = tableFieldName;
      }
      
      String cValidationFunction = this.model.getCtrlProp(tableFieldName, CtrlPropEnum.VALIDATIONFUNCTION);
      if (cValidationFunction == null) {
         cValidationFunction = tableFieldName.replace(".", "");
      }
      
      /*clear any error icons from the field before validation*/
      this.model.removeError(tableFieldName);
      
      PresentationMessage oRequest = buildValidationContract( tableName, fieldName, cValidationContract, fluidEvent);
      
      oRequest.setCallingField(tableFieldName);
      
      PresentationMessage oResponse = AppServerAdapter.getInstance().presenter(this, cValidationFunction, oRequest);
      
      return oResponse;
   }
   
	public List<PresentationMessage> validateAsynchronously(String[][] tableFields) throws Throwable  {
		
		List<PresentationMessage> oResponses = new ArrayList<PresentationMessage>();
		
		this.asyncResponses = true;
		try {
			for (String[] tableField : tableFields) {
				if (tableField.length != 2) {
					continue;
				}
				oResponses.add(validateField(tableField[0], tableField[1]));
			}
		} finally {
			this.asyncResponses = false;
		}
		
		Collections.sort(oResponses, new AsyncResponseComparator());
		
		for (PresentationMessage oResponse : oResponses) {
			processResponse(oResponse);
		}
		
		return oResponses;
	}
   
	protected class AsyncResponseComparator implements Comparator<Response> {
		@Override
		public int compare(Response o1, Response o2) {
/*			System.out.println("o1 " + o1.getResponseTime());
			System.out.println("o2 " + o2.getResponseTime());*/
			return Long.compare(o1.getResponseTime(), o2.getResponseTime());
		}
	}
	
   private void checkFieldValueInList(String tableName, String fieldName, String list) throws Throwable {

      Record record = this.dataSourceDefinitionMap.get(tableName).getCurrentRecord();
      //String[] listArray = list.split("\\,");
      
      if (record == null) {
         Throwable e = new Throwable("System error: temp-table record for "
               + tableName + "." + fieldName + " is not available");
         e.fillInStackTrace();
         throw e;
      }
      
      // This dead code validates if the value actually exists inside the list, but apparently any value can be forced inside.
      /*      boolean found = false;
      String value = record.getFieldValueAsString(fieldName);
      for (String entry : listArray) {
         if (entry.equalsIgnoreCase(value)) {
            found = true;
            break;
         }
      }
      
      Object fieldConstructor = getDataSourceDefinitionMap().get(tableName).getViewDefinition().getFieldViewDefinition(fieldName).get("_constructor");
      
      if (!found && !"ComboBoxItem".equalsIgnoreCase(Objects.toString(fieldConstructor, null))) {
         Throwable e = new Throwable(
               "Invalid value [" + value + "] for " + tableName + "."
                     + fieldName + "Not in selection list [" + list + "]");
         e.fillInStackTrace();
         throw e;
      }*/
   }
   
   private void checkFieldValueInPairsList(String tableName, String fieldName, String list) throws Throwable {
      Record record = dataSourceDefinitionMap.get(tableName).getCurrentRecord();
      String[] listArray = list.split("\\,");

      if (record == null) {
         Throwable e = new Throwable("System error: temp-table record for "
               + tableName + "." + fieldName + " is not available");
         e.fillInStackTrace();
         throw e;
      }

      boolean found = false;
      String value = record.getFieldValueAsString(fieldName);
      for (int i = 0; i < listArray.length; i++) {
         String entry = listArray[i];
         if ((i % 2) == 0)
         {
            if (entry.equalsIgnoreCase(value)) {
               found = true;
               break;
            }
         }   
      }
      
      if (!found) {
/*         Throwable e = new Throwable(
               "Invalid value [" + value + "] for " + tableName + "."
                     + fieldName + "Not in selection list [" + list + "]");
         e.fillInStackTrace();
         throw e;*/
      }
      
   }

   public PresentationMessage buildValidationContract(String tableName, String fieldName, String validationContract, FluidEvent event) {

      // Create the request
      PresentationMessage valRequest = new PresentationMessage();
      
      String value;

      if (validationContract != null && validationContract.length() > 0) {
         // Split comma separated string into an array
         String[] requiredCtrlsArray = validationContract.split("\\,");

         if (requiredCtrlsArray != null && requiredCtrlsArray.length > 0) {
            // Iterated over all required controls
            for (int index = 0; index < requiredCtrlsArray.length; index++) {
               String req = requiredCtrlsArray[index].trim();

               if (req.matches("\\[.*\\(.*\\)\\]")) { // Temp-Table required
                  String table = req.substring(1, req.indexOf("("));
                  String fieldList = req.substring(req.indexOf("(") + 1, req.indexOf(")"));
                  String[] fieldArray = fieldList.split("\\|");
                  JsonObject requiredDataTable = getRequiredDataTable(table, fieldArray, false);
                  if (requiredDataTable != null) {
                	  valRequest.setParam(table, requiredDataTable);
                  }
               } else if (req.equalsIgnoreCase("eventName")) {
                  valRequest.setCtrlProp(req, CtrlPropEnum.SCREENVALUE, event.getEventType().getValue());
               } else { // single field value required
                  value = findScreenValue(req);
                  if (value == null) {
                     valRequest.setCtrlProp(req, CtrlPropEnum.SCREENVALUE, "");
                  } else {
                     valRequest.setCtrlProp(req, CtrlPropEnum.SCREENVALUE, value);
                  }
               }
            }
         }
      } else {
         // Add a control property for the field being validated
         value = findScreenValue(tableName + "." + fieldName);
         if (value == null) {
            valRequest.setCtrlProp(tableName + "." + fieldName, CtrlPropEnum.SCREENVALUE, "");
         } else {
            valRequest.setCtrlProp(tableName + "." + fieldName, CtrlPropEnum.SCREENVALUE, value);
         }
      }
      
      event.applyEventToRequest(this, tableName, fieldName, valRequest);
      
      return valRequest;
   }
   
   public String findScreenValue(String tableFieldName) {
      
	   StringBuilder screenValue = new StringBuilder("");
      
      if (tableFieldName.split("\\.").length >= 2) {
         
         String tableName = tableFieldName.split("\\.")[0];
         String fieldName = tableFieldName.split("\\.")[1];
         
         if ("button".equalsIgnoreCase(tableName)) {
            return this.model.getCtrlProp(tableFieldName, CtrlPropEnum.SCREENVALUE);
         }
         
         if (!getDataSourceDefinitionMap().containsKey(tableName)) {
        	 return screenValue.toString();
         }
         DataSourceDefinition dsDef = getDataSourceDefinitionMap().get(tableName);
         
		 if (dsDef.getRecordList().isEmpty()) {
			 return screenValue.toString();
		 }
         
         if ("grid".equalsIgnoreCase(fieldName)) {
        	 fieldName = dsDef.getPrimaryKey();
         }
		 
         List<Record> records;
         if (dsDef.getSelectedGridRecordPrimaryKeyValues() != null && !dsDef.getSelectedGridRecordPrimaryKeyValues().isEmpty()) {
        	 Set<Object> primaryKeyValues = dsDef.getSelectedGridRecordPrimaryKeyValues();
        	 
        	 List<Record> selectedGridRecords = new ArrayList<>();
        	 for (Record gridRecord : dsDef.getRecordList()) {
        		 if (primaryKeyValues.contains(gridRecord.getFieldValueAsObject(dsDef.getPrimaryKey()))) {
        			 selectedGridRecords.add(gridRecord);
        		 }
        	 }
        	 records = selectedGridRecords;
         } else {
        	 records = Collections.singletonList(dsDef.getCurrentRecord());
         }
         FieldDefinition fieldDef = dsDef.getFieldDefinition(fieldName);
         
         String[] fieldValues = new String[records.size()];
         for (int i = 0 ; i < records.size() ; i++) {
            final Record record = records.get(i);

            if (record == null) {
               fieldValues[i] = screenValue.toString();
            }

            if (fieldDef != null) {
               String fldType = fieldDef.getType();
               try {
                  if (fldType.equalsIgnoreCase("DATE")) {
                     Date dsDateValue = record.getFieldValueAsDate(fieldName);
                     if (dsDateValue == null) {
                        fieldValues[i] = record.getFieldValueAsString(fieldName);
                     } else {
                        SimpleDateFormat df = new SimpleDateFormat(AppServerAdapter.getInstance().getDateFormat());
                        fieldValues[i] = df.format(dsDateValue);
                     }
                  } else if (fldType.equalsIgnoreCase("DATETIME")) {
                     Date dsDateValue = record.getFieldValueAsDatetime(fieldName);
                     if (dsDateValue == null) {
                        fieldValues[i] = record.getFieldValueAsString(fieldName);
                     } else {
                        SimpleDateFormat df = new SimpleDateFormat(
                              AppServerAdapter.getInstance().getDateFormat() + " HH:mm:ss");
                        fieldValues[i] = df.format(dsDateValue);
                     }
                  } else {
                     fieldValues[i] = record.getFieldValueAsString(fieldName);
                  }
                  if (fieldValues[i] == null) {
                     fieldValues[i] = "";
                  }
               } catch (Exception e) {
                  // return the string value, invalid dates must be validated on
                  // the server
                  fieldValues[i] = record.getFieldValueAsString(fieldName);
                  if (fieldValues[i] == null) {
                     fieldValues[i] = "";
                  }
               }
            } else if (AppServerAdapter.getInstance().allowOrphanFields()) {
               fieldValues[i] = record.getFieldValueAsString(fieldName);
               if (fieldValues[i] == null) {
                  fieldValues[i] = "";
               }
            }
         }
         
         for (String fieldValue: fieldValues) {
        	 screenValue.append(fieldValue + ",");
         }
         if (!screenValue.isEmpty()) {
        	 screenValue.delete(screenValue.length()-1, screenValue.length()); // Remove last comma
         }
      }
      
      return screenValue.toString();
   }

   public JsonObject getRequiredDataTable(String tableName, String[] fieldArray, boolean trim) {
	   
	  if (getDataSourceDefinitionMap().get(tableName) == null) return null;
	   
      List<Record> records = getDataSourceDefinitionMap().get(tableName).getRecordList();
      
      JsonArray jsArr = new JsonArray();

      if (records.size() > 0) {
         for (int i = 0; i < records.size(); i++) {
            Record record = records.get(i);
            if (fieldArray.length == 1 && fieldArray[0].equalsIgnoreCase("*")) {
               if (trim) {
                  record = new Record(record); // Prevent unwanted mutation
                  record.keySet().retainAll(getDataSourceDefinitionMap().get(tableName).getFieldDefinitionMap().keySet());
               }
               jsArr.add(record);
            } else {
               Record newRecord = new Record();
               for (String fieldName : fieldArray) {
                  newRecord.put(fieldName, record.getFieldValueAsObject(fieldName));
               }
               jsArr.add(newRecord);
            }
         }
      }

      JsonObject jsonObj = new JsonObject();
      jsonObj.put(tableName, jsArr);
      return jsonObj;
   }

   public void setCommonParams(AbstractMessage request) {
      request.setMessageKeyPin(getMessageKeyPin());
   }

   public String getMessageKeyPin() {
      return messageKeyPin;
   }

   public void setMessageKeyPin(String messageKeyPin) {
      this.messageKeyPin = messageKeyPin;
   }
   
   private void updateTables(List<fluid.test.data.UpdateTableDefinition> updateTables) {
      for (UpdateTableDefinition updateTableDefinition : updateTables) {
         
         String tableName = updateTableDefinition.getTableName();
         if (this.dataSourceDefinitionMap.containsKey(tableName)) {
             DataSourceDefinition dataSourceDefinition = dataSourceDefinitionMap.get(tableName);
             
             List<Record> updateRecordList = updateTableDefinition.getRecordList();
             if (updateRecordList != null) {
                 for (Record record : updateRecordList) {
                     dataSourceDefinition.updateData(record);
                 }
             }
         }
      }      
   }

   private void refreshTables(List<fluid.test.data.UpdateTableDefinition> refreshTables) {
      for (UpdateTableDefinition updateTableDefinition : refreshTables) {
         String tableName = updateTableDefinition.getTableName();
         if (this.dataSourceDefinitionMap.containsKey(tableName)) {
        	 dataSourceDefinitionMap.get(tableName).refreshData(updateTableDefinition.getRecordList());
         }
      }
   }

   public void setScreenValue(String tableFieldName, String screenValue) {

      if (tableFieldName.split("\\.").length >= 2) {
               
               String tableName = tableFieldName.split("\\.")[0];
               String fieldName = tableFieldName.split("\\.")[1];
               
               if ("button".equalsIgnoreCase(tableName)) {
                  this.model.setCtrlProp(tableFieldName, CtrlPropEnum.SCREENVALUE, screenValue);
               }
               
               Map<String, DataSourceDefinition> dsDefMap = getDataSourceDefinitionMap();
               if (!dsDefMap.containsKey(tableName) ||
                     dsDefMap.get(tableName).getRecordList().isEmpty() ||
                     dsDefMap.get(tableName).getCurrentRecord() == null) {
            	   return;
               }
               
               // Special logic for "grid" fieldname that selects record.
               if ("grid".equals(fieldName)) {
                  if (screenValue != null && !screenValue.isEmpty()) {
                     DataSourceDefinition dsDef = dsDefMap.get(tableName);
                     List<Record> recordList = dsDef.getRecordList();
                     for (Record recordEntry : recordList) {
                        String primaryKey = recordEntry.getFieldValueAsString(dsDef.getPrimaryKey());
                        if (screenValue.equals(primaryKey)) {
                           dsDef.setCurrentRow(recordList.indexOf(recordEntry));
                           break;
                        }
                     }
                  }
                  return;
               }
               
               Record record = dsDefMap.get(tableName).getCurrentRecord();
               
               FieldDefinition fieldDef = dsDefMap.get(tableName).getFieldDefinition(fieldName);      
               if (fieldDef != null) {
                  String fldType = fieldDef.getType();
                  
	                  if (fldType.equalsIgnoreCase("DATE")) {
	                	  if (screenValue == null) {
	                		  record.setFieldValue(fieldName, null);
	                	  } else {
		                     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		                     try {
		                        record.setFieldValue(fieldName, df.parse(screenValue));
		                     } catch (ParseException e1) {
		                    	try {
									record.setFieldValue(fieldName, df.format(new SimpleDateFormat(AppServerAdapter.getInstance().getDateFormat()).parse(screenValue)));
								} catch (ParseException e2) {
			                        record.setFieldValue(fieldName, null);
								}
		                     }
	                	  }
	                  } else if (fldType.equalsIgnoreCase("DATETIME")) {
	                     SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	                     try {
	                        record.setFieldValue(fieldName, df.parse(screenValue));
	                     } catch (ParseException e) {
	                        record.setFieldValue(fieldName, screenValue);
	                     }
	                  } else if (fldType.equalsIgnoreCase("FLOAT")) {
	                     try {
	                        record.setFieldValue(fieldName, new BigDecimal(screenValue));
	                     } catch (NumberFormatException | NullPointerException ex) {
	                        record.setFieldValue(fieldName, screenValue);
	                     }
	                  } else if (fldType.equalsIgnoreCase("INTEGER")) {
	                     try {
	                        record.setFieldValue(fieldName, Integer.parseInt(screenValue));
	                     } catch (NumberFormatException e) {
	                        record.setFieldValue(fieldName, screenValue);
	                     }
	                  } else if (fldType.equalsIgnoreCase("BOOLEAN")) {
	                	  if ("true".equalsIgnoreCase(screenValue) || "false".equalsIgnoreCase(screenValue)) {
	                		  record.setFieldValue(fieldName, Boolean.parseBoolean(screenValue));
	                	  }else {
	                		  record.setFieldValue(fieldName, "yes".equalsIgnoreCase(screenValue));
	                	  }
	                  } else {
	                     record.setFieldValue(fieldName, screenValue);
	                  }
               } else if (AppServerAdapter.getInstance().allowOrphanFields()) {
            	   record.setFieldValue(fieldName, screenValue);
               }
               
      }
   }
   
}
