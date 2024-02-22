package fluid.test.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class SaveDataRequest extends Request {
   
   /* Constants */
   private static final String JSON_KEY_DATA_SETS = "dataSets";
   private static final String JSON_KEY_STOP_ON_WARN = "stopOnWarning";
   private static final String JSON_KEY_VAL_ONLY = "validateOnly";

   /* Instance fields */
   private Boolean stopOnWarning;
   private Boolean validateOnly = Boolean.FALSE;
   private List<DataSet> ttDataSets = new ArrayList<DataSet>();

   /**
    * Default Constructor
    */
   public SaveDataRequest() {
      super();
   }
   
   public SaveDataRequest(boolean deleteData, Map<String, DataSourceDefinition> aDsDefMap) {
      super();
      buildSaveDataJson(deleteData, aDsDefMap);
   }

   @Override
   public void storeProperties() {
      super.storeProperties();

      if (ttDataSets != null && !ttDataSets.isEmpty()) {
         setDataSetParam(JSON_KEY_DATA_SETS, ttDataSets);
      }

      if (stopOnWarning != null) {
         setParam(JSON_KEY_STOP_ON_WARN, stopOnWarning);
      }

      if (validateOnly != null) {
         setParam(JSON_KEY_VAL_ONLY, validateOnly);
      }
   }

   /**
    * @param deleteData
    * @param aDsDefMap
    */
   public void buildSaveDataJson(boolean deleteData, Map<String, DataSourceDefinition> aDsDefMap) {
      // Loop over all data sources
      for (String tableName : aDsDefMap.keySet()) {
         // Get data from the definition
         DataSourceDefinition dsDef = aDsDefMap.get(tableName);
         if (dsDef.getDataSetName() != null && !dsDef.getDataSetName().isEmpty()) {            
            DataSet dataSet = getDataSet(dsDef.getDataSetName(), dsDef.getDataSetID());
            //if (!deleteData) {
               
               List<Record> records = deleteData ? Collections.emptyList() : dsDef.getRecordList();
               List<Record> backingRecords = dsDef.getBackingList();
               
               if (records != null && backingRecords != null) {
                  if (dsDef.isTrackingChanges() != null) {
                	  
                	  final String prodsHasChangesString = "prods:hasChanges";
                	  final String prodsBeforeString = "prods:before";
                	  
                	  boolean dataSetHasChanges = false;
                	  
                	  if (dsDef.hasChanges() != null && dsDef.hasChanges()) {
                		  dataSetHasChanges = (boolean) dsDef.hasChanges();
                	  }
                	  
                	  if (!backingRecords.equals(records)) {
                		  dataSetHasChanges = true;
                	  }
                	  
                	  List<Record> differences = new ArrayList<>(backingRecords);
                	  differences.removeAll(records);
                	  
                	  final String prodsIdString = "prods:id";
                	  final String prodsStateString = "prods:rowState";
                	  
                	  List<Record> before = new ArrayList<>();
                	  if (dsDef.isTrackingChanges()) {
	                	  for (Record difference : differences) {
	                		  if ("created".equals(difference.get(prodsStateString))) {
	                			  continue;
	                		  }
	                		  boolean found = false;
	                		  for (Record record : records) {
	                			  String recordId = (String) record.get(prodsIdString);
	                			  String diffId = (String) difference.get(prodsIdString);
	                			  if (diffId.equals(recordId)) {
	                				  difference.setFieldValue(prodsStateString, "modified");
	                				  record.setFieldValue(prodsStateString, "modified");
	                				  found = true;
	                				  break;
	                			  }
	                		  }
	                		  if (!found) {
	                			  difference.setFieldValue(prodsStateString, "deleted");
	                		  }
	                		  before.add(difference);
	                	  }
	                	  List<Record> after = new ArrayList<>();
	                	  for (Record record : records) {
	                		  if (!record.containsKey(prodsIdString)) {
	                			  record.setFieldValue(prodsIdString, dsDef.getNextRecId());
	                			  record.setFieldValue(prodsStateString, "created");
	                		  }
	                		  Record shallowRecordCopy = new Record(record);
	                		  if (!shallowRecordCopy.containsKey(prodsStateString)) {
	                			  shallowRecordCopy.remove(prodsIdString);
	                		  }
	                		  after.add(shallowRecordCopy);
	                	  }
	                	  records = after;
                	  }
                	  
                	  dataSet.getDataSet().put(prodsHasChangesString, dataSetHasChanges); //auto-boxed
                	  
                	  JsonObject prodsBefore;
                	  if (dataSet.getDataSet().containsKey(prodsBeforeString)) {
                		  prodsBefore = (JsonObject) dataSet.getDataSet().get(prodsBeforeString);
                	  } else {
                		  prodsBefore = new JsonObject();
                		  dataSet.getDataSet().put(prodsBeforeString, prodsBefore);
                	  }
                	  
                	  if (dsDef.isTrackingChanges()) {
                		  if (!before.isEmpty()) {
                			  prodsBefore.put(tableName, before);
                		  }
                	  }
                  }
                  
			      if (records.size() >= 1) {
			          dataSet.getDataSet().put(tableName, new JsonArray(records));
			      }
               }
            //}
         }
      }

      // Add the data source JSON to a data set JSON
      for (DataSet ds : ttDataSets) {
         if (!ds.getName().isEmpty()) {
            JsonObject jsonDataSet = new JsonObject();
            jsonDataSet.put(ds.getName(), ds.getDataSet());
            ds.setDataSet(jsonDataSet);
         }
      }
   }

   /**
    * @return the stopOnWarning
    */
   public Boolean getStopOnWarning() {
      return stopOnWarning;
   }

   /**
    * @param stopOnWarning
    *           the stopOnWarning to set
    */
   public void setStopOnWarning(Boolean stopOnWarning) {
      this.stopOnWarning = stopOnWarning;
   }

   /**
    * @return the validateOnly
    */
   public Boolean getValidateOnly() {
      return validateOnly;
   }

   /**
    * @param validateOnly
    *           the validateOnly to set
    */
   public void setValidateOnly(Boolean validateOnly) {
      this.validateOnly = validateOnly;
   }

   /**
    * @return the dataSet
    */
   private DataSet getDataSet(String dataSetName, String dataSetID) {
      DataSet dataSet = null;
      for (DataSet ds : ttDataSets) {
         if (ds.getName().equalsIgnoreCase(dataSetName)) {
            dataSet = ds;
            break;
         }
      }
      if (dataSet == null) {
         dataSet = new DataSet();
         dataSet.setName(dataSetName);
         dataSet.setDataSetID(dataSetID);
         JsonObject js = new JsonObject();
         dataSet.setDataSet(js);
         ttDataSets.add(dataSet);
      }
      return dataSet;
   }

}