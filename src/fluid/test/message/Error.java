package fluid.test.message;

import fluid.test.json.JsonObject;

public class Error extends JsonObject {

   private static final long serialVersionUID = 4241893866425641855L;

   public Error() {
   }

   public Error(String field, String type, String msg) {
      super();
      setField(field);
      setType(type);
      setMsg(msg);
   }
   
   public Error(String field, String type, String msg, String lineid) {
	   this(field, type, msg);
	   setLineid(lineid);
   }
   
   /**
    * @return the field
    */
   public String getField() {
      return (String) this.get("field");
   }

   /**
    * @param field the field to set
    */
   public void setField(String field) {
      this.put("field", field);
   }

   /**
    * @return the type
    */
   public String getType() {
      return (String) this.get("type");
   }

   /**
    * @param type the type to set
    */
   public void setType(String type) {
      this.put("type", type);
   }

   /**
    * @return the msg
    */
   public String getMsg() {
      return (String) this.get("msg");
   }

   /**
    * @param msg the msg to set
    */
   public void setMsg(String msg) {
      this.put("msg", msg);
   }

   /**
    * @return line number the error occurred on
    */
   public String getLineid() {
	   return (String) this.get("lineid");
   }
   
   /**
    * @param lineid String line number the error occurred on
    */
   public void setLineid(String lineid) {
	   this.put("lineid", lineid);
   }

}
