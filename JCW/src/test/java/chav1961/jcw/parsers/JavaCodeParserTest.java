package chav1961.jcw.parsers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

import chav1961.jcw.interfaces.TokenSort;

public class JavaCodeParserTest {

	@Test
	public void testSimpleParsing() throws IOException {
		int		count;
		
		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader(" \t\n\r\f"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.WHITESPACE);
			count++;
		}
		Assert.assertEquals(count,1);
		
		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("(){}[];,."))) {
			Assert.assertEquals(item.tokenSort,TokenSort.PUNCTUATION);
			count++;
		}
		Assert.assertEquals(count,1);
		
		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("=><!~?:&|+-*^%"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.OPERATORS);
			count++;
		}
		Assert.assertEquals(count,1);
		
		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("/"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.OPERATORS);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("// somesheet\n"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.COMMENT);
			count++;
		}
		Assert.assertEquals(count,1);
		
		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("/* somesheet */"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.COMMENT);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("/** doc */"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.DOCCONTENT);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("'\\\''"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.TEXTCONSTANT);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("\"\\\"\""))) {
			Assert.assertEquals(item.tokenSort,TokenSort.TEXTCONSTANT);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("a123_D"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.IDENTIFIER);
			count++;
		}
		Assert.assertEquals(count,1);

		count = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("#"))) {
			Assert.assertEquals(item.tokenSort,TokenSort.UNCLASSIFIED);
			count++;
		}
		Assert.assertEquals(count,1);
	}

	@Test
	public void testComplexParsing() throws IOException {
		int		count,index;
		
		count = index = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("1,0x2F,0b10,3.22e-10"))) {
			Assert.assertEquals(item.tokenSort,index == 0 ? TokenSort.NUMCONSTANT : TokenSort.PUNCTUATION);
			count++;	index = 1 - index;
		}
		Assert.assertEquals(count,7);
	
		count = index = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("if,synchronized"))) {
			Assert.assertEquals(item.tokenSort,index == 0 ? TokenSort.KEYWORD : TokenSort.PUNCTUATION);
			count++;	index = 1 - index;
		}
		Assert.assertEquals(count,3);

		count = index = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("false,true"))) {
			Assert.assertEquals(item.tokenSort,index == 0 ? TokenSort.SPECIALCONSTANT : TokenSort.PUNCTUATION);
			count++;	index = 1 - index;
		}
		Assert.assertEquals(count,3);
		
		count = index = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new StringReader("@ annotation1 (value = [\"10{\",\"20]\"], test={5,7}),@ annotation2 (value = [\"10{\",\"20]\"], test={5,7})"))) {
			Assert.assertEquals(item.tokenSort,index == 0 ? TokenSort.ANNOTATION : TokenSort.PUNCTUATION);
			count++;	index = 1 - index;
		}
		Assert.assertEquals(count,3);

		
		count = index = 0;
		for(TokenDescriptor<?> item : new JavaCodeParser(new ByteArrayInputStream("false,true".getBytes()))) {
			Assert.assertEquals(item.tokenSort,index == 0 ? TokenSort.SPECIALCONSTANT : TokenSort.PUNCTUATION);
			count++;	index = 1 - index;
		}
		Assert.assertEquals(count,3);
		
	}

	@Test
	public void testExceptions() throws IOException {
		try{new JavaCodeParser((InputStream)null);
			Assert.fail("Mandatory exception was not detected ()");
		} catch (IllegalArgumentException exc) {
		}
	}
}
