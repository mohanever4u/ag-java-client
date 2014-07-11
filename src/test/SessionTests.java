/******************************************************************************
** Copyright (c) 2008-2014 Franz Inc.
** All rights reserved. This program and the accompanying materials
** are made available under the terms of the Eclipse Public License v1.0
** which accompanies this distribution, and is available at
** http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/

package test;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.repository.RepositoryException;

import com.franz.agraph.http.AGHttpRepoClient;
import com.franz.agraph.http.exception.AGHttpException;
import com.franz.agraph.repository.AGAbstractRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.util.Closer;

import static test.Util.logTimeStamped;

public class SessionTests extends AGAbstractTest {

	@Test
	@Category(TestSuites.Prepush.class)
	public void sessionUsingDedicatedPort() throws Exception {
		int mainPort = getPort(conn);
                String oldUse = System.setProperty("com.franz.agraph.http.useMainPortForSessions", "false");
                String oldOverride = System.setProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions", "true");
		conn.begin();
		int sessionPort = getPort(conn);
		Assert.assertTrue("session port should be different from main port", mainPort!=sessionPort);
		conn.add(OWL.INVERSEOF, OWL.INVERSEOF, OWL.INVERSEOF);
		conn.commit();
        if (oldUse != null) {
            System.setProperty("com.franz.agraph.http.useMainPortForSessions", oldUse);
        } else {
            System.clearProperty("com.franz.agraph.http.useMainPortForSessions");
        }
        if (oldOverride != null) {
            System.setProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions", oldOverride);
        } else {
            System.clearProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions");
        }
	}
	
    @Test
    @Category(TestSuites.Prepush.class)
    public void sessionUsingMainPort() throws Exception {
    	int mainPort = getPort(conn);
    	String oldUse = System.setProperty("com.franz.agraph.http.useMainPortForSessions", "true");
    	String oldOverride = System.setProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions", "true");
    	conn.begin();
    	int sessionPort = getPort(conn);
    	Assert.assertEquals("session port should be the same as main port", mainPort, sessionPort);
    	conn.add(OWL.INVERSEOF, OWL.INVERSEOF, OWL.INVERSEOF);
    	conn.commit();
        if (oldUse != null) {
            System.setProperty("com.franz.agraph.http.useMainPortForSessions", oldUse);
        } else {
            System.clearProperty("com.franz.agraph.http.useMainPortForSessions");
        }
        if (oldOverride != null) {
            System.setProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions", oldOverride);
        } else {
            System.clearProperty("com.franz.agraph.http.overrideServerUseMainPortForSessions");
        }
    }
    
    private int getPort(AGRepositoryConnection conn) throws AGHttpException, MalformedURLException {
    	URL url = new URL(conn.getHttpRepoClient().getRoot());
    	return url.getPort();
    }

    @Test
    @Category(TestSuites.Prepush.class)
    public void sessionLifetime_rfe9436() throws Exception {
	// Seconds
	int conn2Life = 5; 
	/* This is how long past conn2Life we allow for the server to expire the session */
	int timeoutFudge = 2;
	/* The +10 is to allow for delays due to system load.  Don't make it too big, otherwise
	   the test will take unnecessarily long */
	int connLife = conn2Life + timeoutFudge + 10; 

	int conn3Life = 30;
	
	
	AGAbstractRepository repo = conn.getRepository();

	        Assert.assertEquals("expected default session lifetime ", AGHttpRepoClient.getDefaultSessionLifetime(),conn.getSessionLifetime());
		
		conn.setSessionLifetime(connLife);
		Assert.assertEquals("expected lifetime "+connLife, connLife, conn.getSessionLifetime());

		AGRepositoryConnection conn2 = repo.getConnection();
		Assert.assertEquals("expected default session lifetime ", AGHttpRepoClient.getDefaultSessionLifetime(),conn2.getSessionLifetime());
		conn2.setSessionLifetime(conn2Life);
		Assert.assertEquals("expected lifetime "+conn2Life, conn2Life, conn2.getSessionLifetime());
		// Make sure changes to conn2's session lifetime do not affect conn.
		Assert.assertEquals("expected lifetime "+connLife, connLife, conn.getSessionLifetime());

		AGHttpRepoClient.setDefaultSessionLifetime(conn3Life);
		Assert.assertEquals("expected default session lifetime "+conn3Life, conn3Life, AGHttpRepoClient.getDefaultSessionLifetime());
		AGRepositoryConnection conn3 = repo.getConnection();
		Assert.assertEquals("expected session lifetime "+conn3Life, conn3Life, conn3.getSessionLifetime());
		// Make sure the other connection lifetimes are still unaffected
		Assert.assertEquals("expected lifetime "+conn2Life, conn2Life, conn2.getSessionLifetime());
		Assert.assertEquals("expected lifetime "+connLife, connLife, conn.getSessionLifetime());

		logTimeStamped("conn.setAutoCommit(false);");
		conn.begin();
		logTimeStamped("conn2.setAutoCommit(false);");
		conn2.begin();
		
		/* Sleep past conn2's timeout.  We expect it to be expired by the time we wake */
		int sleepTime=conn2Life+timeoutFudge;
		logTimeStamped("Thread.sleep(" + sleepTime*1000 + ")");
		Thread.sleep(sleepTime*1000);
		try {
			conn2.size();
			Assert.fail("expected conn2 session to expire");
		} catch (RepositoryException e) {
		}

		// Reset conn's session timer
		logTimeStamped("conn.ping();");
		conn.ping(); 

		/* Verify that conn's session timer was really reset.
		   Up to this point we've already consumed at least
		   sleepTime (conn2Life+timeoutFudge) seconds of the original
		   timeout, leaving at most connLife-sleepTime seconds remaining. */

		/* This is a maximum.  The actual original timeout remaining could
		   be less due due to system load */
		int orig_timeout_remaining = connLife - sleepTime;
		
		sleepTime = orig_timeout_remaining + timeoutFudge;
		
		/* Sleep past the original timeout.  */
		logTimeStamped("Thread.sleep(" + sleepTime*1000 + ")");
		Thread.sleep(sleepTime*1000);
		
		/* If the prior ping didn't really reset the session timer, this will fail */
		logTimeStamped("conn.size();");
		conn.size(); // fails if ping doesn't work

		// Verify that closing expired conn2's session throws an exception 
		try {
			conn2.close();
			Assert.fail("closing expired conn2 session should throw an exception");
		} catch (RepositoryException e) {
			// TODO: want a more specific exception here?
		}

		conn3.close();
		// conn is closed in test tearDown
    }

}
