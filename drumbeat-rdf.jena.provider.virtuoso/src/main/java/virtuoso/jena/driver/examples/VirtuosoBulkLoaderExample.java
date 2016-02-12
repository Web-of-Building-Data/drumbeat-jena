package virtuoso.jena.driver.examples;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import fi.aalto.cs.drumbeat.rdf.jena.provider.virtuoso.VirtuosoJenaProvider;

public class VirtuosoBulkLoaderExample {

	public static void main(String[] args) {
		
		try {
			
			VirtuosoJenaProvider provider = new VirtuosoJenaProvider(
					"",
					"jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2",
					"dba",
					"dba",
					null,
					null,
					null);
			
			if (provider.supportsBulkLoading()) {
				File file = new File("c:/PROGRAMS/virtuoso-opensource/vad/sample.ttl");
				System.out.println("Bulk loading");
				provider.bulkLoad(file.getParentFile().getCanonicalPath(), file.getName(), "http://example.org/test/sample-6");
			}
		
//			Class.forName("virtuoso.jdbc4.Driver");
//			
//			Connection connection = DriverManager.getConnection("jdbc:virtuoso://localhost:1111", "dba", "dba");
//			
//			Statement stmt = connection.createStatement();
//			
//			ResultSet resultSet = stmt.executeQuery(
//					"ld_dir ('c:/PROGRAMS/virtuoso-opensource/vad/', 'sample.ttl', 'http://example.org/test/sample-3')");
//
//			resultSet = stmt.executeQuery("select * from DB.DBA.load_list");			
//			
//			resultSet = stmt.executeQuery(
//					"rdf_loader_run()");
			
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

}
