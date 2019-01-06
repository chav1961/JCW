package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import chav1961.jcw.interfaces.ContentSort;

public class ParserFactory {

	public static <T extends Enum<?>> Iterable<TokenDescriptor<T>> createParser(final InputStream is, final ContentSort sort) {
		return null;
	}

	public static <T extends Enum<?>> Iterable<TokenDescriptor<T>> createParser(final Reader rdr, final ContentSort sort) {
		return null;
	}

	static char[] loadContent(final Reader rdr, final int typicalContentLength) throws IOException {
		final StringBuilder sb = new StringBuilder(typicalContentLength+1);
		char[]				buffer = new char[typicalContentLength+1];
		int					len;
		
		while ((len = rdr.read(buffer)) > 0) {
			sb.append(buffer,0,len);
		}
		
		if (sb.length() > typicalContentLength) {
			buffer = new char[sb.length()+1];
		}
		sb.getChars(0,sb.length(),buffer,0);
		buffer[sb.length()] = 0;
		return buffer;
	}
	
}
