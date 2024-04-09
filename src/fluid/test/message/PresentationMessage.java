package fluid.test.message;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Objects;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class PresentationMessage extends DataResponse {

   public static final String CTRL_PROPS_JSON_KEY = "ctrlProp";

   private String callingField;
   private LinkedHashMap<CtrlPropPair, String> ttCtrlProps = new LinkedHashMap<CtrlPropPair, String>();

   public PresentationMessage() {
      super();
   }

   public PresentationMessage(String pcMessage) throws Throwable {
      super(pcMessage);
      
      if (hasParam(CTRL_PROPS_JSON_KEY)) {
    	  JsonArray jsCtrlProps = getParamAsJSONArray(CTRL_PROPS_JSON_KEY);
         for (@SuppressWarnings("rawtypes")
         Iterator iterator = jsCtrlProps.iterator(); iterator.hasNext();) {
            JsonObject jsCtrlProp = (JsonObject) iterator.next();
            
            String controlName = (String) jsCtrlProp.get("name");
            String controlProp = (String) jsCtrlProp.get("prop");
            String propValue = (String) jsCtrlProp.get("value");
            CtrlPropEnum controlPropEnum = CtrlPropEnum.valueOf(controlProp);
            setCtrlProp(controlName, controlPropEnum, propValue);
         }
      }
      
   }
   
   public void setCallingField(String callingField) {
	   this.callingField = callingField;
   }
   
   public String getCallingField() {
	   return this.callingField;
   }
   
   public boolean hasCtrlProps() {
      return !ttCtrlProps.isEmpty();
   }
   
   public boolean hasCtrlProp(CtrlPropEnum prop) {
      for (CtrlPropPair pair : ttCtrlProps.keySet()) {
         if (prop.equals(pair.getProp())) {
            return true;
         }
      }
      return false;
   }

	public void setCtrlProp(String name, CtrlPropEnum prop, String value) {
		ttCtrlProps.put(new CtrlPropPair(name, prop), value);
	}

   public String getCtrlProp(String name, CtrlPropEnum prop) {
	  return ttCtrlProps.get(new CtrlPropPair(name, prop));
   }

   public void removeCtrlProp(CtrlPropEnum prop) {
      Iterator<CtrlPropPair> iter = ttCtrlProps.keySet().iterator();
      while (iter.hasNext()) {
         CtrlPropPair pair = (CtrlPropPair) iter.next();
         if (prop.equals(pair.getProp())) {
            iter.remove();
         }
      }
   }
   
   public void removeCtrlProp(String name, CtrlPropEnum prop, String value) {
	   CtrlPropPair ctrlPropPair = new CtrlPropPair(name, prop);
	   String oldValue = ttCtrlProps.get(ctrlPropPair);
	   if (Objects.equals(value, oldValue)) {
		   ttCtrlProps.remove(ctrlPropPair);
	   }
   }
   
   public void removeAllCtrlProp(String name, CtrlPropEnum prop, String value, boolean before) {
      boolean foundClose = false;
      Iterator<CtrlPropPair> iter = ttCtrlProps.keySet().iterator();
      while (iter.hasNext()) {
         CtrlPropPair pair = (CtrlPropPair) iter.next();
         if (!foundClose) {
            if (pair.getProp().equals(prop) && pair.getName().equalsIgnoreCase(name)) {
               String test = ttCtrlProps.get(pair);
               if ((test == null && value == null) || (test != null && test.equalsIgnoreCase(value))) {
                  foundClose = true;
               }
            }
            if (before) {
               iter.remove();
            }
         } else if (!before) {
            iter.remove();
         }
      }
   }

   @Override
   protected void storeProperties() {
      super.storeProperties();
      JsonArray ttCtrlProp = getCtrlPropList();
      if (ttCtrlProp != null && !ttCtrlProp.isEmpty()) {
         setParam(CTRL_PROPS_JSON_KEY, ttCtrlProp);
      }
   }

   private JsonArray getCtrlPropList() {
	   JsonArray ttCtrlProp = new JsonArray();
      for (CtrlPropPair pair : ttCtrlProps.keySet()) {
         String value = ttCtrlProps.get(pair);
         CtrlProp c = new CtrlProp(pair.getName(), pair.getProp(), value);
         ttCtrlProp.add(c);
      }
      return ttCtrlProp;
   }

   public LinkedHashMap<CtrlPropPair, String> getCtrlProperties() {
      return ttCtrlProps;
   }
   
   public void setCtrlProperties (LinkedHashMap<CtrlPropPair, String> ctrlProperties) {
	   this.ttCtrlProps = ctrlProperties;
   }

}
