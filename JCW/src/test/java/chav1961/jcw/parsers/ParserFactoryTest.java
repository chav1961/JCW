package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;

public class ParserFactoryTest {

	@Test
	public void testCreateParserInputStreamContentSort() {
	}

	@Test
	public void testCreateParserReaderContentSort() {
	}

	@Test
	public void testLoadContent() throws IOException {
		char[]	content;
		
		content = ParserFactory.loadContent(new StringReader("0123456789"),10);
		Assert.assertArrayEquals(content, new char[]{'0','1','2','3','4','5','6','7','8','9',0});

		content = ParserFactory.loadContent(new StringReader("0123456789"),11);
		Assert.assertArrayEquals(content, new char[]{'0','1','2','3','4','5','6','7','8','9',0,0});
		
		content = ParserFactory.loadContent(new StringReader("0123456789"),9);		
		Assert.assertArrayEquals(content, new char[]{'0','1','2','3','4','5','6','7','8','9',0});
		content = ParserFactory.loadContent(new StringReader("0123456789"),4);		
		Assert.assertArrayEquals(content, new char[]{'0','1','2','3','4','5','6','7','8','9',0});
	}
}
