package fluid.test.data;

import java.util.Map;

public class Record extends DataObject {

   private static final long serialVersionUID = -5050610310398898572L;

   public Record() {
   }
   
   public Record(@SuppressWarnings("rawtypes") Map recordProperties) {
      super(recordProperties);
   }
   
   /**
    * Creates a shallow copy of <code>record</code> containing its FieldValues for properties
    * named in <code>properties</code>.
    *
    * @param record the record to copy FieldValues from.
    * @param properties an array of properties to copy.
    * @return a copy of <code>record</code> containing only the FieldValues for properties
    * named in <code>properties</code>.
    */
   public static Record copyFieldValues(Record record, String... properties) {
       Record copyOfRecord = new Record();
       copyFieldValuesInto(copyOfRecord, record, properties);
       return copyOfRecord;
   }

   /**
    * Shallow copies the FieldValues of <code>record</code> to <code>destRecord</code> for
    * properties named in <code>properties</code>.
    *
    * @param destRecord (out) destination record into which FieldValues are copied.
    * @param record the record to copy FieldValues from.
    * @param properties an array of properties to copy.
    */
   public static void copyFieldValuesInto(Record destRecord, Record record, String... properties) {
       for (String property : properties) {
           if (property == null) continue;
           Object attr = record.getFieldValueAsObject(property);
           destRecord.setFieldValue(property, attr);
       }
   }

}
