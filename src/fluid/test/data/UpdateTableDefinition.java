package fluid.test.data;

import java.util.List;

public class UpdateTableDefinition {

   private String tableName;
   private List<Record> recordList;
   
   public UpdateTableDefinition(String tableName, List<Record> recordList) {
      super();
      this.tableName = tableName;
      this.recordList = recordList;
   }

   public void setTableName(String tableName) {
      this.tableName = tableName;
   }

   public String getTableName() {
      return tableName;
   }

   public List<Record> getRecordList() {
      return recordList;
   }

   public void setRecordList(List<Record> recordList) {
      this.recordList = recordList;
   }

}
