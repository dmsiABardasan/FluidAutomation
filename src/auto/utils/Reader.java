package auto.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;

public class Reader {
	
	public static final String USER_DIRECTORY = System.getProperty("user.dir");
	public static final Charset CHAR_SET = StandardCharsets.UTF_8;
	
	public static Iterable<Object[]> inputData(String inputDataDirectory) {
		String csvFile = USER_DIRECTORY + inputDataDirectory;
		return csvReader(csvFile);
	}
	
	public static List<Object[]> csvReader(String csvLocation){
		CSVReader reader;
		List<Object[]> data = new ArrayList<Object[]>();
		
		try {
			reader = new CSVReader(new FileReader(csvLocation));
			Object[] line;
			while ((line = reader.readNext()) != null) {
				data.add(line);
			}
		} catch (FileNotFoundException ex) {
			throw new IllegalArgumentException(ex);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return data;
	}
	
	public static String readFile(String path) {
		byte[] encoded = null;
		
		try {
			encoded = Files.readAllBytes(Paths.get(USER_DIRECTORY + path));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return new String(encoded, CHAR_SET);
	}
	
	public static List<String> bufferedReader(String path) {
		List<String> lines = new ArrayList<String>();
		try {
			final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(USER_DIRECTORY + path), CHAR_SET));
			String line;
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
	
}
