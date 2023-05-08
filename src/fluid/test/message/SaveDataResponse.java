package fluid.test.message;

public class SaveDataResponse extends PresentationMessage {
   
   public static final String JSON_KEY_SAVE_ROW_ID = "saveRowid";
   public static final String JSON_KEY_SAVE_SUCCESS = "saveSuccess";

   private String saveRowId;
   private boolean saveSuccess = false;

   public SaveDataResponse() {
      super();
   }

   public SaveDataResponse(String pcMessage) throws Throwable {
      super(pcMessage);

      saveRowId = getParamAsString(JSON_KEY_SAVE_ROW_ID);
      saveSuccess = getParamAsBoolean(JSON_KEY_SAVE_SUCCESS);

   }

   @Override
   protected void storeProperties() {
      super.storeProperties();
      if (saveRowId != null) {
         setParam(JSON_KEY_SAVE_ROW_ID, saveRowId);
      }
      setParam(JSON_KEY_SAVE_SUCCESS, saveSuccess);
   }

   /**
    * @return the saveRowId
    */
   public String getSaveRowId() {
      return saveRowId;
   }

   /**
    * @param saveRowId
    *           the saveRowId to set
    */
   public void setSaveRowId(String saveRowId) {
      this.saveRowId = saveRowId;
   }

   /**
    * @return the saveSuccess
    */
   public boolean getSaveSuccess() {
      return saveSuccess;
   }

   /**
    * @param saveSuccess
    *           the saveSuccess to set
    */
   public void setSaveSuccess(boolean saveSuccess) {
      this.saveSuccess = saveSuccess;
   }
}