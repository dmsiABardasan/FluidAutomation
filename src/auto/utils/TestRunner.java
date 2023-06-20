package auto.utils;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import auto.test.template.JunitTemplate;
import fluid.test.adapter.AppServerAdapter;

/**
 * <p>
 * A mere debug test class with a main method to run via CLI, to easier
 * determine working functionality without fussing over JUnit CLI syntax.
 * </p>
 * @author vercin
 */
public class TestRunner {

    public static void main(String[] args) {
    	System.out.println("Start Test");
        Result result = JUnitCore.runClasses(JunitTemplate.class);
        System.out.println("Assertion failure test:");
        for (Failure failure : result.getFailures()) {
            System.out.println(failure.toString());
        }
        System.out.println("AppServerAdapter found? " + AppServerAdapter.getInstance() != null);
        System.out.println("End test");
    }
	

}
