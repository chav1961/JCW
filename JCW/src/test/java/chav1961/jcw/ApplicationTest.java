package chav1961.jcw;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.jcw.Application;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class ApplicationTest {
	@Test
	public void testParseParameters() throws CommandLineParametersException {
		Application.Parameters	p = Application.parseParameters(new String[0]), pInitial = new Application.Parameters();
		
		Assert.assertEquals(p,pInitial);
		
		p = Application.parseParameters(new String[]{"-conf","./src/test/resources/chav1961/jcw/testconfig.properties"});
		
		Assert.assertTrue(p.refresh);
		Assert.assertFalse(p.inMemory);
		Assert.assertEquals(p.luceneDir,new File("./src/test/resources/chav1961/jcw/index"));
		Assert.assertEquals(p.crowlingDir.length,1);
		Assert.assertEquals(p.crowlingDir[0],URI.create("file:./src/main/java"));

		
		p = Application.parseParameters(new String[]{"-index","./src/test/resources/chav1961/jcw/index","-crowling","file:./src/main/java;file:./src/test/java","-refresh","-noscreen"});
		
		Assert.assertTrue(p.refresh);
		Assert.assertFalse(p.inMemory);
		Assert.assertFalse(p.screen);
		Assert.assertEquals(p.luceneDir,new File("./src/test/resources/chav1961/jcw/index"));
		Assert.assertEquals(p.crowlingDir.length,2);
		Assert.assertEquals(p.crowlingDir[0],URI.create("file:./src/main/java"));

		p = Application.parseParameters(new String[]{"-conf","./src/test/resources/chav1961/jcw/testconfig.properties","-index","./src/test/resources/chav1961/jcw/index","+crowling","file:./src/test/java","-noscreen"});

		Assert.assertTrue(p.refresh);
		Assert.assertFalse(p.inMemory);
		Assert.assertFalse(p.screen);
		Assert.assertEquals(p.luceneDir,new File("./src/test/resources/chav1961/jcw/index"));
		Assert.assertEquals(p.crowlingDir.length,2);
		Assert.assertEquals(p.crowlingDir[0],URI.create("file:./src/test/java"));

		p = Application.parseParameters(new String[]{"-conf","./src/test/resources/chav1961/jcw/testconfig.properties","-nocrowling"});

		Assert.assertNull(p.crowlingDir);
	}

	@Test
	public void testParseIllegalParameters() {
		try{Application.parseParameters(new String[]{"-unknown"});
			Assert.fail("Mandatory exception was not detected (unknown key)");
		} catch (CommandLineParametersException exc) {
		}

		
		try{Application.parseParameters(new String[]{"-conf"});
			Assert.fail("Mandatory exception was not detected (config file is missing)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.parseParameters(new String[]{"-conf", "a:/unknown"});
			Assert.fail("Mandatory exception was not detected (config file is not exists)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.parseParameters(new String[]{"-conf", "./src/test/resources/chav1961/jcw/testconfig.properties","-conf", "./src/test/resources/chav1961/jcw/testconfig.properties"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}
		
		
		try{Application.parseParameters(new String[]{"-index"});
			Assert.fail("Mandatory exception was not detected (index directory is missing)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{Application.parseParameters(new String[]{"-index","./src/test/resources/chav1961/jcw/testconfig.properties"});
			Assert.fail("Mandatory exception was not detected (not a directory for index)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.parseParameters(new String[]{"-index","a:/unexsitent/index"});
			Assert.fail("Mandatory exception was not detected (can't create directory for index)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.parseParameters(new String[]{"-index","./src/test/resources/chav1961/jcw/index","-index","./src/test/resources/chav1961/jcw/index"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}
		
		
		try{Application.parseParameters(new String[]{"-crowling"});
			Assert.fail("Mandatory exception was not detected (crowling URI)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{Application.parseParameters(new String[]{"-crowling",":123"});
			Assert.fail("Mandatory exception was not detected (illegal URI format for crowling)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.parseParameters(new String[]{"-crowling","file:./src/test/java","-crowling","file:./src/test/java"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}


		try{Application.parseParameters(new String[]{"-nocrowling","-nocrowling"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{Application.parseParameters(new String[]{"-crowling","file:./src/test/java","-nocrowling"});
			Assert.fail("Mandatory exception was not detected (mutually exclusive parameters)");
		} catch (CommandLineParametersException exc) {
		}

		
		try{Application.parseParameters(new String[]{"-refresh","-refresh"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}

		
		try{Application.parseParameters(new String[]{"-noscreen","-noscreen"});
			Assert.fail("Mandatory exception was not detected (duplicate parameter)");
		} catch (CommandLineParametersException exc) {
		}
	}

	@Test
	public void testFillIllegalParameters() throws IOException {
		final Application.Parameters	p = new Application.Parameters();
		
		try{Application.fillParameters(new ByteArrayInputStream("unknown=\n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (unknown key)");
		} catch (CommandLineParametersException exc) {
		}

		
		try{Application.fillParameters(new ByteArrayInputStream("index= \n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (lucene index directory is not typed)");
		} catch (CommandLineParametersException exc) {
		}
		
		try{Application.fillParameters(new ByteArrayInputStream("index=a:/unknown\n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (illegal lucene index directory)");
		} catch (CommandLineParametersException exc) {
		}

		
		try{Application.fillParameters(new ByteArrayInputStream("crowling=\n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (crowling uris are not typed)");
		} catch (CommandLineParametersException exc) { 
		}

		try{Application.fillParameters(new ByteArrayInputStream("crowling=:illegal\n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (crowling uris are illegal)");
		} catch (CommandLineParametersException exc) {
		}

		try{Application.fillParameters(new ByteArrayInputStream("refresh=unknown\n".getBytes()),p);
			Assert.fail("Mandatory exception was not detected (unknown boolean value)");
		} catch (CommandLineParametersException exc) {
		}
	}
	
	
	@Test
	public void testPrintUsage() throws CommandLineParametersException {
		Application.printUsage();
		Application.parseParameters(new String[]{"-help"});
	}
}
