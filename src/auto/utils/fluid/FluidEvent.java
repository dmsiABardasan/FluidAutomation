package auto.utils.fluid;

import java.util.List;

import fluid.test.data.ActivityDefinition;
import fluid.test.data.DataSourceDefinition;
import fluid.test.data.Record;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.message.EventTypeEnum;
import fluid.test.message.PresentationMessage;

/**
 * <p>
 * Implement request mutation during field validation to mimic certain dynamic
 * UI events that happen within FluidUI.
 * </p>
 */
public interface FluidEvent {
	
	public EventTypeEnum getEventType();
	public void applyEventToRequest(ActivityDefinition activityDef, String tableName, String fieldName, PresentationMessage request);
	
	/** 
	 * <p>Default event that does no mutation to the request</p>
	 */
	public static final FluidEvent LEAVE = new FluidEvent() {
		@Override
		public EventTypeEnum getEventType() {
			return EventTypeEnum.LEAVE;
		}
		@Override
		public void applyEventToRequest(ActivityDefinition activityDef, String tableName, String fieldName, PresentationMessage request) {
			// Do nothing
		}
	};
	
	/**
	 * <p>
	 * Event for the drag-and-drop functionality for ListGrids. The target and
	 * dropped records are passed to the server to be checked which a refreshed
	 * table may be received in the response.
	 * </p>
	 */
	public static class RecordDropped implements FluidEvent {
		
		protected int targetIndex; 		// Can only have one position to drop to
		protected int[] droppedIndices; 	// Can have multiple selected
		
		public RecordDropped(int[] droppedIndices, int targetIndex) {
			this.droppedIndices = droppedIndices;
			this.targetIndex = targetIndex;
		}
		
		public RecordDropped(int droppedIndex, int targetIndex) {
			this(new int[] {droppedIndex}, targetIndex);
		}
		
		@Override
		public EventTypeEnum getEventType() {
			return EventTypeEnum.RECORD_DROPPED;
		}
		
		@Override
		public void applyEventToRequest(ActivityDefinition activityDef, String tableName, String fieldName, PresentationMessage request) {
			
			DataSourceDefinition dsDef = activityDef.getDataSourceDefinitionMap().get(tableName);
			if (dsDef == null) {
				return;
			}
			
			List<Record> recordList = dsDef.getRecordList();
			if (recordList == null) {
				return;
			}
			
			JsonArray ttTarget = new JsonArray();
			JsonArray ttDropped = new JsonArray();
			
			ttTarget.add(recordList.get(this.targetIndex));
			for (int droppedIndex : this.droppedIndices) {
				ttDropped.add(recordList.get(droppedIndex));
			}
			
			JsonObject targetRecord = new JsonObject();
			targetRecord.put("ttTarget", ttTarget);
			request.setParam("targetRecord", targetRecord);
			
			JsonObject droppedRecords = new JsonObject();
			droppedRecords.put("ttDropped", ttDropped);
			request.setParam("droppedRecords", droppedRecords);
			
		}
	}
	
	public static class RecordDroppedOtherListGrid extends RecordDropped {

		private String sourceListGridTable;
		
		public RecordDroppedOtherListGrid(String sourceListGridTable, int droppedIndex, int targetIndex) {
			super(droppedIndex, targetIndex);
			this.sourceListGridTable = sourceListGridTable;
		}
		
		public RecordDroppedOtherListGrid(String sourceListGridTable, int[] droppedIndex, int targetIndex) {
			super(droppedIndex, targetIndex);
			this.sourceListGridTable = sourceListGridTable;
		}
		
		@Override
		public void applyEventToRequest(ActivityDefinition activityDef, String tableName, String fieldName, PresentationMessage request) {
			
			DataSourceDefinition targetDsDef = activityDef.getDataSourceDefinitionMap().get(tableName);
			DataSourceDefinition dsDef = activityDef.getDataSourceDefinitionMap().get(this.sourceListGridTable);
			if (dsDef == null || targetDsDef == null) {
				return;
			}
			
			JsonArray ttTarget = new JsonArray();
			List<Record> targetRecordList = targetDsDef.getRecordList();
			if (targetRecordList == null) {
				return;
			}
			if (!targetRecordList.isEmpty()) {
				ttTarget.add(targetRecordList.get(this.targetIndex));
				
				JsonObject targetRecordJso = new JsonObject();
				targetRecordJso.put("ttTarget", ttTarget);
				request.setParam("targetRecord", targetRecordJso);
			}
			
			JsonArray ttDropped = new JsonArray();
			List<Record> recordList = dsDef.getRecordList();
			if (recordList == null) {
				return;
			}
			for (int droppedIndex : this.droppedIndices) {
				ttDropped.add(recordList.get(droppedIndex));
			}
			JsonObject droppedRecords = new JsonObject();
			droppedRecords.put("ttDropped", ttDropped);
			request.setParam("droppedRecords", droppedRecords);
		}
	}
}
