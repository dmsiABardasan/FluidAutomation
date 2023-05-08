package fluid.test.message;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class LoadActivityResponse extends PresentationMessage {

   private String activity; 
   private String activityDesc; 
   private String activityIcon;
   private String activityType; 
   private boolean passwordReq;
   private JsonArray dataSourceDefs;
   private JsonArray dataRelations;
   private String resultsDSPath;
   private JsonObject viewDetails;
   
   public LoadActivityResponse() {
      super();
   }

   public LoadActivityResponse(String pcMessage) throws Throwable {
      super(pcMessage);
      activity = getParamAsString("activity");
      activityDesc = getParamAsString("activityDesc");
      activityIcon = getParamAsString("activityIcon");
      activityType = getParamAsString("activityType");
      passwordReq = getParamAsBoolean("passwordReq");
      
      dataSourceDefs = getParamAsJSONArray("dataSourceDefs");
      dataRelations = getParamAsJSONArray("dataRelations");
      resultsDSPath = getParamAsString("resultsDSPath");
      viewDetails = getParamAsJSONObject("viewDetails");
   }
   
   
   /**
    * @return the activity
    */
   public String getActivity() {
      return activity;
   }

   /**
    * @param activity the activity to set
    */
   public void setActivity(String activity) {
      this.activity = activity;
   }

   /**
    * @return the activityDesc
    */
   public String getActivityDesc() {
      return activityDesc;
   }

   /**
    * @param activityDesc the activityDesc to set
    */
   public void setActivityDesc(String activityDesc) {
      this.activityDesc = activityDesc;
   }

   /**
    * @return the activityIcon
    */
   public String getActivityIcon() {
      return activityIcon;
   }

   /**
    * @param activityIcon the activityIcon to set
    */
   public void setActivityIcon(String activityIcon) {
      this.activityIcon = activityIcon;
   }

   /**
    * @return the activityType
    */
   public String getActivityType() {
      return activityType;
   }

   /**
    * @param activityType the activityType to set
    */
   public void setActivityType(String activityType) {
      this.activityType = activityType;
   }

   /**
    * @return the passwordReq
    */
   public boolean isPasswordReq() {
      return passwordReq;
   }

   /**
    * @param passwordReq the passwordReq to set
    */
   public void setPasswordReq(boolean passwordReq) {
      this.passwordReq = passwordReq;
   }

   /**
    * @return the dataSourceDefs
    */
   public JsonArray getDataSourceDefs() {
      return dataSourceDefs;
   }

   /**
    * @param dataSourceDefs the dataSourceDefs to set
    */
   public void setDataSourceDefs(JsonArray dataSourceDefs) {
      this.dataSourceDefs = dataSourceDefs;
   }

   /**
    * @return the dataRelations
    */
   public JsonArray getDataRelations() {
      return dataRelations;
   }

   /**
    * @param dataRelations the dataRelations to set
    */
   public void setDataRelations(JsonArray dataRelations) {
      this.dataRelations = dataRelations;
   }

   /**
    * @return the resultsDSPath
    */
   public String getResultsDSPath() {
      return resultsDSPath;
   }

   /**
    * @param resultsDSPath the resultsDSPath to set
    */
   public void setResultsDSPath(String resultsDSPath) {
      this.resultsDSPath = resultsDSPath;
   }

   /**
    * @return the viewDetails
    */
   public JsonObject getViewDetails() {
      return viewDetails;
   }

   /**
    * @param viewDetails the viewDetails to set
    */
   public void setViewDetails(JsonObject viewDetails) {
      this.viewDetails = viewDetails;
   }
   
   @Override
   protected void storeProperties() {
      super.storeProperties();
      if (activity != null) {
         setParam("activity", activity);
      }
      if (activityDesc != null) {
         setParam("activityDesc", activityDesc);
      }
      if (activityIcon != null) {
         setParam("activityIcon", activityIcon);
      }
      if (activityType != null) {
         setParam("activityType", activityType);
      }
      setParam("passwordReq", passwordReq);
      
      if (dataSourceDefs != null) {
         setParam("dataSourceDefs", dataSourceDefs);
      }
      if (dataRelations != null) {
         setParam("dataRelations", dataRelations);
      }
      if (resultsDSPath != null) {
         setParam("resultsDSPath", resultsDSPath);
      }
      if (viewDetails != null) {
         setParam("viewDetails", viewDetails);
      }
      
   }
   
}