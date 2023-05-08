package fluid.test.message;

import java.util.ArrayList;
import java.util.List;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public class ValidationResponse extends Response {

   /**
    * Constants
    **/
   public static final String JSON_ERROR_TABLE_KEY = "errorTable";
   public static final String JSON_VALID_CONTEXT_ID_KEY = "validContextID";
   public static final String HAS_WARNINGS_OR_INFORMATION_PARAM = "hasWarningsOrInformation";
   public static final String HAS_ERRORS_PARAM = "hasErrors";

   /**
    * Instance fields
    **/
   private List<Error> ttError = new ArrayList<Error>();
   private Boolean validContextID = null;
   private Boolean hasWarningsOrInformationParam = null;
   private Boolean hasErrorsParam = null;

   /**
    * Default Constructor
    **/
   public ValidationResponse() {
      super();
   }

   public ValidationResponse(String pcMessage) throws Throwable {
      super(pcMessage);
      this.validContextID = getParamAsBoolean(JSON_VALID_CONTEXT_ID_KEY);
      this.hasWarningsOrInformationParam = getParamAsBoolean(HAS_WARNINGS_OR_INFORMATION_PARAM);
      this.hasErrorsParam = getParamAsBoolean(HAS_ERRORS_PARAM);
      if (hasParam(JSON_ERROR_TABLE_KEY)) {
         JsonArray jsError = getParamAsJSONArray(JSON_ERROR_TABLE_KEY);
         for (int i = 0; i < jsError.size(); i++) {
            JsonObject jsonError = (JsonObject) jsError.get(i);
            Error error = new Error((String) jsonError.get("field"), (String) jsonError.get("type"), (String) jsonError.get("msg"), (String) jsonError.get("lineid"));
            ttError.add(error);
         }
      } else if (hasParam("_errors")) {
         JsonArray jsError = getParamAsJSONArray("_errors");
         for (int i = 0; i < jsError.size(); i++) {
        	 JsonObject jsonError = (JsonObject) jsError.get(i);
            Error error = new Error("", ErrorTypeEnum.ERROR.getValue(), ((String) jsonError.get("_errorMsg")));
            this.ttError.add(error);
         }
      }
   }

   /**
    * Adds an error to the error list
    **/
   public void addError(String errField, ErrorTypeEnum errType, String errMsg) {
      Error e = new Error(errField, errType.toString(), errMsg);
      ttError.add(e);
   }

   /**
    * @return true when errors exist, false otherwise.
    **/
   public boolean hasErrors() {
      if (ttError == null) {
         return false;
      } else {
         return !ttError.isEmpty();
      }
   }
   
   public boolean getHasErrorsParam() {
	   return (this.hasErrorsParam != null) ? this.hasErrorsParam : false;
   }
   
   public boolean getHasWarningsOrInformationParam() {
	   return (this.hasWarningsOrInformationParam != null) ? this.hasWarningsOrInformationParam : false;
   }

   /**
    * @return the errorList
    */
   public List<Error> getErrorList() {
      return ttError;
   }

   /**
    * @return the validContextID
    */
   public boolean getValidContextID() {
      if (validContextID == null) {
         return false;
      } else {
         return validContextID;
      }
   }
   
   public boolean hasStopErrors() {
      boolean hasStopErrors = false;

      if (ttError != null && !ttError.isEmpty()) {
         for (Error error : ttError) {
            if ((ErrorTypeEnum.ERROR.getValue().equals(error.getType())
                  || ErrorTypeEnum.FATAL.getValue().equals(error.getType())) && !error.getMsg().trim().isEmpty()) {
               hasStopErrors = true;
               break;
            }
         }
      }
      return hasStopErrors;
   }
   
   public boolean hasWarnings() {
      boolean hasWarnings = false;

      if (ttError != null && !ttError.isEmpty()) {
         for (Error error : ttError) {
            if ((ErrorTypeEnum.WARNING.getValue().equals(error.getType())
                  || ErrorTypeEnum.INFORMATION.getValue().equals(error.getType())) && !error.getMsg().trim().isEmpty()) {
               hasWarnings = true;
               break;
            }
         }
      }
      return hasWarnings;
   }
   
   public String getErrorMsg() {
      StringBuilder builder = new StringBuilder();
      List<Error> errorList = getErrorList();
      for (Error error : errorList) {
         if (!error.getMsg().isEmpty()) {
            builder.append(error.getMsg().replaceAll("\n", " "));
            builder.append(" ");
         }
      }
      return builder.toString().trim();
   }
   
   public void removeError(String errField) {
      if (ttError != null && !ttError.isEmpty()) {
         List<Error> toBeRemoved = new ArrayList<Error>();
         for (Error error : ttError) {
            if (errField.equalsIgnoreCase(error.getField())) {
               toBeRemoved.add(error);
            }
         }
         for (Error error : toBeRemoved) {
            ttError.remove(error);
         }
      }
   }
   
   public void removeBlankErrorMessages() {
      if (ttError != null && !ttError.isEmpty()) {
         List<Error> toBeRemoved = new ArrayList<Error>();
         for (Error error : ttError) {
            if (error.getMsg().trim().isEmpty()) {
               toBeRemoved.add(error);
            }
         }
         for (Error error : toBeRemoved) {
            ttError.remove(error);
         }
      }
   }
   
   public void appendErrorSetFrom( ValidationResponse appendFromResponse ) {
      List<Error> errors = appendFromResponse.getErrorList();
      if (errors != null && !errors.isEmpty()) {
         for (Error error : errors) {
           addError(error.getField(), ErrorTypeEnum.valueOf(error.getType()), error.getMsg());
         }
      }
   }

   @Override
   protected void storeProperties() {
      if (ttError != null && !ttError.isEmpty()) {
         JsonArray jsError = new JsonArray();
         jsError.addAll(ttError);
         setParam(JSON_ERROR_TABLE_KEY, jsError);
      }
      if (validContextID != null) {
         setParam(JSON_VALID_CONTEXT_ID_KEY, validContextID);
      }
   }

}
