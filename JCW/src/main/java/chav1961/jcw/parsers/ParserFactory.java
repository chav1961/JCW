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

}
