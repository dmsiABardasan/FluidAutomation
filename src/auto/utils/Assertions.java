package auto.utils;

import static org.junit.Assert.fail;

@FunctionalInterface
public interface Assertions {

	/**
	 * Providing no assertions is a no-op
	 * 
	 * @param assertions
	 */
	public static void assertGroup(Assertions... assertions) {
		try {
			assertGroupSilently(assertions);
		}catch (AssertionError ass){
			System.err.println(ass.getMessage());
			throw ass;
		}
	}
	
	public static void assertGroupSilently(Assertions... assertions) {
		StringBuilder errorMsg = new StringBuilder();
		boolean failed = false;
		
		for (int i = 0 ; i < assertions.length ; i++) {	
			try {
				assertions[i].assertion();
			} catch(AssertionError ass) {
				errorMsg.append("["+i+"] " + ass.getMessage() + "\r\n");
				failed = true;
			}
		}
		
		if (failed) {
			fail(errorMsg.toString());
		}
	}
	
	/**
	 * Group asserts and if an {@link AssertionError} is thrown, will print the
	 * stacktrace with the passed message
	 * 
	 * @param message
	 *            Custom message to provide if an {@link AssertionError} occurs
	 * @param assertions
	 *            varags Assertions
	 */
	public static void assertGroup(String message, Assertions... assertions) {
		try {
			assertGroup(assertions);
		}catch(AssertionError ex) {
			fail(message + "\r\n" + ex.getMessage());
		}
	}
	
	public static void assertGroupWithSeperator(Assertions... assertions) {
		try {
			assertGroup(assertions);
		}finally {
			System.out.println("----------------------------------------------\n");
		}
	}
	
	public void assertion() throws AssertionError;
	
}
