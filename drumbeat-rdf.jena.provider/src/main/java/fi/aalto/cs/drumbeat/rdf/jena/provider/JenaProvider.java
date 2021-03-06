package fi.aalto.cs.drumbeat.rdf.jena.provider;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;

public interface JenaProvider {
	
	public String getDefaultGraphName();

	public void setDefaultGraphName(String graphName);
	
	/**
	 * Initialises resources 
	 */	
	public void init() throws JenaProviderException;
	
	/**
	 * Releases resources (connections, cached data and so on) 
	 */	
	public void release() throws JenaProviderException;

	/**
	 * Returns a Jena model instance for the default graph
	 * @param graphName
	 * @return
	 */
	public Model openDefaultModel() throws JenaProviderException;
	
	
	/**
	 * Returns a Jena model instance for the given graph name. If the model already exists, it is simply opened.
	 * @param graphName
	 * @return
	 */
	public Model openModel(String graphName) throws JenaProviderException;
	
	
	/**
	 * Returns a Jena model instance for the given graph name. If the model already exists, it is simply opened.
	 * @param graphName
	 * @return
	 */
	public void deleteModel(String graphName) throws JenaProviderException;
	
	

	/**
	 * Creates a {@link QueryExecution} object using a {@link QueryExecutionFactory}
	 * @param query
	 * @param model
	 * @return
	 */
	public QueryExecution createQueryExecution(String query, Model model);
	
	
	/**
	 * Creates a {@link QueryExecution} object using a {@link QueryExecutionFactory}
	 * @param query
	 * @param model
	 * @return
	 */
	public QueryExecution createQueryExecution(Query query, Model model);
	
	
	
	/**
	 * Returns a flag indicating whethere the jena provider support bulk loading
	 * @return
	 */
	public boolean supportsBulkLoading();	
	
	/**
	 * Loads a file to a graph
	 * @param filePath
	 * @param graphName
	 * @return number of files loaded
	 * @throws JenaProviderException
	 */
	public boolean bulkLoadFile(String filePath, String graphName) throws JenaProviderException;
	
	/**
	 * Loads a file to a graph
	 * @param dirPath
	 * @param fileNamePattern
	 * @param graphName
	 * @return number of files loaded
	 * @throws JenaProviderException
	 */
	public int bulkLoadDir(String dirPath, String fileNamePattern, String graphName) throws JenaProviderException;

}
