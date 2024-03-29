/******************************************************************************
** Copyright (c) 2008-2014 Franz Inc.
** All rights reserved. This program and the accompanying materials
** are made available under the terms of the Eclipse Public License v1.0
** which accompanies this distribution, and is available at
** http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/

package com.franz.agraph.jena;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UpdateExecutionException;
import com.franz.agraph.repository.AGBooleanQuery;
import com.franz.agraph.repository.AGGraphQuery;
import com.franz.agraph.repository.AGTupleQuery;
import com.franz.agraph.repository.AGUpdate;
import com.franz.util.Closeable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;

/**
 * Implements the Jena QueryExecution interface for AllegroGraph.
 * 
 */
public class AGQueryExecution implements QueryExecution, Closeable {
	
	private final AGQuery query;
	private final AGModel model;
	private QuerySolution binding;	
	private static final long   TIMEOUT_UNSET = -1 ;
	protected long timeout = TIMEOUT_UNSET;
		
	public AGQueryExecution(AGQuery query, AGModel model) {
		this.query = query;
		this.model = model;		
	}

	
	@Override
	public void abort() {
		throw new UnsupportedOperationException(AGUnsupportedOperation.message);
	}

	@Override
	public void close() {
	}

	@Override
	public boolean execAsk() {
		if (query.getLanguage()!=QueryLanguage.SPARQL) {
			throw new UnsupportedOperationException(query.getLanguage().getName() + " language does not support ASK queries.");
		}
		AGBooleanQuery bq = model.getGraph().getConnection().prepareBooleanQuery(query.getLanguage(), query.getQueryString());
		bq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		bq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		bq.setCheckVariables(query.isCheckVariables());
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				bq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		boolean result;
		try {
			bq.setDataset(model.getGraph().getDataset());
			// New Code Added by prasady@ls
			
			if(timeout > 0)
				bq.setMaxQueryTime((int) (timeout/1000));
			
			//Code Added END = prasady@ls
			result = bq.evaluate();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		return result;
	}

	@Override
	public Model execConstruct() {
		return execConstruct(null);
	}

	@Override
	public Model execConstruct(Model m) {
		if (query.getLanguage()!=QueryLanguage.SPARQL) {
			throw new UnsupportedOperationException(query.getLanguage().getName() + " language does not support CONSTRUCT queries.");
		}
		AGGraphQuery gq = model.getGraph().getConnection().prepareGraphQuery(query.getLanguage(), query.getQueryString());
		gq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		gq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		gq.setCheckVariables(query.isCheckVariables());
		gq.setLimit(query.getLimit());
		gq.setOffset(query.getOffset());
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				gq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		GraphQueryResult result;
		try {
			gq.setDataset(model.getGraph().getDataset());
			//New Code Added by prasady@ls
			
			if(timeout > 0)
				gq.setMaxQueryTime((int) (timeout/1000));
			
			//Code Added END = prasady@ls 
			result = gq.evaluate();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		if (m==null) {
			m = ModelFactory.createDefaultModel();
		}
		try {
			m.setNsPrefixes(result.getNamespaces());			
			while (result.hasNext()) {				
				m.add(model.asStatement(AGNodeFactory.asTriple(result.next())));
			}
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		return m;
	}

	@Override
	public Model execDescribe() {
		return execDescribe(null);
	}

	@Override
	public Model execDescribe(Model m) {
		return execConstruct(m);
	}

	@Override
	public ResultSet execSelect() {
		AGTupleQuery tq = model.getGraph().getConnection().prepareTupleQuery(query.getLanguage(), query.getQueryString());
		tq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		tq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		tq.setCheckVariables(query.isCheckVariables());
		tq.setLimit(query.getLimit());
		tq.setOffset(query.getOffset());		
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				tq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		TupleQueryResult result;
		try {
			tq.setDataset(model.getGraph().getDataset());
			//New Code Added by prasady@ls
			
			if(timeout > 0)
				tq.setMaxQueryTime((int) (timeout/1000));
			
			//Code Added END = prasady@ls
			result = tq.evaluate();
		}		
		catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		return new AGResultSet(result, model);
	}

	/**
	 * Executes SPARQL Update.
	 * <p>
	 * Executes as a <a href="http://www.w3.org/TR/sparql11-update/">SPARQL Update</a> 
	 * to modify the model/repository.
	 */
	public void execUpdate() {
		AGUpdate u = model.getGraph().getConnection().prepareUpdate(query.getLanguage(), query.getQueryString());
		u.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		u.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		u.setCheckVariables(query.isCheckVariables());
		u.setLimit(query.getLimit());
		u.setOffset(query.getOffset());
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				u.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		try {
			u.setDataset(model.getGraph().getDataset());
			//New Code Added by prasady@ls
			
			if(timeout > 0)
				u.setMaxQueryTime((int) (timeout/1000));
			
			//Code Added END = prasady@ls
			u.execute();
		} catch (UpdateExecutionException e) {
			throw new QueryException(e);
		}
	}
	
	public long countSelect() {
		AGTupleQuery tq = model.getGraph().getConnection().prepareTupleQuery(query.getLanguage(), query.getQueryString());
		tq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		tq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		tq.setCheckVariables(query.isCheckVariables());
		tq.setLimit(query.getLimit());
		tq.setOffset(query.getOffset());
		tq.setDataset(model.getGraph().getDataset());
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				tq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		long count;
		try {
			count = tq.count();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		return count;
	}
	
	public long countConstruct() {
		if (query.getLanguage()!=QueryLanguage.SPARQL) {
			throw new UnsupportedOperationException(query.getLanguage().getName() + " language does not support CONSTRUCT queries.");
		}
		AGGraphQuery gq = model.getGraph().getConnection().prepareGraphQuery(query.getLanguage(), query.getQueryString());
		gq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		gq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		gq.setCheckVariables(query.isCheckVariables());
		gq.setLimit(query.getLimit());
		gq.setOffset(query.getOffset());
		gq.setDataset(model.getGraph().getDataset());
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				gq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}
		long count;
		try {
			count = gq.count();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}
		return count;
	}
	
	@Override
	public Context getContext() {
		throw new UnsupportedOperationException(AGUnsupportedOperation.message);
	}

	@Override
	public Dataset getDataset() {
		throw new UnsupportedOperationException(AGUnsupportedOperation.message);
	}

	@Override
	public void setFileManager(FileManager fm) {
		throw new UnsupportedOperationException(AGUnsupportedOperation.message);
	}

	@Override
	public void setInitialBinding(QuerySolution binding) {
		binding = this.binding;
	}


	@Override
	public Iterator<Triple> execConstructTriples() {			
		// New Code Added by prasady@ls
		Iterator<Triple> it = null;		
		if (query.getLanguage()!=QueryLanguage.SPARQL) {
			throw new UnsupportedOperationException(query.getLanguage().getName() + " language does not support CONSTRUCT queries.");
		}		
		AGGraphQuery gq = model.getGraph().getConnection().prepareGraphQuery(query.getLanguage(), query.getQueryString());
		gq.setIncludeInferred(model.getGraph() instanceof AGInfGraph);
		gq.setEntailmentRegime(model.getGraph().getEntailmentRegime());
		gq.setCheckVariables(query.isCheckVariables());
		gq.setLimit(query.getLimit());
		gq.setOffset(query.getOffset());		
		if (binding!=null) {
			Iterator<String> vars = binding.varNames();
			while (vars.hasNext()) {
				String var = vars.next();
				gq.setBinding(var, model.getGraph().vf.asValue(binding.get(var).asNode()));
			}
		}		
		GraphQueryResult result;		
		try {
			gq.setDataset(model.getGraph().getDataset());			
			// New Code Added by prasady@ls			
			if(timeout > 0)
				gq.setMaxQueryTime((int) (timeout/1000));			
			//Code Added END = prasady@ls			
			result = gq.evaluate();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}		
		try {
			List<Triple> tripleArrayList = new ArrayList<Triple>();			
	        while (result.hasNext()) {	        	
				tripleArrayList.add(AGNodeFactory.asTriple(result.next()));				
			}
			//Getting Iterator from List
			it = tripleArrayList.iterator();
		} catch (QueryEvaluationException e) {
			throw new QueryException(e);
		}		
		return it;
		// Code Added END = prasady@ls
	}


	@Override
	public Iterator<Triple> execDescribeTriples() {
		// New Code Added by prasady@ls		
		return execConstructTriples();		
		// Code Added END = prasady@ls
	}


	@Override
	public Query getQuery() {		
		Map<String, String> modelPrefixMap = model.getNsPrefixMap();
		String CompletePrefixString = "";
		Set<String> keySet = modelPrefixMap.keySet();
		Iterator<String> keySetIterator = keySet.iterator();
		while (keySetIterator.hasNext()) {
		   String key = keySetIterator.next();		   
		   CompletePrefixString+=" PREFIX "+key+":<"+modelPrefixMap.get(key)+">";		   
		}		
		Query queryObj = QueryFactory.create(CompletePrefixString+" "+query.getQueryString());			
		return queryObj; 
	}


	@Override
	public void setTimeout(long timeout) {			
		// New Code Added by prasady@ls		
		this.timeout = timeout;		
		// Code Added END = prasady@ls
	}


	@Override
	public void setTimeout(long arg0, TimeUnit arg1) {
		this.timeout = arg1.toMillis(arg0);
		
	}


	@Override
	public void setTimeout(long arg0, long arg1) {
		setTimeout(arg0, TimeUnit.MILLISECONDS, arg1, TimeUnit.MILLISECONDS) ;		

	}


	@Override
	public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2, TimeUnit timeUnit2) {		
		this.timeout = asMillis(timeout1, timeUnit1) ;	}
	
	private long asMillis(long duration, TimeUnit timeUnit)
    {
        return (duration < 0 ) ? duration : timeUnit.toMillis(duration) ;
    }


	@Override
	public long getTimeout1() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public long getTimeout2() {
		// TODO Auto-generated method stub
		return 0;
	}
	

    
	
}
