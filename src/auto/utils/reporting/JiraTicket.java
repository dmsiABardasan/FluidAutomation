package auto.utils.reporting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * A method annotated with this and that follows the proceeding requirements
 * will add a String value representing a JIRA ticket id (ABCD-1234) to the
 * 'Ticket' column on generated JUnit Reports.
 * </p>
 * 
 * <p>
 * The annotated method requires a public static method with parameters for a
 * String and returns a String.
 * </p>
 * 
 * <p>
 * A {@link org.apache.tools.ant.BuildException BuildException} is thrown during
 * the report building stage if a method uses this annotation, but does not
 * fulfill the above requirements.
 * </p>
 * 
 * <p>
 * How this works is that in the ant junitReport step, a
 * {@link auto.utils.reporting.CustomFormatter CustomFormatter} will reflectively
 * look at this class and iterate over the tests that it undertook, passing each
 * testID to the aforementioned implemented method. The method itself can simply
 * return a String to cover all tests undertaken, or use the testID argument to
 * specify individual tests with arbitrary code.
 * </p>
 * 
 * @author vercin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JiraTicket {
	
}
