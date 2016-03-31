package fi.aalto.cs.drumbeat.rdf.jena.provider.virtuoso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rdf.jena.provider.AbstractJenaProvider;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProviderException;

public class VirtuosoJenaProvider extends AbstractJenaProvider {
	
	public static final long BULK_LOAD_STATUS_WAITING = 0; 
	public static final long BULK_LOAD_STATUS_IN_PROGRESS = 1; 
	public static final long BULK_LOAD_STATUS_COMPLETE = 2; 
	
	private static final Logger logger = Logger.getLogger(VirtuosoJenaProvider.class);
	
	private static Object locker = new Object();
	
	private Map<String, Model> cache = new HashMap<>();

	public VirtuosoJenaProvider(String factoryName, Properties properties, String propertyPrefix) {
		super(factoryName, properties, propertyPrefix);
	}
	
	public VirtuosoJenaProvider(
			String providerName,
			String serverUrl,
			String userName,
			String password,
			String defaultGraphName,
			Properties properties,
			String propertyPrefix) {
		super(providerName, serverUrl, userName, password, defaultGraphName, properties, propertyPrefix);
	}
	
	
	@Override
	public Model openModel(String graphName) throws JenaProviderException {
		
		if (graphName == null) {
			graphName = "";
		}
		
		Model model = cache.get(graphName);
		if (model == null) {
			logger.info(String.format("[Virt] Getting model '%s'", graphName));
			
			try {

				if (getServerUrl() == null) {
					throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_SERVER_URL));
				}
	
				if (getUserName() == null) {
					throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_USER_NAME));
				}
	
				if (getPassword() == null) {
					throw new IllegalArgumentException(String.format("Argument %s is undefined", ARGUMENT_PASSWORD));
				}
	            logger.info("Graphname: "+graphName);
	            logger.info("ServerUrl: "+getServerUrl());
	            logger.info("UserName: "+getUserName());
	            logger.info("Password: "+getPassword());
				model = VirtModel.openDatabaseModel(graphName, getServerUrl(), getUserName(), getPassword());
			} catch (Exception e) {
				throw new JenaProviderException(e);
			}
			
			logger.info(String.format("[Virt] Getting model '%s' completed", graphName));

			cache.put(graphName, model);
		}
		return model;	
		
	}

	@Override
	public void deleteModel(String graphName) throws JenaProviderException {
		
		if (graphName == null) {
			graphName = "";
		}
		
		cache.remove(graphName);
		
		synchronized (locker) {

			logger.info(String.format("[Virt] Removing graph <%s>", graphName));

			try {
				
				Class.forName("virtuoso.jdbc4.Driver");				
				Connection connection = DriverManager.getConnection(getServerUrl(), getUserName(), getPassword());
				
				Statement stmt = connection.createStatement();
				
				//
				// clear status table
				//
				String deleteGraphQuery = "SPARQL DROP SILENT GRAPH <" + graphName + ">";				
				stmt.executeQuery(deleteGraphQuery);
				
			} catch (Exception e) {
				throw new JenaProviderException("Deleting graph error: " + e.getMessage(), e);
			}			
			
		}
		
		
	}

	
	@Override
	public void release() throws JenaProviderException {
		for (Model model : cache.values()) {
			model.close();			
		}
		cache.clear();
	}

	@Override
	public QueryExecution createQueryExecution(String query, Model model) {
		return VirtuosoQueryExecutionFactory.create(query, model);
	}

	@Override
	public QueryExecution createQueryExecution(Query query, Model model) {
		return VirtuosoQueryExecutionFactory.create(query, model);
	}

	@Override
	public boolean supportsBulkLoading() {
		return true;
	}
	
	@Override
	public boolean bulkLoadFile(String filePath, String graphName) throws JenaProviderException {
		
		filePath = filePath.replaceAll("\\\\",  "/");		
		int indexOfSlash = filePath.lastIndexOf('/');
		int loaded = bulkLoadDir(filePath.substring(0, indexOfSlash), filePath.substring(indexOfSlash + 1), graphName);
		return loaded > 0;
		
	}

	@Override
	public int bulkLoadDir(String dirPath, String fileNamePattern, String graphName) throws JenaProviderException {
		
		synchronized (locker) {

			dirPath = dirPath.replaceAll("\\\\",  "/");				
			while (dirPath.endsWith("/")) {
				dirPath = dirPath.substring(0, dirPath.length() - 1);
			}

			logger.info(String.format("[Virt] Loading file '%s/%s' into graph <%s>", dirPath, fileNamePattern, graphName));
			
			Connection connection = null;
			Statement stmt = null;

			try {
				
				Class.forName("virtuoso.jdbc4.Driver");				
				connection = DriverManager.getConnection(getServerUrl(), getUserName(), getPassword());
				
				stmt = connection.createStatement();
				
				//
				// clear status table
				//
				String clearTableQuery = "DELETE FROM DB.DBA.load_list";				
				logger.debug(String.format("[Virt] Query: '%s'", clearTableQuery));
				stmt.executeUpdate(clearTableQuery);
				
				//
				// load files to queue
				//
				String loadDirQuery = String.format(
						"ld_dir('%s', '%s', '%s')",
						dirPath,
						fileNamePattern,
						graphName);
				logger.debug(String.format("[Virt] Query: '%s'", loadDirQuery));				
				stmt.executeQuery(loadDirQuery);
				
				//
				// wait while loader running
				//
				stmt.executeQuery("rdf_loader_run()");				
		
				//
				// clear status table
				//
				String selectStatusQuery = "SELECT ll_file, ll_graph, ll_state, ll_error FROM DB.DBA.load_list";				
				logger.debug(String.format("[Virt] Query: '%s'", selectStatusQuery));
				ResultSet resultSet = stmt.executeQuery(selectStatusQuery);
				
				int fileLoadedCount = 0;
				
				for (; resultSet.next(); ++fileLoadedCount) {
					long status = resultSet.getLong("ll_state");
					
					if (status != BULK_LOAD_STATUS_COMPLETE) {
						throw new JenaProviderException(
								String.format(
									"Bulk load is incompelete: state=%d, file='%s', graph='%s'",
									status,
									resultSet.getString("ll_file"),
									resultSet.getString("ll_graph")));
					}
					
					String error = resultSet.getString("ll_error");
					if (error != null) {
						throw new JenaProviderException(
								String.format(
									"Bulk load error: error=%s, file='%s', graph='%s'",
									error,
									resultSet.getString("ll_file"),
									resultSet.getString("ll_graph")));						
					}
				}
				
				return fileLoadedCount;
				
			} catch (JenaProviderException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Bulk load error: " + e.getMessage(), e);
				throw new JenaProviderException("Bulk load error: " + e.getMessage(), e);
			} finally {
				try {
					if (connection != null) {
						if (stmt != null) {
							stmt.close();
						}
							connection.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			
			
		}
		
	}
	
}
