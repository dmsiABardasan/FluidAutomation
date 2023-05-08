package auto.utils.testRail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

import auto.utils.reporting.TestRailLink;

/**
 * <p>
 * Looks at the current class path and finds any classes annotated with
 * {@link auto.utils.reporting.TestRailLink TestRailLink}, creating a file in the
 * project root that contains entries of pairs representing the class's
 * canonical name and the linking test rail ID provided by the annotation.
 * </p>
 * 
 * <p>
 * The generated file can then be utilised by
 * {@link auto.utils.testRail.TestRailPostResults TestRailPostResults}
 * </p>
 * 
 * @author vercin
 */
public class TestRailCollator {

	public static void main(String[] args) {
		
		URL[] urls = ClasspathUrlFinder.findClassPaths();
		AnnotationDB db = new AnnotationDB();
		try {
			db.scanArchives(urls);
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		Set<String> entityClasses = db.getAnnotationIndex().get(TestRailLink.class.getName());
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("testrail.ini"));
			for (String className : entityClasses) {
				Class<?> clazz = Class.forName(className);
			    writer.append(className + "=" + clazz.getAnnotation(TestRailLink.class).value() + "\r\n");
			}
		}catch (ClassNotFoundException | IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}finally {
			try {
				writer.close();
			} catch (IOException ex) {
				ex.printStackTrace();
				throw new NullPointerException("Writer could not be closed");
			}
		}
		System.out.println("Links found and created: " + entityClasses.size());
	}
	
}
