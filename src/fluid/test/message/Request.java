package fluid.test.message;

public class Request extends AbstractMessage {

   public Request() {
      
   }

   public Request(String pcMessage) throws Throwable {
      super(pcMessage);
   }

   @Override
   protected void storeProperties() {

   }
   
}
