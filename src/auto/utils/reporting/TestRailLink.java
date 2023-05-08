package auto.utils.reporting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applying this annotation to a JUnit test class will allow the passed value to
 * appear on the 'Test Rail' column in the JUnit report.
 * 
 * Additionally, if {@link auto.utils.testRail.TestRailCollator} is run, it will
 * collect all classes annotated with this and create a reference file.
 * 
 * @author vercin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestRailLink {
	/**
	 * @return int representing the test case id number for the appropriate test on
	 *         TestRail.
	 */
	public int value();
}
