package fluid.test.message;

public enum ErrorTypeEnum {
   FATAL("FATAL"), ERROR("ERROR"), WARNING("WARNING"), INFORMATION(
         "INFORMATION");

   private String value;

   private ErrorTypeEnum(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }
}
