package fi.aalto.cs.drumbeat.rdf.jena.provider;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.rdf.model.Model;

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
	public abstract QueryExecution createQueryExecution(Query query, Model model);	

}
