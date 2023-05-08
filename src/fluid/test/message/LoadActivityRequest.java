package fluid.test.message;

public class LoadActivityRequest extends PresentationMessage {

   private String activity;

   public LoadActivityRequest() {
      super();
   }

   public LoadActivityRequest(String pcMessage) throws Throwable {
      super(pcMessage);
      setActivity(getParamAsString("activity"));
   }

   public String getActivity() {
      return activity;
   }

   public void setActivity(String activity) {
      this.activity = activity;
   }

   @Override
   protected void storeProperties() {
      super.storeProperties();
      if (activity != null) {
         setParam("activity", activity);
      }
   }

}
