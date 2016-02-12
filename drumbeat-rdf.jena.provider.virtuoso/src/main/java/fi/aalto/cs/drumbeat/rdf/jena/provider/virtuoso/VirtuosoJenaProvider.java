package fi.aalto.cs.drumbeat.rdf.jena.provider.virtuoso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

import fi.aalto.cs.drumbeat.rdf.jena.provider.AbstractJenaProvider;
import fi.aalto.cs.drumbeat.rdf.jena.provider.JenaProviderException;

public class VirtuosoJenaProvider extends AbstractJenaProvider {
	
	private static final Logger logger = Logger.getLogger(VirtuosoJenaProvider.class);
	
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
	public void bulkLoad(String dirPath, String fileNamePattern, String graphName) throws JenaProviderException {
		
		try {
			
			Class.forName("virtuoso.jdbc4.Driver");
			
			Connection connection = DriverManager.getConnection(getServerUrl(), getUserName(), getPassword());
			
			Statement stmt = connection.createStatement();
			
			dirPath = dirPath.replaceAll("\\\\",  "/");
			
			logger.info(String.format("[Virt] Loading file '%s/%s' into graph <%s>", dirPath, fileNamePattern, graphName));
			
			stmt.executeQuery(String.format("ld_dir('%s', '%s', '%s')", dirPath, fileNamePattern, graphName));
	
	//			stmt.executeQuery("select * from DB.DBA.load_list");			
			
			stmt.executeQuery("rdf_loader_run()");
			
		} catch (Exception e) {
			throw new JenaProviderException("Bulk loading error: " + e.getMessage(), e);
		}
		
	}
	
}
