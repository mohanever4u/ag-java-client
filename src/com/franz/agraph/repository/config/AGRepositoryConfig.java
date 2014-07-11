/******************************************************************************
** Copyright (c) 2008-2014 Franz Inc.
** All rights reserved. This program and the accompanying materials
** are made available under the terms of the Eclipse Public License v1.0
** which accompanies this distribution, and is available at
** http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/

package com.franz.agraph.repository.config;

import static com.franz.agraph.repository.config.AGRepositorySchema.CATALOGID;
import static com.franz.agraph.repository.config.AGRepositorySchema.*;
import static com.franz.agraph.repository.config.AGRepositorySchema.PASSWORD;
import static com.franz.agraph.repository.config.AGRepositorySchema.REPOSITORYID;
import static com.franz.agraph.repository.config.AGRepositorySchema.SERVERURL;
import static com.franz.agraph.repository.config.AGRepositorySchema.USERNAME;

import java.util.concurrent.TimeUnit;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryImplConfigBase;

import com.franz.agraph.pool.AGConnProp.Session;

/**
 * Configuration for an AllegroGraph Repository.
 * 
 */
public class AGRepositoryConfig extends RepositoryImplConfigBase {

	private String serverUrl;
	private String username;
	private String password;
	private String catalogId;
	private String repositoryId;
	private Session session = com.franz.agraph.pool.AGConnProp.Session.DEDICATED;
	private long sessionLifetime = TimeUnit.MINUTES.toSeconds(1);
	private boolean shutdownHook = false;
	private boolean testOnBorrow = true;
	private long maxActive = 10;
	private long maxIdle = 0;
	private long maxWait = TimeUnit.SECONDS.toMillis(30);

	public AGRepositoryConfig() {
		super(AGRepositoryFactory.REPOSITORY_TYPE);
	}

	public AGRepositoryConfig(String url) {
		this();
		setServerUrl(url);
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String url) {
		this.serverUrl = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCatalogId() {
		return catalogId;
	}
	
	public void setCatalogId(String catalogId) {
		this.catalogId = catalogId;
	}
	
	public String getRepositoryId() {
		return repositoryId;
	}
	
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
	
	public Session getSession() {
		 		return session;
	}

	public void setSession(Session session) {
			this.session = session;
	}
	
	public long getSessionLifetime() {
			return sessionLifetime;
	}
	
	public void setSessionLifetime(long sessionLifetime) {
			this.sessionLifetime = sessionLifetime;
	}

	public boolean isShutdownHook() {
		return shutdownHook;
	}

	public void setShutdownHook(boolean shutdownHook) {
		 this.shutdownHook = shutdownHook;
	}

	public boolean isTestOnBorrow() {
		return testOnBorrow;
	}
	
	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}
	
	public long getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(long maxActive) {
		this.maxActive = maxActive;
	}

	public long getMaxIdle() {
		return maxIdle;
	}
	
	public void setMaxIdle(long maxIdle) {
		this.maxIdle = maxIdle;
	}
	
	public long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}
		 
	@Override
	public void validate()
		throws RepositoryConfigException
	{
		super.validate();
		if (serverUrl == null) {
			throw new RepositoryConfigException("No serverUrl specified for AG repositoryId");
		}
		if (username == null) {
			throw new RepositoryConfigException("No username specified for AG repositoryId");
		}
		if (password == null) {
			throw new RepositoryConfigException("No password specified for AG repositoryId");
		}
		if (catalogId == null) {
			throw new RepositoryConfigException("No catalogId specified for AG repositoryId");
		}
		if (repositoryId == null) {
			throw new RepositoryConfigException("No repositoryId specified for AG repositoryId");
		}
	}

	@Override
	public Resource export(Graph graph) {
		Resource implNode = super.export(graph);

		if (serverUrl != null) {
			graph.add(implNode, SERVERURL, getValueFactory().createURI(serverUrl));
		}
		if (username != null) {
			graph.add(implNode, USERNAME, getValueFactory().createLiteral(username));
		}
		if (password != null) {
			graph.add(implNode, PASSWORD, getValueFactory().createLiteral(password));
		}
		if (catalogId != null) {
			graph.add(implNode, CATALOGID, getValueFactory().createLiteral(catalogId));
		}
		if (repositoryId != null) {
			graph.add(implNode, REPOSITORYID, getValueFactory().createLiteral(repositoryId));
		}
		if (maxIdle > 0) {
				graph.add(implNode, SESSION_TYPE, getValueFactory().createLiteral(session.name()));
				graph.add(implNode, SESSION_LIFETIME, getValueFactory().createLiteral(sessionLifetime));
				graph.add(implNode, SHUTDOWN_HOOK, getValueFactory().createLiteral(shutdownHook));
				graph.add(implNode, TEST_ON_BORROW, getValueFactory().createLiteral(testOnBorrow));
				graph.add(implNode, POOL_MAX_ACTIVE_CONNECTIONS, getValueFactory().createLiteral(maxActive));
				graph.add(implNode, POOL_MAX_IDLE_CONNECTIONS, getValueFactory().createLiteral(maxIdle));
				graph.add(implNode, POOL_MAX_TIME_TO_WAIT_FOR_CONNECTION, getValueFactory().createLiteral(maxWait));
		}
			  

		return implNode;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
		throws RepositoryConfigException
	{
		super.parse(graph, implNode);

		try {
			URI uri = GraphUtil.getOptionalObjectURI(graph, implNode, SERVERURL);
			if (uri != null) {
				setServerUrl(uri.toString());
			}
			Literal username = GraphUtil.getOptionalObjectLiteral(graph, implNode, USERNAME);
			if (username != null) {
				setUsername(username.getLabel());
			}
			Literal password = GraphUtil.getOptionalObjectLiteral(graph, implNode, PASSWORD);
			if (password != null) {
				setPassword(password.getLabel());
			}
			Literal catalogId = GraphUtil.getOptionalObjectLiteral(graph, implNode, CATALOGID);
			if (catalogId != null) {
				setCatalogId(catalogId.getLabel());
			}
			Literal repositoryId = GraphUtil.getOptionalObjectLiteral(graph, implNode, REPOSITORYID);
			if (repositoryId != null) {
				setRepositoryId(repositoryId.getLabel());
			}
			Literal session = GraphUtil.getOptionalObjectLiteral(graph, implNode, SESSION_TYPE);
			if (session != null) {
					setSession(Session.valueOf(session.getLabel()));
			}
			Literal sessionLifetime = GraphUtil.getOptionalObjectLiteral(graph, implNode, SESSION_LIFETIME);
			if (sessionLifetime != null) {
					setSessionLifetime(sessionLifetime.longValue());
			}
			Literal shutdownHook = GraphUtil.getOptionalObjectLiteral(graph, implNode, SHUTDOWN_HOOK);
			if (shutdownHook != null) {
					setShutdownHook(shutdownHook.booleanValue());
			}
			Literal testOnBorrow = GraphUtil.getOptionalObjectLiteral(graph, implNode, TEST_ON_BORROW);
			if (testOnBorrow != null) {
					setTestOnBorrow(testOnBorrow.booleanValue());
			}
			Literal maxActive = GraphUtil.getOptionalObjectLiteral(graph, implNode, POOL_MAX_ACTIVE_CONNECTIONS);
			if (maxActive != null) {
					setMaxActive(maxActive.longValue());
			}
			Literal maxIdle = GraphUtil.getOptionalObjectLiteral(graph, implNode, POOL_MAX_IDLE_CONNECTIONS);
			if (maxIdle != null) {
					setMaxIdle(maxIdle.longValue());
			}
			Literal maxWait = GraphUtil.getOptionalObjectLiteral(graph, implNode, POOL_MAX_TIME_TO_WAIT_FOR_CONNECTION);
			if (maxWait != null) {
					setMaxWait(maxWait.longValue());
			}
			
		}
		catch (GraphUtilException e) {
			throw new RepositoryConfigException(e.getMessage(), e);
		}
	}
	
	private ValueFactory getValueFactory() {
		 	return ValueFactoryImpl.getInstance();
	}
}
