package fluid.test.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fluid.test.data.DataSourceDefinition;
import fluid.test.data.FieldDefinition;
import fluid.test.data.Record;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class DataResponse extends ValidationResponse {

   /**
    * Constants
    **/
   public static final String JSON_DATA_SETS_KEY = "dataSets";
   
   /**
    * Instance fields
    **/
   private List<DataSet> ttDataSets = new ArrayList<DataSet>();
   
   /**
    * Default Constructor
    **/
   public DataResponse() {
      super();
   }

   /**
    * Constructor
    * @throws Throwable 
    **/
   public DataResponse(String pcMessage) throws Throwable {
      super(pcMessage);

      if (hasParam(JSON_DATA_SETS_KEY)) {
    	  JsonArray jsDataSets = getParamAsJSONArray(JSON_DATA_SETS_KEY);
         for (int i = 0; i < jsDataSets.size(); i++) {
            JsonObject jsDataSet = (JsonObject) jsDataSets.get(i);
            DataSet dataSet = new DataSet(jsDataSet);
            ttDataSets.add(dataSet);
         }
      }
   }

   @Override
   protected void storeProperties() {
      super.storeProperties();
      if (ttDataSets != null && !ttDataSets.isEmpty()) {
         setDataSetParam(JSON_DATA_SETS_KEY, ttDataSets);
      }
   }

   public void populateRecordList(DataSourceDefinition dsDef) {
	  
      dsDef.setRecordList(null); //clear the current data
      for (DataSet dataSet : ttDataSets) {
         if (dataSet.getName().equalsIgnoreCase(dsDef.getDataSetName())) {
            JsonObject jsDataSet = (JsonObject) dataSet.getDataSet().get(dsDef.getDataSetName());
            
            if (Boolean.TRUE.equals(dataSet.isTrackingChanges())) {
            	dsDef.setTrackingChanges(dataSet.getBiInfo().get(dsDef.getTableName()));
            }
            
            JsonArray jsTable = (JsonArray) jsDataSet.get(dsDef.getTableName());
            if (jsTable != null) {
               dsDef.setDataSetID(dataSet.getDataSetID());
               
               List<Record> records = new ArrayList<Record>();
               for (int i = 0; i < jsTable.size(); i++) {
                  records.add(new Record((JsonObject) jsTable.get(i)));
               }
               
               if (Boolean.TRUE.equals(dsDef.isTrackingChanges())) {
            	    final String prodsIdString = "prods:id";
					dsDef.addFieldDefinition(new FieldDefinition(prodsIdString, "text"));
					dsDef.addFieldDefinition(new FieldDefinition("prods:rowState", "text"));
					
					Map<String, Record> changedRecords = new HashMap<>();
					Boolean hasChanges = (Boolean) jsDataSet.get("prods:hasChanges");
					dsDef.setChanges(hasChanges);
					if (Boolean.TRUE.equals(hasChanges)) {
						JsonObject prodsBefore = (JsonObject) jsDataSet.get("prods:before");
						if (prodsBefore != null && prodsBefore.get(dsDef.getTableName()) != null) {
							JsonArray biRecords = (JsonArray) prodsBefore.get(dsDef.getTableName());
							for (Object obj : biRecords) {
								Record record = new Record((JsonObject) obj);
								changedRecords.put(record.getFieldValueAsString(prodsIdString), record);
							}
						}
					}
					
					dsDef.resetNextRecId();
					
					List<Record> backingRecords = new ArrayList<>();
					for (Record record : records) {
						String prodsId = record.getFieldValueAsString(prodsIdString);
						if (!prodsId.isEmpty()) {
							if (changedRecords.containsKey(prodsId)) {
								record = changedRecords.remove(prodsId);
								//record = changedRecords.get(prodsId);
							}
						} else {
							record.put(prodsIdString, dsDef.getNextRecId());
						}
						backingRecords.add(new Record(record));
					}
					
					for (Record record : changedRecords.values()) {
						backingRecords.add(record);
					}
					
					dsDef.setBackingList(backingRecords);
               }
               
               dsDef.setRecordList(records);
            }
         }
      }
   }
   
   public boolean hasDataSets() {
      return !getDataSets().isEmpty();
   }
   
   public List<DataSet> getDataSets(){
	   return this.ttDataSets;
   }
   
}
