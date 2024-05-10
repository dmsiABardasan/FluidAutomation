package fluid.test.message;

import fluid.test.data.DataSourceDefinition;

public class GetResultsResponse extends PresentationMessage {

   public GetResultsResponse() {
      super();
   }

   public GetResultsResponse(String pcMessage) throws Throwable {
      super(pcMessage);
   }
   
	@Override
	public void populateRecordList(DataSourceDefinition dsDef) {
		dsDef.setRecordList(null);
		super.populateRecordList(dsDef);
	}
   
}
