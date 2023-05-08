package fluid.test.data;

public class FieldDefinition {

   private String name;
   private String type;
   private boolean isPrimaryKey;

   public FieldDefinition() {
   }

   public FieldDefinition(String name, String type) {
      super();
      this.name = name;
      this.type = type;
      this.isPrimaryKey = false;
   }

   public FieldDefinition(String name, String type, boolean isPrimaryKey) {
      super();
      this.name = name;
      this.type = type;
      this.isPrimaryKey = isPrimaryKey;
   }

   /**
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * @param name
    *           the name to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the type
    */
   public String getType() {
      return type;
   }

   /**
    * @param type
    *           the type to set
    */
   public void setType(String type) {
      this.type = type;
   }

   /**
    * @return the isPrimaryKey
    */
   public boolean isPrimaryKey() {
      return isPrimaryKey;
   }

   /**
    * @param isPrimaryKey
    *           the isPrimaryKey to set
    */
   public void setPrimaryKey(boolean isPrimaryKey) {
      this.isPrimaryKey = isPrimaryKey;
   }

}
