package fluid.test.message;

import fluid.test.json.JsonObject;

public class CtrlProp extends JsonObject {

   private static final long serialVersionUID = -5377969004654864030L;

   public CtrlProp() {
   }

   public CtrlProp(String name, CtrlPropEnum prop, String value) {
      setName(name);
      setProp(prop);
      setValue(value);
   }

   public void setName(String name) {
      this.put("name", name);
   }

   public String getName() {
      return (String) this.get("name");
   }

   public void setProp(CtrlPropEnum prop) {
      this.put("prop", prop.toString());
   }

   public CtrlPropEnum getProp() {
      return CtrlPropEnum.valueOf((String) this.get("prop"));
   }

   public void setValue(String value) {
      this.put("value", value);
   }

   public String getValue() {
      return (String) this.get("value");
   }
}