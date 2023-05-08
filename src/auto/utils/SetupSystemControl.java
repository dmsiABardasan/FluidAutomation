package auto.utils;

import java.util.ArrayList;
import java.util.List;

public class SetupSystemControl {

	public static void main(String[] args) {
		
		String systemName = System.getProperty("systemName");
		
		List<String> systemControlQueries = new ArrayList<String>();
		
		systemControlQueries.add("UPDATE pub.system_control SET path_spool       		= '/prostix/"+systemName+"/spool/'");
		systemControlQueries.add("UPDATE pub.system_control SET path_program     		= '/prostix/"+systemName+"/lib/'");
		systemControlQueries.add("UPDATE pub.system_control SET filename_db      		= '/prostix/data/"+systemName+"/'");
		systemControlQueries.add("UPDATE pub.system_control SET filename_db_archive		= '/tmp/'");
		systemControlQueries.add("UPDATE pub.system_control SET path_data_files_import	= '/prostix/dmp/"+systemName+"/'");
		systemControlQueries.add("UPDATE pub.system_control SET path_export      		= '/prostix/dmp/"+systemName+"/'");
		systemControlQueries.add("UPDATE pub.system_control SET id_printer_system		= 'lp0'");
		
		ProgressDatabaseConnection.update(systemControlQueries);
	}

}
