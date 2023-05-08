package auto.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fluid.test.message.Error;
import fluid.test.message.PresentationMessage;

/**
 * <p>
 * Enum containing different ways that a field can be validated.
 * </p>
 * <ul>
 * <li><strong>WARN_ON_ERROR</strong> - Ignores all errors, but will print them out to the system</li> 
 * <li><strong>IGNORE_ERRORS</strong> - Ignores all errors that return during validation</li>
 * <li><strong>EXPECT_ERRORS</strong> - Expects errors regardless if for the given or another field</li> 
 * <li><strong>VALIDATE_SELF</strong> - Checks for errors only for the given field</li>
 * <li><strong>VALIDATE_ALL</strong> - Checks for all errors returned from validation</li>
 * <li><strong>SELF_EXCLUDED</strong> - Checks for all errors except for the given field</li> 
 * <li><strong>SELF_ERROR</strong> - Expects that an error is attached to the given field</li> 
 * </ul>
 * 
 * @see #validateResponse(ValidationTypeEnum, PresentationMessage, String, String)
 * @author vercin
 */
public enum ValidationTypeEnum {
	/**
	 * Ignores all errors, but will print them out to the system
	 */
	WARN_ON_ERROR,
	/**
	 * Ignores all errors that return during validation
	 */
	IGNORE_ERRORS,
	/**
	 * Expects errors regardless if for the given or another field
	 */
	EXPECT_ERRORS,
	/**
	 * Checks for errors only for the given field
	 */
	VALIDATE_SELF,
	/**
	 * Checks for all errors returned from validation
	 */
	VALIDATE_ALL,
	/**
	 * Checks for all errors except for the given field
	 */
	SELF_EXCLUDED,
	/**
	 * Expects that an error is attached to the given field
	 */
	SELF_ERROR;
	
	
	/**
	 * You are going to need to make sure that the table and field actually exist in
	 * the activity the response is for.
	 * 
	 * @throws AssertionError
	 * @param validationEnum
	 * @param oResponse
	 * @param table
	 * @param field
	 */
	public static void validateResponse(ValidationTypeEnum validationEnum, PresentationMessage oResponse, String table, String field) throws AssertionError {
		
		String tableField = table + "." + field;
		
		switch (validationEnum) {
			case WARN_ON_ERROR:
				if (oResponse.hasStopErrors()) {
					System.err.println("WARNING! Error in response to table <" + table + "> and field <" + field + ">:");
					for(Error error: oResponse.getErrorList()) {
						System.err.println(error.getMsg());
					}
				}
				break;
			case IGNORE_ERRORS:
				// Do nothing
				break;
			case EXPECT_ERRORS:
				assertTrue("No errors returned when expected.", oResponse.hasStopErrors());
				break;
			case VALIDATE_SELF:
				for (Error error : oResponse.getErrorList()) {
					assertFalse("Error returned from validation: " + oResponse.getErrorMsg(),
							tableField.equals(error.getField()));
				}
				break;
			case VALIDATE_ALL:
				assertFalse("Error returned from validation:\n" + oResponse.getErrorMsg(), oResponse.hasStopErrors());
				break;
			case SELF_EXCLUDED:
				for (Error error : oResponse.getErrorList()) {
					assertTrue("Error returned from validation: " + oResponse.getErrorMsg(),
							tableField.equals(error.getField()));
				}
				break;
			case SELF_ERROR:
				{
					boolean hasErrors = false;
					for (Error error : oResponse.getErrorList()) {
						if (tableField.equals(error.getField())) {
							hasErrors = true;
						}
					}
					assertTrue("No error returned when expected.", hasErrors);
				}
				break;
			default:
				throw new IllegalArgumentException("Unhandled validation type");
		}
	}
}
