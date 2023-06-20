package auto.utils;

import java.sql.Connection;
//import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simplistic way to send queries and receive the results to a database
 * 
 * @author vercin
 */
public class ProgressDatabaseConnection {

	//Driver class name for 10.1x and later = com.ddtek.jdbc.openedge.OpenEdgeDriver
	//Driver class name for 10.0x and lower = com.progress.sql.jdbc.JdbcProgressDriver
	
	public final static String DRIVER = "com.ddtek.jdbc.openedge.OpenEdgeDriver";
	public final static String URL;
	static {
		String portString = System.getProperty("stixPort");
		final int port;
		if (portString != null && !portString.isEmpty()) {
			port = Integer.parseInt(portString); // make sure it converts into at least an int
		} else {
			throw new NullPointerException("System property \"stixPort\" was null or empty.");
		}
		
		URL = "jdbc:datadirect:openedge://localhost:" + port + ";databaseName=stix";
	}
	
	/**
	 * Update a database while supplying your own connection
	 * 
	 * @param con
	 *            Connection for the database
	 * @param updateQuery
	 *            String update query to apply to the database
	 * @throws SQLException
	 */
	public static void update(Connection con, String updateQuery) throws SQLException {
		Statement stmt = con.createStatement();
		
		try {
			stmt.executeUpdate(updateQuery);
		} finally {
			stmt.close();
		}
	}
	
	/**
	 * @param updateQuery
	 *            String update query to apply to the database implemented in
	 *            {@link #getDbConnection()}
	 */
	public static void update(String updateQuery) {
		Connection con = null;
		
		try {
			con = getDbConnection();
			update(con, updateQuery);
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex.getMessage());
			}
		}
	}
	
	/**
	 * @param updateQueries
	 *            List of String update queries to apply to the database implemented
	 *            in {@link #getDbConnection()}
	 */
	public static void update(List<String> updateQueries) {
		
		Connection con = null;
		
		try {
			con = getDbConnection();
			for (String query: updateQueries) {
				update(con, query);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex.getMessage());
			}
		}
	}
	
	/**
	 * Query a database while supplying your own connection.
	 * 
	 * @param con
	 *            A database connection from either {@link #getDbConnection()} or a
	 *            custom one
	 * @param query
	 *            String representation of the query
	 * @return List of Maps, where each map is representing a row with the key for
	 *         each value being the column name
	 * @throws SQLException
	 */
	public static List<Map<String, Object>> query(Connection con, String query) throws SQLException {
		
		List<Map<String, Object>> results = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		Statement stmt = con.createStatement();
		
		try {
			ResultSet rs = stmt.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				while (rs.next()) {
					for (int i = 0; i < rsmd.getColumnCount(); i++) {
						map.put(rsmd.getColumnLabel(1 + i), rs.getObject(1 + i));
					}
					results.add(map);
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		return results;
	}
	
	/**
	 * Queries the database by using the database connection generated in
	 * {@link #getDbConnection()}
	 * 
	 * @param query
	 *            String representation of the query
	 * @return List of Maps, where each map is representing a row with the key for
	 *         each value being the column name
	 */
	public static List<Map<String, Object>> query(String query) {
		
		Connection con = null;
		
		try {
			con = getDbConnection();
			return query(con, query);
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex.getMessage());
			}
		}
	}
	
	/**
	 * Iterates through a list of queries and applies them to the database using the
	 * database connection generated in {@link #getDbConnection()}
	 * 
	 * @param queryList
	 *            String List of queries
	 * @return Map of a List of Maps, where the query is the key with the {@link #query(String) results as the value}.
	 */
	public static Map<String, List<Map<String, Object>>> query(List<String> queryList) {
		
		Map<String, List<Map<String, Object>>> results = new HashMap<>();
		Connection con = null;
		
		try {
			con = getDbConnection();
			for (String query: queryList) {
				results.put(query, query(con, query));
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		} finally {
			try {
				con.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
				throw new RuntimeException(ex.getMessage());
			}
		}
		return results;
	}
	
	/**
	 * @return Connection to stix database
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getDbConnection() throws SQLException {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage());
		}

		return DriverManager.getConnection( URL, "root", "st3r1@nd" );
	}
}
