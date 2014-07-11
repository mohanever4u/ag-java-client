package test.openrdf.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryResult;

import test.AGAbstractTest;

import com.franz.agraph.repository.AGRepository;

public class AGRDFSchemaRepositoryConnectionTest extends AGRepositoryConnectionTest {

	static AGRepository repo = null;
	
	private URI person;
	
	private URI woman;
	
	private URI man;
	
	/*public AGRDFSchemaRepositoryConnectionTest(String name) {
		super(name);
	}*/

	@Override
	protected Repository createRepository() throws Exception {
		return AGAbstractTest.sharedRepository();
	}

	@Override
	public void setUp()
		throws Exception
	{
		super.setUp();

		person = vf.createURI(FOAF_NS + "Person");
		woman = vf.createURI("http://example.org/Woman");
		man = vf.createURI("http://example.org/Man");
	}

    @Test
	public void testDomainInference()
		throws Exception
	{
		testCon.add(name, RDFS.DOMAIN, person);
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, person, true));
	}

    @Test
	public void testSubClassInference()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(man, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

    @Test
	public void testMakeExplicit()
		throws Exception
	{
		testCon.begin();
		testCon.add(woman, RDFS.SUBCLASSOF, person);
		testCon.add(alice, RDF.TYPE, woman);
		testCon.commit();

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));

		testCon.add(alice, RDF.TYPE, person);

		assertTrue(testCon.hasStatement(alice, RDF.TYPE, person, true));
	}

    @Test
	public void testExplicitFlag()
		throws Exception
	{
		RepositoryResult<Statement> result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, true);
		while(result.hasNext())	
		try {			
			assertTrue("result should not be empty", result.hasNext());
		}
		finally {
		result.close();
		}

		result = testCon.getStatements(RDF.TYPE, RDF.TYPE, null, false);
		try {
			assertFalse("result should be empty", result.hasNext());
		}
		finally {
		result.close();
		}
	}

    @Test
	public void testInferencerUpdates()
		throws Exception
	{
		testCon.begin();

		testCon.add(bob, name, nameBob);
		testCon.remove(bob, name, nameBob);

		testCon.commit();

		assertFalse(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

    @Test
	public void testInferencerQueryDuringTransaction()
		throws Exception
	{
		testCon.begin();
		testCon.add(bob, name, nameBob);
		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();
	}

    @Test
	public void testInferencerTransactionIsolation()
		throws Exception
	{
		testCon.begin();
		testCon.add(bob, name, nameBob);

		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertFalse(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));

		testCon.commit();
	
		assertTrue(testCon.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
		assertTrue(testCon2.hasStatement(bob, RDF.TYPE, RDFS.RESOURCE, true));
	}

}
