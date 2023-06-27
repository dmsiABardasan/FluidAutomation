package fluid.test.message;

public class Response extends AbstractMessage {

   private long sentTime;
   private long receivedTime;

   public Response() {
      super();
   }

   public Response(String pcMessage) throws Throwable {
      super(pcMessage);
   }

   @Override
   protected void storeProperties() {

   }

	public long getSentTime() {
		return sentTime;
	}
	
	public long getReceivedTime() {
		return receivedTime;
	}
	
	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}
	
	public void setReceivedTime(long receivedTime) {
		this.receivedTime = receivedTime;
	}
	
	public long getResponseTime() {
		return this.receivedTime - this.sentTime;
	}
   
}
