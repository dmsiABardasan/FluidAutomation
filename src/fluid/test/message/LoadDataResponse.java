package fluid.test.message;

public class LoadDataResponse extends PresentationMessage {

   private LoadDataModeEnum loadDataMode;
	
   public LoadDataResponse(LoadDataModeEnum loadDataMode) {
      super();
      this.loadDataMode = loadDataMode;
   }

   public LoadDataResponse(String pcMessage, LoadDataModeEnum loadDataMode) throws Throwable {
      super(pcMessage);
      this.loadDataMode = loadDataMode;
   }
   
   public LoadDataModeEnum getLoadDataMode() {
	   return this.loadDataMode;
   }
}