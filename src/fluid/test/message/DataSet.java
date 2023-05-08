package fluid.test.message;

import java.util.HashMap;
import java.util.Map;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class DataSet extends JsonObject {

   private static final long serialVersionUID = -2728324920656443833L;
   private JsonObject dataSet;
   private Boolean trackingChanges;
   private Map<String, Boolean> biInfo = new HashMap<>();
   
   public DataSet() {}

   public DataSet(JsonObject jsDataSet) {
	  
	  setName((String) jsDataSet.get("name"));
	  setDataSetID((String) jsDataSet.get("id"));
	  setDataSet((JsonObject) jsDataSet.get("dataSet"));
	  
      this.trackingChanges = (Boolean) jsDataSet.get("trackingChanges");
      JsonArray biInfo = (JsonArray) jsDataSet.get("biInfo");
      if (biInfo != null) {
	      for (int i = 0 ; i < biInfo.size() ; i++) {
	    	  JsonObject jsonObj = (JsonObject) biInfo.get(i);
	    	  this.biInfo.put((String) jsonObj.get("tableName"), (Boolean) jsonObj.get("trackingChanges"));
	      }
      }
   }
   
   public void setTrackingChanges(Boolean trackingChanges) {
	   this.trackingChanges = trackingChanges;
   }
   
   public Boolean isTrackingChanges() {
	   return this.trackingChanges;
   }
   
   public Map<String, Boolean> getBiInfo() {
	   return this.biInfo;
   }
   
   public void setName(String name) {
      this.put("name", name);
   }

   public String getName() {
      return (String) this.get("name");
   }

   public void setDataSetID(String id) {
      this.put("id", id);
   }

   public String getDataSetID() {
      return (String) this.get("id");
   }

   public JsonObject getDataSet() {
      return dataSet;
   }

   public void setDataSet(JsonObject dataSet) {
      this.dataSet = dataSet;
      this.put("dataSet", this.dataSet); //Needed so that this JS object is added to the serialised message
   }

}
