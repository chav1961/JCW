package jcw.parsers;

import java.io.InputStream;
import java.io.Reader;

import jcw.interfaces.ContentSort;

public class ParserFactory {

	public static <T extends Enum<?>> Iterable<TokenDescriptor<T>> createParser(final InputStream is, final ContentSort sort) {
		return null;
	}

	public static <T extends Enum<?>> Iterable<TokenDescriptor<T>> createParser(final Reader rdr, final ContentSort sort) {
		return null;
	}
}
