package fi.aalto.cs.drumbeat.rdf.jena.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class MemoryJenaProvider extends AbstractJenaProvider {
	
	private Map<String, Model> cache = new HashMap<>();
	
	public MemoryJenaProvider() {
		super(null, null, null);
	}

	public MemoryJenaProvider(String factoryName, Properties properties, String propertyPrefix) {
		super(factoryName, properties, propertyPrefix);
	}

	public MemoryJenaProvider(
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
			model = ModelFactory.createDefaultModel();
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
	}

	@Override
	public void release() throws JenaProviderException {
		cache.clear();
	}

	@Override
	public QueryExecution createQueryExecution(String query, Model model) {
		return QueryExecutionFactory.create(query, model);
	}

	@Override
	public QueryExecution createQueryExecution(Query query, Model model) {
		return QueryExecutionFactory.create(query, model);
	}

	@Override
	public boolean supportsBulkLoading() {
		return false;
	}

	public boolean bulkLoadFile(String filePath, String graphName) throws JenaProviderException {
		throw new UnsupportedOperationException();		
	}
	
	@Override
	public int bulkLoadDir(String dirPath, String fileNamePattern, String graphUri) {
		throw new UnsupportedOperationException();
	}
}
