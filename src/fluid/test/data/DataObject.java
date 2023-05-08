package fluid.test.data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import fluid.test.json.JsonObject;


public class DataObject extends JsonObject {

   private static final long serialVersionUID = -2791032702110408712L;

   public DataObject() {
   }

   @SuppressWarnings("unchecked")
   public DataObject(@SuppressWarnings("rawtypes") Map map) {
      super(map);
   }
   
   public void setFieldValue(String property, Object value) {
      this.put(property, value);
   }
   
   public Object getFieldValueAsObject(String pcParam) {
      return this.get(pcParam);
   }
   
   public String getFieldValueAsString(String pcParam) {
	   return Objects.toString(this.get(pcParam), "");
   }
   
   public int getFieldValueAsInteger(String pcParam) {
      return new BigDecimal(stripNonNumbers(this.get(pcParam).toString())).intValueExact();
   }
   
   public BigInteger getFieldValueAsBigInteger(String pcParam) {
	   return new BigDecimal(stripNonNumbers(this.get(pcParam).toString())).toBigIntegerExact();
   }
   
   public BigDecimal getFieldValueAsBigDecimal(String pcParam) {
	   return new BigDecimal(stripNonNumbers(this.get(pcParam).toString()));
   }
   
   @Deprecated // Not recommended for production, precision can be lost
   public double getFieldValueAsDouble(String pcParam) {
      return new BigDecimal(stripNonNumbers(this.get(pcParam).toString())).doubleValue();
   }
   
   @Deprecated // Not recommended for production, precision can be lost
   public float getFieldValueAsFloat(String pcParam) {
      return new BigDecimal(stripNonNumbers(this.get(pcParam).toString())).floatValue();
   }
   
   public long getFieldValueAsLong(String pcParam) {
	  return new BigDecimal(stripNonNumbers(this.get(pcParam).toString())).longValueExact();
   }
   
   public Date getFieldValueAsDate(String pcParam ) {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
      try {
        return df.parse(((String) this.get(pcParam)));
     } catch (java.text.ParseException e) {
        return null;
     }
   }
   
   public Date getFieldValueAsDatetime(String pcParam ) {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
      try {
        return df.parse((String) this.get(pcParam));
     } catch (java.text.ParseException e) {
        return null;
     }
   }
   
   public Date getFieldValueAsDatetimeTZ(String pcParam) {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      try {
        return df.parse((String) this.get(pcParam));
     } catch (java.text.ParseException e) {
        return null;
     }
   }
   
   public boolean getFieldValueAsBoolean(String pcParam ) {
      return Boolean.parseBoolean(this.get(pcParam).toString());
   }
   
   private String stripNonNumbers(String str) {
	   return str.replaceAll("[^\\d.-]", "");
   }
}
