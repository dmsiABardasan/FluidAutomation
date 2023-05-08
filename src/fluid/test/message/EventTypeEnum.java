package fluid.test.message;

public enum EventTypeEnum {
   LEAVE("LEAVE"), VALUE_CHANGED("VALUE_CHANGED"), KEY_UP("KEY_UP"), ROW_CHANGED(
         "ROW_CHANGED"), SELECTION_UPDATED("SELECTION_UPDATED"), CLICK("CLICK"), BEFORE_ROW_DELETE("BEFORE_ROW_DELETE"), AFTER_ROW_DELETE(
         "AFTER_ROW_DELETE"), BEFORE_ROW_SAVE("BEFORE_ROW_SAVE"), AFTER_ROW_SAVE(
               "AFTER_ROW_SAVE"), RECORD_DROPPED("RECORD_DROPPED"), FORCED("FORCED");

   private String value;

   private EventTypeEnum(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }
}
