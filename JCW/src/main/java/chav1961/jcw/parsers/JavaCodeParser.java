package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.CharacterUtils;

import chav1961.jcw.interfaces.TokenSort;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class JavaCodeParser implements Iterable<TokenDescriptor<TokenSort>> {
	
	private static final SyntaxTreeInterface<TokenSort>	WORDS = new AndOrTree<>();	
	private static final BitCharSet		SEPARATORS = new BitCharSet("(){}[];,.:");
	private static final BitCharSet		OPERATORS = new BitCharSet("=><!~?:&|+-*/^%");
	
	static {
		WORDS.placeName("abstract",TokenSort.KEYWORD);		WORDS.placeName("assert",TokenSort.KEYWORD);
		WORDS.placeName("boolean",TokenSort.KEYWORD);		WORDS.placeName("break",TokenSort.KEYWORD);
		WORDS.placeName("byte",TokenSort.KEYWORD);			WORDS.placeName("case",TokenSort.KEYWORD);
		WORDS.placeName("catch",TokenSort.KEYWORD);			WORDS.placeName("char",TokenSort.KEYWORD);
		WORDS.placeName("class",TokenSort.KEYWORD);			WORDS.placeName("const",TokenSort.KEYWORD);
		WORDS.placeName("continue",TokenSort.KEYWORD);		WORDS.placeName("default",TokenSort.KEYWORD);
		WORDS.placeName("do",TokenSort.KEYWORD);			WORDS.placeName("double",TokenSort.KEYWORD);
		WORDS.placeName("else",TokenSort.KEYWORD);			WORDS.placeName("enum",TokenSort.KEYWORD);
		WORDS.placeName("extends",TokenSort.KEYWORD);		WORDS.placeName("final",TokenSort.KEYWORD);
		WORDS.placeName("finally",TokenSort.KEYWORD);		WORDS.placeName("float",TokenSort.KEYWORD);
		WORDS.placeName("for",TokenSort.KEYWORD);			WORDS.placeName("goto",TokenSort.KEYWORD);
		WORDS.placeName("if",TokenSort.KEYWORD);			WORDS.placeName("implements",TokenSort.KEYWORD);
		WORDS.placeName("import",TokenSort.KEYWORD);		WORDS.placeName("instanceof",TokenSort.KEYWORD);
		WORDS.placeName("int",TokenSort.KEYWORD);			WORDS.placeName("interface",TokenSort.KEYWORD);
		WORDS.placeName("long",TokenSort.KEYWORD);			WORDS.placeName("native",TokenSort.KEYWORD);
		WORDS.placeName("new",TokenSort.KEYWORD);			WORDS.placeName("package",TokenSort.KEYWORD);
		WORDS.placeName("private",TokenSort.KEYWORD);		WORDS.placeName("protected",TokenSort.KEYWORD);
		WORDS.placeName("public",TokenSort.KEYWORD);		WORDS.placeName("return",TokenSort.KEYWORD);
		WORDS.placeName("short",TokenSort.KEYWORD);			WORDS.placeName("static",TokenSort.KEYWORD);
		WORDS.placeName("strictfp",TokenSort.KEYWORD);		WORDS.placeName("super",TokenSort.KEYWORD);
		WORDS.placeName("switch",TokenSort.KEYWORD);		WORDS.placeName("synchronized",TokenSort.KEYWORD);
		WORDS.placeName("this",TokenSort.KEYWORD);			WORDS.placeName("throw",TokenSort.KEYWORD);
		WORDS.placeName("throws",TokenSort.KEYWORD);		WORDS.placeName("transient",TokenSort.KEYWORD);
		WORDS.placeName("try",TokenSort.KEYWORD);			WORDS.placeName("void",TokenSort.KEYWORD);
		WORDS.placeName("volatile",TokenSort.KEYWORD);		WORDS.placeName("while",TokenSort.KEYWORD);
		
		WORDS.placeName("false",TokenSort.SPECIALCONSTANT);	WORDS.placeName("null",TokenSort.SPECIALCONSTANT);
		WORDS.placeName("true",TokenSort.SPECIALCONSTANT);
	}
	
	private final List<TokenDescriptor<TokenSort>>	tokens = new ArrayList<>();
	private boolean		insideComment = false;

	public JavaCodeParser(final Reader rdr) throws IOException, SyntaxException {
		if (rdr == null) {
			throw new NullPointerException("Input stream can't be null");
		}
		else {
			try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)->processLine(displacement, lineNo, data, from, length, tokens))) {
				lblp.write(rdr);
			}
		}
	}
	
	@Override
	public Iterator<TokenDescriptor<TokenSort>> iterator() {
		return tokens.iterator();
	}
	
	void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length, final List<TokenDescriptor<TokenSort>> tokens) throws IOException, SyntaxException {
		final int	start = from;
		
		while (data[from] != '\n') {
			from = CharUtils.skipBlank(data, from, true);
			
			if (insideComment) {
				if (data[from] == '*' && data[from+1] == '/') {
					from += 2;
					insideComment = false;
				}
			}
			else {
				if (data[from] == '/' && data[from+1] == '/') {
					return;
				}
				if (data[from] == '/' && data[from+1] == '*') {
					from += 2;
					insideComment = true;
				}
				else {
					while (SEPARATORS.contains(data[from])) {
						from++;
					}
					while (OPERATORS.contains(data[from])) {
						from++;
					}
					while (Character.isDigit(data[from])) {
						from++;
					}
					if (Character.isJavaIdentifierStart(data[from])) {
						int	begin = from;
						
						while (Character.isJavaIdentifierPart(data[from])) {
							from++;
						}
						if (WORDS.seekName(data, begin, from) < 0) {
							final char[]	content = Arrays.copyOfRange(data, begin, from);
							
							tokens.add(new TokenDescriptor<TokenSort>(TokenSort.IDENTIFIER, content, (int)(displacement + (begin - start)), (int)(displacement + (from - start))));
						}
					}
				}
			}
		}
	}
}
