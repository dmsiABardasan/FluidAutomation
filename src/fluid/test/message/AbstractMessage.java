package fluid.test.message;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import fluid.test.data.Record;
import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;
import fluid.test.json.Jsoner;

public abstract class AbstractMessage {

   private JsonObject moMsgParams;

   private String messageKeyPin = null;

   public AbstractMessage() {
      moMsgParams = new JsonObject();
   }

   public boolean isEmpty() {
	   return moMsgParams.isEmpty();
   }
   
   public AbstractMessage(String pcMessage) throws Throwable {
      if (pcMessage == null || pcMessage.isEmpty()) {
         pcMessage = "{}";
      }
      if (!pcMessage.trim().startsWith("{")) {
         Throwable e = new Throwable("Not JSON message:" + pcMessage);
         e.fillInStackTrace();
         e.printStackTrace();
         throw e;
      }
      
      moMsgParams = (JsonObject) Jsoner.deserialize(pcMessage);
      this.messageKeyPin = getParamAsString("_password");
   }

   public String getMessageKeyPin() {
      return messageKeyPin;
   }

   public void setMessageKeyPin(String messageKeyPin) {
      this.messageKeyPin = messageKeyPin;
   }

   /**
    * Returns a logical indicating if this message object contains the named
    * parameter.
    * 
    * @param pcParam
    *           The name of the param
    */
   public final boolean hasParam(String pcParam) {

      return moMsgParams.containsKey(pcParam);

   }

   /**
    * Remove a parameter from the message object
    * 
    * @param pcParam
    *           The name of the param to be removed
    */
   public final void removeParam(String pcParam) {

      if (hasParam(pcParam)) {
         moMsgParams.remove(pcParam);
      }

   }

   public final void setParam(String pcParam, Object poValue) {
	   moMsgParams.put(pcParam, poValue);
   }
   
   public final void setParam(String pcParam, Object[] plValue) {

	   JsonArray array = new JsonArray();
      for (int i = 0; i < plValue.length; i++) {
         array.add(i, plValue[i]);
      }

      setParam(pcParam, array);

   }
   
   public final void setParam(String pcParam, JsonObject poValue) {

      if (poValue == null) {
         poValue = new JsonObject();
      }

      moMsgParams.put(pcParam, poValue.clone());

   }

   public final void setParam(String pcParam, JsonArray poValue) {

      if (poValue == null) {
         poValue = new JsonArray();
      }

      moMsgParams.put(pcParam, poValue.clone());

   }

   public final void setDataSetParam(String pcParam, List<DataSet> poValue) {

	  JsonArray jsDataSets = new JsonArray();
      jsDataSets.addAll(poValue);

      moMsgParams.put(pcParam, jsDataSets.clone());

   }

   public final void setParam(String pcParam, Date ptValue) {

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      moMsgParams.put(pcParam, df.format(ptValue));

   }

   public final void setParam(String pcParam, Date[] ptValue) {

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      JsonArray array = new JsonArray();
      for (int i = 0; i < ptValue.length; i++) {
         array.add(i, df.format(ptValue[i]));
      }

      setParam(pcParam, array);

   }

   public Object getParamAsObject (String pcParam) {
	   return (Object) moMsgParams.get(pcParam);
   }
   
   public JsonObject getParamAsJSONObject(String pcParam) {

      return (JsonObject) moMsgParams.get(pcParam);

   }

   public JsonArray getParamAsJSONArray(String pcParam) {

      return (JsonArray) moMsgParams.get(pcParam);

   }

   /**
    * Get the Parameter value as a character string
    * 
    * @param pcParam
    *           The name of the Parameter to get
    * @return The Parameter value as a character string
    */
   public String getParamAsString(String pcParam) {
      return (String) moMsgParams.get(pcParam);
   }

   public String[] getParamAsStringEx(String pcParam) {

	   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
      if (array == null) {
         return null;
      }

      String[] retArray = new String[array.size()];

      for (int i = 0; i < array.size(); i++) {
         retArray[i] = (String) array.get(i);
      }

      return retArray;

   }

   /**
    * Get the Parameter value as a Integer
    * 
    * @param pcParam
    *           The name of the Parameter to get
    * @return The Parameter value as a Integer
    */
   public Integer getParamAsInteger(String pcParam) {
      return (Integer) moMsgParams.get(pcParam);
   }

   public Integer[] getParamAsIntegerEx(String pcParam) {

	   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
      if (array == null) {
         return null;
      }

      Integer[] retArray = new Integer[array.size()];

      for (int i = 0; i < array.size(); i++) {
         retArray[i] = (Integer) array.get(i);
      }

      return retArray;

   }
   
	public BigInteger getParamAsBigInteger(String pcParam) {
		return (BigInteger) moMsgParams.get(pcParam);
	}

	public BigInteger[] getParamAsBigIntegerEx(String pcParam) {
		JsonArray array = (JsonArray) moMsgParams.get(pcParam);
		if (array == null) {
			return null;
		}

		BigInteger[] retArray = new BigInteger[array.size()];
		for (int i = 0; i < array.size(); i++) {
			retArray[i] = (BigInteger) array.get(i);
		}
		return retArray;
	}
	
	public BigDecimal getParamAsBigDecimal(String pcParam) {
		return (BigDecimal) moMsgParams.get(pcParam);
	}

	public BigDecimal[] getParamAsBigDecimalEx(String pcParam) {
		JsonArray array = (JsonArray) moMsgParams.get(pcParam);
		if (array == null) {
			return null;
		}

		BigDecimal[] retArray = new BigDecimal[array.size()];
		for (int i = 0; i < array.size(); i++) {
			retArray[i] = (BigDecimal) array.get(i);
		}
		return retArray;
	}
	
	public Long getParamAsLong(String pcParam) {
	      return (Long) moMsgParams.get(pcParam);
	   }

	public Long[] getParamAsLongEx(String pcParam) {
	
		   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
	      if (array == null) {
	         return null;
	      }
	
	      Long[] retArray = new Long[array.size()];
	
	      for (int i = 0; i < array.size(); i++) {
	         retArray[i] = (Long) array.get(i);
	      }
	
	      return retArray;
	
	   }
	
	public Boolean getParamAsBoolean(String pcParam) {
	      return (Boolean) moMsgParams.get(pcParam);
	   }

	public Boolean[] getParamAsBooleanEx(String pcParam) {
	
		   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
	      if (array == null) {
	         return null;
	      }
	
	      Boolean[] retArray = new Boolean[array.size()];
	
	      for (int i = 0; i < array.size(); i++) {
	         retArray[i] = (Boolean) array.get(i);
	      }
	
	      return retArray;
	
	   }

	@Deprecated // Not recommended for production, precision can be lost
   public Double getParamAsDouble(String pcParam) {
      return (Double) moMsgParams.get(pcParam);
   }

	@Deprecated // Not recommended for production, precision can be lost
   public Double[] getParamAsDoubleEx(String pcParam) {

	   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
      if (array == null) {
         return null;
      }

      Double[] retArray = new Double[array.size()];

      for (int i = 0; i < array.size(); i++) {
         retArray[i] = (Double) array.get(i);
      }

      return retArray;

   }

   @Deprecated // Not recommended for production, precision can be lost
   public Float getParamAsFloat(String pcParam) {
      return (Float) moMsgParams.get(pcParam);
   }

   @Deprecated // Not recommended for production, precision can be lost
   public Float[] getParamAsFloatEx(String pcParam) {

	   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
      if (array == null) {
         return null;
      }

      Float[] retArray = new Float[array.size()];

      for (int i = 0; i < array.size(); i++) {
         retArray[i] = (Float) array.get(i);
      }

      return retArray;

   }

   public Date getParamAsDate(String pcParam) {

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      try {
         return df.parse(((String) moMsgParams.get(pcParam)));
      } catch (java.text.ParseException e) {
         return null;
         // e.printStackTrace();
      }

   }

   public Date getParamAsDatetime(String pcParam) {

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
      try {
         return df.parse(((String) moMsgParams.get(pcParam)));
      } catch (java.text.ParseException e) {
         return null;
         // e.printStackTrace();
      }

   }

   public Date getParamAsDatetimeTZ(String pcParam) {

      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      try {
         return df.parse(((String) moMsgParams.get(pcParam)));
      } catch (java.text.ParseException e) {
         return null;
         // e.printStackTrace();
      }

   }

   public Date[] getParamAsDateEx(String pcParam) {

	   JsonArray array = (JsonArray) moMsgParams.get(pcParam);
      if (array == null) {
         return null;
      }

      Date[] retArray = new Date[array.size()];

      for (int i = 0; i < array.size(); i++) {
         retArray[i] = (Date) array.get(i);
      }

      return retArray;

   }

   public List<Record> getParamAsDataSet(String pcParam) {
      /*
       * Fluid sends: "newLineTable":{"ttSampleLine":[{...}]}
       * Spark sends: "newLineTable":[{...}]
       */
      Object jsObj = moMsgParams.get(pcParam);
      if (jsObj == null) {
         return null;
      }
      if (jsObj instanceof JsonArray) {
    	  JsonArray jsonTable = (JsonArray) jsObj;
         return getTableArray(jsonTable);
      } else {
    	 JsonObject jsonObj = (JsonObject) jsObj;
    	 JsonArray jsonTable = null;
         for (@SuppressWarnings("rawtypes")
         Iterator iterator = jsonObj.keySet().iterator(); iterator.hasNext();) {
            String property = (String) iterator.next();
            jsonTable = (JsonArray) jsonObj.get(property);
            break;
         }
         return getTableArray(jsonTable);
      }
   }

   private List<Record> getTableArray(JsonArray jsTable) {
      List<Record> records = null;
      if (jsTable != null) {
         records = new ArrayList<Record>();
         for (int i = 0; i < jsTable.size(); i++) {
        	JsonObject jsonRecord = (JsonObject) jsTable.get(i);
            Record record = new Record();
            for (@SuppressWarnings("rawtypes")
            Iterator iterator = jsonRecord.keySet().iterator(); iterator.hasNext();) {
               String property = (String) iterator.next();
               record.setFieldValue(property, jsonRecord.get(property));
            }
            records.add(record);
         }
      }
      return records;
   }

   /**
    * Serialise the message for passing through the service interface
    * 
    * @return A String JSON string
    */
   public String serializeMessage() {

      /* Call StoreProperties in all inherited classes */
      storeProperties();

      if (messageKeyPin != null) {
         setParam("_password", messageKeyPin);
      }
      
      return moMsgParams.toJson();

   }

   public JsonObject getMoMsgParams(){
	   return moMsgParams;
   }
   
   /**
    * An abstract method to be implemented by the concrete Message classes
    *
    * This method must save any local Properties or DataSets using setParam()
    * methods so that they can be serialised and de-serialised. This method is
    * called from the serializeMessage method. Whilst all static members could
    * store and retrieve their values directly via setParam/getParam methods a
    * dataset's or temp-table's handle can be set at any point during the
    * service request. At the end of the service request, the dataset needs to
    * be serialised and stored in the message object and not before as the data
    * would be changing.
    */
   protected abstract void storeProperties();

}
