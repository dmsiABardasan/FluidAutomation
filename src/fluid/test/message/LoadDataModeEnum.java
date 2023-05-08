package fluid.test.message;

public enum LoadDataModeEnum {
   
   EDIT("EDIT"), ADD("ADD"), COPY("COPY"), VIEW("VIEW");

   private String value;

   private LoadDataModeEnum(String value) {
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

}
