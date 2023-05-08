package auto.utils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fluid.test.json.JsonArray;
import fluid.test.json.JsonObject;

public final class ApiUtils {
	
	private ApiUtils() {}
	
	public static boolean jsonCompare(Object expected, Object actual){
		return jsonCompare(expected, actual, Collections.emptyList());
	}
	
	public static boolean jsonCompare(Object expected, Object actual, Collection<String> exceptions) {
		return jsonCompare(expected, actual, exceptions, true);
	}
	
	public static boolean jsonCompare(Object expected, Object actual, boolean strict){
		return jsonCompare(expected, actual, Collections.emptyList(), strict);
	}
	
	public static boolean jsonCompare(Object expected, Object actual, Collection<String> exceptions, boolean strict) {
		StringBuilder errOutput = new StringBuilder();
		
		boolean result = jsonCompare(expected, actual, "", exceptions, errOutput, strict);
		
		if (errOutput != null && !errOutput.toString().isEmpty()) {
			System.err.println(errOutput.toString());
		}
		assertTrue(errOutput.toString(), result);
		
		return result;
	}
	
	private static boolean jsonCompare(Object expected, Object actual, String xpath, final Collection<String> exceptions, final StringBuilder errOutput, boolean strict) {
	    boolean isEqual = true;
		
	    if (expected instanceof JsonObject && actual instanceof JsonObject) {
	    	JsonObject jsonObj1 = new JsonObject((JsonObject) expected);
	    	JsonObject jsonObj2 = new JsonObject((JsonObject) actual);
	    	jsonObj1.keySet().removeAll(exceptions);
	    	jsonObj2.keySet().removeAll(exceptions);
	    	if (!jsonObj1.keySet().equals(jsonObj2.keySet())) {
	    		Set<String> exclusions = new HashSet<String>();
	    		for (String key : jsonObj1.keySet()) {
	    			if (key.contains(":")) {  // Check for new pasoe markers, todo maybe add dynamic exclusion argument for this method
	    				exclusions.add(key);
	    			}
	    		}
	    		for (String key : jsonObj2.keySet()) {
	    			if (key.contains(":")) {
	    				exclusions.add(key);
	    			}
	    		}
	    		if (!exclusions.isEmpty()) {
	    			jsonObj1.keySet().removeAll(exclusions);
	    			jsonObj2.keySet().removeAll(exclusions);
	            	if (!jsonCompare(jsonObj1, jsonObj2, xpath, exceptions, errOutput, strict)) {
	            		isEqual = false;
	            	}
	    		} else {
	    			Set<String> keys1 = new HashSet<>(jsonObj1.keySet());
	    			Set<String> keys2 = new HashSet<>(jsonObj2.keySet());
	    			
	    			keys1.removeAll(jsonObj2.keySet());
	    			keys2.removeAll(jsonObj1.keySet());
	    			
	    			String sizeError;
	    			if (jsonObj1.size() != jsonObj2.size()) {
	    				sizeError = " (KEY SIZE(act/exp) " + jsonObj2.size() + " != " + jsonObj1.size();
	    			} else {
	    				sizeError = " (KEY MISMATCH";
	    			}
	    			
		    		xpath += sizeError + ", Added: " + keys2 + " Removed: " + keys1 + ")";
		    		isEqual = false;
		    		errOutput.append(xpath + "\r\n");
	    		}
	    	} else {
		        for (String key : jsonObj1.keySet()) {
		        	//if (!exceptions.contains(key)) {
			            Object value1 = jsonObj1.get(key);
			            Object value2 = jsonObj2.get(key);
		            	if (!jsonCompare(value1, value2, xpath + " -> " + key, exceptions, errOutput, strict)) {
		            		isEqual = false;
		            	}
		        	//}
		        }
	    	}
	    } else if (expected instanceof JsonArray && actual instanceof JsonArray) {
	    	JsonArray jsonArr1 = (JsonArray) expected;
	    	JsonArray jsonArr2 = (JsonArray) actual;
			if (jsonArr1.size() != jsonArr2.size()) {
				xpath += " (ARRAY SIZE(act/exp) " + jsonArr2.size() + " != " + jsonArr1.size() + ")";
				isEqual = false;
				errOutput.append(xpath + "\r\n");
		    } else {
				for (int i = 0 ; i < jsonArr1.size() ; i ++) {
					Object value1 = jsonArr1.get(i);
					Object value2 = jsonArr2.get(i);
	            	if (!jsonCompare(value1, value2, xpath + " -> [" + i + "]", exceptions, errOutput, strict)) {
	            		isEqual = false;
	            	}
				}
		    }
	    } else {
	    	if (strict) { // Strict object implementation equality
		    	if (!Objects.equals(expected, actual)) {
	        		isEqual = false;
	        	}
	    	} else { // Handle 'truthy' equality types
		    	if (expected instanceof BigDecimal || actual instanceof BigDecimal) {
		    		try {
				    	BigDecimal bd1 = new BigDecimal(String.valueOf(expected));
				    	BigDecimal bd2 = new BigDecimal(String.valueOf(actual));
			    		if (bd1.compareTo(bd2) != 0) {
			    			isEqual = false;
			    		}
		    		} catch (NumberFormatException ex) {
		    			isEqual = false;
		    		}
		    	} else if (!Objects.equals(expected, actual)) {
		    		isEqual = false;
	        	}
	    	}
	    	
	    	if (!isEqual) {
		    	if (!Objects.equals(expected, actual)) {
		    		String expVal = expected instanceof String ? "\"" + expected + "\"" : String.valueOf(expected);
		    		String actVal = actual instanceof String ? "\"" + actual + "\"" : String.valueOf(actual);
		    		xpath += ":" + actVal + ", (" + expVal +")";
	        		//isEqual = false;
	        		errOutput.append(xpath + "\r\n");
	        	}
	    	}
	    }
	    
	    return isEqual;
	}
	
	public static void replace(StringBuilder strBldr, String replace, String replacement) {
		int start;
		int from = 0;
		while ((start = strBldr.indexOf(replace, from)) >= 0) {
			int end = start + replace.length();
			strBldr.replace(start, end, replacement);
			from = start + replacement.length();
		}
	}
	
	public static String pad(String input, char pad, int length) {
		String output;
		if (length >= input.length()) {
			output = input;
		}else {
			StringBuilder strBldr = new StringBuilder();
			for (int i = 0; i < length; i++) {
				strBldr.append(' ');
			}
			output = strBldr.substring(input.length()) + input;
		}
		return output;
	}
	
	public static String[] prettyPrintStrings(final String[] toFormat, final String aligningString) {
		final String[] outFormat = toFormat.clone();
		
		if (outFormat.length != 0) {
			for (int i = 0 ; i < StringUtils.countMatches(outFormat[0], aligningString) ; i++) {
				
				int longest = 0;
				
				for(int x = 0 ; x < outFormat.length ; x++) {
					String bar = outFormat[x].split(aligningString)[i];
					if (bar.length() > longest) {
						longest = bar.length();
					}
				}
				
				for (int x = 0 ; x < outFormat.length ; x++) {
					String bar = outFormat[x].split(aligningString)[i];
					
					int whitespace = longest - bar.length();
					
					String newBar = bar + StringUtils.repeat(" ", whitespace);
					
					StringBuilder strBldr = new StringBuilder();
					for (int y = 0 ; y < i  ; y++) {
						strBldr.append(outFormat[x].split(aligningString)[y] + aligningString);
					}
					outFormat[x] = outFormat[x].replace(strBldr + bar, strBldr + newBar);
				}
			}
		}
		
		return outFormat;
	}
	
	public static void writeToFile(File file, String[] formatted) {
		file.getParentFile().mkdirs();
		
		FileWriter fr = null; 
		BufferedWriter br = null;
		try {
			fr = new FileWriter(file, false);
			br = new BufferedWriter(fr);
			
			for (String foo : formatted) {
				fr.write(foo + "\r\n");
			}
			
			br.close();
			fr.close();
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	
	private static final String ALPHA_NUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	public static String randomAlphaNumeric(int length){
		return randomSequence(ALPHA_NUMERIC, length);
	}
	
	public static String randomSequence(String criteria, int length) {
		StringBuilder strBldr = new StringBuilder(length);
		for(int i = 0; i < length; i++) {
			strBldr.append(criteria.charAt(SECURE_RANDOM.nextInt(criteria.length())));
		}
		return strBldr.toString();
	}
}
