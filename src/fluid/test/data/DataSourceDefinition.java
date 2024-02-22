package fluid.test.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fluid.test.adapter.AppServerAdapter;

public class DataSourceDefinition {

   private String tableName;
   
   private String dataSetName;
   private String dataSetID;
   private Map<String, FieldDefinition> fieldDefinitionMap = new HashMap<String, FieldDefinition>();
   private ViewDefinition viewDefinition = new ViewDefinition();
   private String primaryKey;
   private int currentRow;
   private int firstRow;
   private int lastRow;

   private Boolean changes;
   private Boolean trackingChanges;
   private int irecId = 0;
   
   private List<Record> backingList = new ArrayList<Record>();
   private List<Record> recordList = new ArrayList<Record>();
   
   public DataSourceDefinition() {}
   
   public DataSourceDefinition(String tableName) {
      setTableName(tableName);
   }
   
   public Boolean hasChanges() {
	   return this.changes;
   }
   
   public void setChanges(Boolean changes) {
	   this.changes = changes;
   }
   
   public void setTrackingChanges(Boolean trackingChanges) {
	   this.trackingChanges = trackingChanges;
   }
   
   public Boolean isTrackingChanges() {
	   return this.trackingChanges;
   }
   
   public List<Record> getBackingList() {
	   return this.backingList;
   }
   
   public ViewDefinition getViewDefinition() {
      return this.viewDefinition;
   }
   
   public String getTableName() {
      return this.tableName;
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   public String getDataSetName() {
      return dataSetName;
   }

   public void setDataSetName(String dataSetName) {
      this.dataSetName = dataSetName;
   }

   public String getDataSetID() {
      return dataSetID;
   }

   public void setDataSetID(String dataSetID) {
      this.dataSetID = dataSetID;
   }

   public Map<String, FieldDefinition> getFieldDefinitionMap() {
      return fieldDefinitionMap;
   }

   public void setFieldDefinitionMap(Map<String, FieldDefinition> fieldDefinitionMap) {
      this.fieldDefinitionMap = fieldDefinitionMap;
   }
   
   public void addFieldDefinition(FieldDefinition fieldDefinition) {
      this.fieldDefinitionMap.put(fieldDefinition.getName(), fieldDefinition);
   }
   
   public FieldDefinition getFieldDefinition( String fieldName ) {
      return this.fieldDefinitionMap.get(fieldName);
   }

   public void updateData(Record updatedRecord) {
      String primKeyValue = updatedRecord.getFieldValueAsString(primaryKey);
      boolean found = false;
      for (int i = 0; i < recordList.size(); i++) {
         Record record = recordList.get(i);
         if (primKeyValue.equalsIgnoreCase(record.getFieldValueAsString(primaryKey))) {
        	
        	record.putAll(updatedRecord);
        	if (trackingChanges == null && !AppServerAdapter.getInstance().allowOrphanFields()) {
        		record.keySet().retainAll(getFieldDefinitionMap().keySet());
        	}
        	
            currentRow = i;
            found = true;
            break;
         }
      }
      if (!found) {
         addData(updatedRecord);
      }
   }

	private void addData(Record record) {
		Record newRecord = new Record(record);
		if (trackingChanges == null && !AppServerAdapter.getInstance().allowOrphanFields()) {
			newRecord.keySet().retainAll(getFieldDefinitionMap().keySet());
		}
		recordList.add(newRecord);
		
		lastRow = recordList.size() - 1;
		currentRow = lastRow;
	}
   
	public void refreshData(List<Record> refreshRecords) {
		List<Record> newRecords = new ArrayList<Record>();
		if (refreshRecords != null) {
			for (Record refreshRecord : refreshRecords) {
				String primKeyValue = refreshRecord.getFieldValueAsString(primaryKey);
				for (Record record : recordList) {
					if (primKeyValue.equalsIgnoreCase(record.getFieldValueAsString(primaryKey))) {
						record.putAll(refreshRecord);
						refreshRecord = record;
						break;
					}
				}
				newRecords.add(refreshRecord);
			}
		}
		setRecordList(newRecords);
	}

	public void removeData(Record oldRecord) {
		int i = recordList.indexOf(oldRecord);
		recordList.remove(oldRecord);
		lastRow = recordList.size() - 1;
		if (currentRow >= i) {
			currentRow = currentRow - 1;
		}
	}
	
   public int getCurrentRow() {
      return currentRow;
   }

   public void setCurrentRow(int currentRow) {
      this.currentRow = currentRow;
   }
   
   public void navToFirst() {
      currentRow = firstRow;
   }
   
   public void navToPrev() {
      if (currentRow > firstRow) currentRow -= 1;
   }
   
   public void navToNext() {
      if (currentRow < lastRow) currentRow += 1;
   }
   
   public void navToLast() {
      currentRow = lastRow;
   }

   public Record getCurrentRecord() {
      return recordList.get(currentRow);
   }

   public String getPrimaryKey() {
      return primaryKey;
   }

   public void setPrimaryKey(String primaryKey) {
      this.primaryKey = primaryKey;
   }

   public List<Record> getRecordList() {
      return recordList;
   }

   public void setBackingList(List<Record> records) {
	   this.backingList = records;
   }
   
   public void setRecordList(List<Record> recordList) {
		List<Record> newRecordList = new ArrayList<Record>();
		if (recordList != null) {
			for (Record record : recordList) {
				Record newRecord = new Record(record);
				if (trackingChanges == null && !AppServerAdapter.getInstance().allowOrphanFields()) {
					newRecord.keySet().retainAll(getFieldDefinitionMap().keySet());
				}
				newRecordList.add(newRecord);
			}
		}
		
		this.recordList = newRecordList;
		currentRow = 0;
		firstRow = 0;
		if (newRecordList.size() > 0) {
			lastRow = newRecordList.size() - 1;
		}
	}
   
   public String getNextRecId() {
	  ++irecId;
	  if (irecId >= 99999999) {
	     irecId = 1;
	  }
	  return tableName + "FldId" + irecId;
   }
	
   public void resetNextRecId() {
      irecId = 0;      
   }
}