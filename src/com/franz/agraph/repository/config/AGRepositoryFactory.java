/******************************************************************************
** Copyright (c) 2008-2014 Franz Inc.
** All rights reserved. This program and the accompanying materials
** are made available under the terms of the Eclipse Public License v1.0
** which accompanies this distribution, and is available at
** http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/

package com.franz.agraph.repository.config;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.config.RepositoryFactory;
import org.openrdf.repository.config.RepositoryImplConfig;

import com.franz.agraph.pool.AGConnPool;
import com.franz.agraph.pool.AGConnProp;
import com.franz.agraph.pool.AGPoolProp;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGServer;

/**
 * A {@link RepositoryFactory} that creates {@link AGRepository}s based on
 * RDF configuration data.
 * 
 */
public class AGRepositoryFactory implements RepositoryFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see RepositoryFactory#getRepositoryType()
	 */
	public static final String REPOSITORY_TYPE = "allegrograph:AGRepository";

	/**
	 * Returns the repository's type: <tt>allegrograph:AGRepository</tt>.
	 */
	public String getRepositoryType() {
		return REPOSITORY_TYPE;
	}

	public AGRepositoryConfig getConfig() {
		return new AGRepositoryConfig();
	}

	public Repository getRepository(RepositoryImplConfig config)
		throws RepositoryConfigException
	{
		AGRepository result = null;
		if (config instanceof AGRepositoryConfig) {
			AGRepositoryConfig agconfig = (AGRepositoryConfig)config;
			AGServer server = new AGServer(agconfig.getServerUrl(),agconfig.getUsername(), agconfig.getPassword());
			try {
				result = server.createCatalog(agconfig.getCatalogId()).createRepository(agconfig.getRepositoryId());
			} catch (RepositoryException e) {
				throw new RepositoryConfigException(e);
			}
			if (agconfig.getMaxIdle() > 0) {
				 	try {
				 			result.setConnPool(AGConnPool.create(
				 							AGConnProp.serverUrl, agconfig.getServerUrl(),
				 							AGConnProp.username, agconfig.getUsername(),
				 							AGConnProp.password, agconfig.getPassword(),
				 							AGConnProp.catalog, agconfig.getCatalogId(),
				 							AGConnProp.repository, agconfig.getRepositoryId(),
				 							AGConnProp.session, agconfig.getSession(),
				 							AGConnProp.sessionLifetime, agconfig.getSessionLifetime(),
				 							AGPoolProp.shutdownHook, agconfig.isShutdownHook(),
				 							AGPoolProp.testOnBorrow, agconfig.isTestOnBorrow(),
				 							AGPoolProp.maxActive, agconfig.getMaxActive(),
				 							AGPoolProp.maxIdle, agconfig.getMaxIdle(),
				 							AGPoolProp.maxWait, agconfig.getMaxWait()));
				 				} catch (RepositoryException e) {
				 					throw new RepositoryConfigException(e);
				 				}
				 			}
		}
		else {
			throw new RepositoryConfigException("Invalid configuration class: " + config.getClass());
		}
		return result;
	}
}
