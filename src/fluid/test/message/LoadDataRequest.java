package fluid.test.message;

public class LoadDataRequest extends Request {

   private LoadDataModeEnum loadMode;
   private String loadRowid;
   
   /**
	 * 
	 */
   public LoadDataRequest(LoadDataModeEnum loadDataMode) {
      super();
      this.loadMode = loadDataMode;
      this.setParam("loadRowid", "");
   }

   /**
    * @param pcMessage
    * @throws Throwable 
    */
   public LoadDataRequest(String pcMessage) throws Throwable {
      super(pcMessage);
      try {
         this.loadRowid = getParamAsString("loadRowid");
         this.loadMode = LoadDataModeEnum.valueOf(getParamAsString("loadMode")); 
      } catch (Exception e) {
         // nothing
      }
   }

   public LoadDataRequest(LoadDataModeEnum loadMode, String loadRowid) {
      super();
      this.loadMode = loadMode;
      this.loadRowid = loadRowid;
   }

   /**
    * @return the loadMode
    */
   public LoadDataModeEnum getLoadMode() {
      return loadMode;
   }

   /**
    * @param loadMode
    *           the loadMode to set
    */
   public void setLoadMode(LoadDataModeEnum loadMode) {
      this.loadMode = loadMode;
   }

   /**
    * @return the loadRowid
    */
   public String getLoadRowid() {
      return loadRowid;
   }

   /**
    * @param loadRowid
    *           the loadRowid to set
    */
   public void setLoadRowid(String loadRowid) {
      this.loadRowid = loadRowid;
   }
   
   @Override
   protected void storeProperties() {
      super.storeProperties();
      setParam("loadMode", loadMode.getValue());
      if (loadRowid != null) setParam("loadRowid", loadRowid);
   }

}
