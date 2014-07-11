/******************************************************************************
** Copyright (c) 2008-2014 Franz Inc.
** All rights reserved. This program and the accompanying materials
** are made available under the terms of the Eclipse Public License v1.0
** which accompanies this distribution, and is available at
** http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/

package com.franz.agraph.repository.config;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.franz.agraph.repository.AGRepository;

/**
 * Defines constants for the AGRepository schema which is used by
 * {@link AGRepositoryFactory}s to initialize {@link AGRepository}s.
 * 
 */
public class AGRepositorySchema {

	/** The AGRepository schema namespace (<tt>http://franz.com/agraph/repository/config#</tt>). */
	public static final String NAMESPACE = "http://franz.com/agraph/repository/config#";

	/** <tt>http://franz.com/agraph/repository/config#serverUrl</tt> */
	public final static URI SERVERURL;

	/** <tt>http://franz.com/agraph/repository/config#username</tt> */
	public final static URI USERNAME;

	/** <tt>http://franz.com/agraph/repository/config#password</tt> */
	public final static URI PASSWORD;

	/** <tt>http://franz.com/agraph/repository/config#catalogId</tt> */
	public final static URI CATALOGID;
	
	/** <tt>http://franz.com/agraph/repository/config#repositoryId</tt> */
	public final static URI REPOSITORYID;
	
	/** <tt>http://franz.com/agraph/repository/config#session</tt> */
	public final static URI SESSION_TYPE;
	
	/** <tt>http://franz.com/agraph/repository/config#sessionLifetime</tt> */
	public final static URI SESSION_LIFETIME;
	
	/** <tt>http://franz.com/agraph/repository/config#shutdownHook</tt> */
	public final static URI SHUTDOWN_HOOK;
	
	/** <tt>http://franz.com/agraph/repository/config#testOnBorrow</tt> */
	public final static URI TEST_ON_BORROW;
	
	/** <tt>http://franz.com/agraph/repository/config#maxActive</tt> */
	public final static URI POOL_MAX_ACTIVE_CONNECTIONS;
	
	/** <tt>http://franz.com/agraph/repository/config#maxIdle</tt> */
	public final static URI POOL_MAX_IDLE_CONNECTIONS;
	
	/** <tt>http://franz.com/agraph/repository/config#maxWait</tt> */
	 public final static URI POOL_MAX_TIME_TO_WAIT_FOR_CONNECTION;
	
	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		SERVERURL = factory.createURI(NAMESPACE, "serverUrl");
		USERNAME = factory.createURI(NAMESPACE, "username");
		PASSWORD = factory.createURI(NAMESPACE, "password");
		CATALOGID = factory.createURI(NAMESPACE, "catalogId");
		REPOSITORYID = factory.createURI(NAMESPACE, "repositoryId");
		SESSION_TYPE = factory.createURI(NAMESPACE, "session");
		SESSION_LIFETIME = factory.createURI(NAMESPACE, "sessionLifetime");
		SHUTDOWN_HOOK = factory.createURI(NAMESPACE, "shutdownHook");
		TEST_ON_BORROW = factory.createURI(NAMESPACE, "testOnBorrow");
		POOL_MAX_ACTIVE_CONNECTIONS = factory.createURI(NAMESPACE, "maxActive");
		POOL_MAX_IDLE_CONNECTIONS = factory.createURI(NAMESPACE, "maxIdle");
		POOL_MAX_TIME_TO_WAIT_FOR_CONNECTION = factory.createURI(NAMESPACE, "maxWait");
	}
}
