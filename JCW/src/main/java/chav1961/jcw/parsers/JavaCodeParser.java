package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.lucene.analysis.CharacterUtils;

import chav1961.jcw.interfaces.TokenSort;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

class JavaCodeParser implements Iterable<TokenDescriptor<TokenSort>> {
	private static final int			TYPICAL_CONTENT_SIZE = 65536;
	
	private static final SyntaxTreeInterface<TokenSort>	WORDS = new AndOrTree<>();	
    private static final BitCharSet		EOL = new BitCharSet("\n\0");
	private static final BitCharSet		BLANKS = new BitCharSet(" \n\r\f\t");
	private static final BitCharSet		SEPARATORS = new BitCharSet("(){}[];,.");
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
	
	private final char[]			content;
	private volatile boolean		finished = false;

	JavaCodeParser(final InputStream is) throws IOException {
		this(is,TYPICAL_CONTENT_SIZE);
	}
	
	JavaCodeParser(final InputStream is, final int typicalContentLength) throws IOException {
		if (is == null) {
			throw new IllegalArgumentException("Input stream can't be null");
		}
		else if (typicalContentLength < 1024) {
			throw new IllegalArgumentException("Typical content length ["+typicalContentLength+"] is too small. Need be at least 1024");
		}
		else {
			try(final Reader	rdr = new InputStreamReader(is)) {
				this.content = ParserFactory.loadContent(rdr,typicalContentLength);
			}
		}
	}

	JavaCodeParser(final Reader rdr) throws IOException {
		this(rdr,TYPICAL_CONTENT_SIZE);
	}
	
	JavaCodeParser(final Reader rdr, final int typicalContentLength) throws IOException {
		if (rdr == null) {
			throw new IllegalArgumentException("Input stream reader can't be null");
		}
		else if (typicalContentLength < 1024) {
			throw new IllegalArgumentException("Typical content length ["+typicalContentLength+"] is too small. Need be at least 1024");
		}
		else {
			this.content = ParserFactory.loadContent(rdr,typicalContentLength);
		}
	}
	
	@Override
	public Iterator<TokenDescriptor<TokenSort>> iterator() {
		if (finished) {
			throw new IllegalStateException("Can't create iterator on the stream already processed");
		}
		else {
			finished = true;
			
			return new Iterator<TokenDescriptor<TokenSort>>(){
				final TokenDescriptor<TokenSort>	token = new TokenDescriptor<TokenSort>();
				final StringBuilder					sb = new StringBuilder();
				final long[]						forLong = new long[2];
				
				int			pos = 0;
				
				@Override
				public boolean hasNext() {
					return content[pos] != 0;
				}

				@Override
				public TokenDescriptor<TokenSort> next() {
					final char[]	tmp = content;
					
					token.tokenContent = tmp;
					token.fromToken = pos;
					
					switch (tmp[pos]) {
						case ' ' : case '\n' : case '\r' : case '\f' : case '\t' :		// Whitespace
							token.tokenSort = TokenSort.WHITESPACE;
							while (BLANKS.contains(tmp[pos])) {
								pos++;
							}
							break;
						case '(' : case ')' : case '{' : case '}' : case '[' : case ']' : case ';' : case ',' : case '.' :	// Separators
							token.tokenSort = TokenSort.PUNCTUATION;
							while (SEPARATORS.contains(tmp[pos])) {
								pos++;
							}
							break;
						case '/' :	// Comments or operators
							if (tmp[pos+1] == '/') {
								token.tokenSort = TokenSort.COMMENT;
								while (!EOL.contains(tmp[pos])) {
									pos++;
								}
								if (tmp[pos] == '\n') {
									pos++;
								}
								break;
							}
							else if (tmp[pos+1] == '*') {
								if (tmp[pos+2] == '*') {
									pos += 3;
									token.tokenSort = TokenSort.DOCCONTENT;
									while (tmp[pos] != 0 && !(tmp[pos-1] == '*' && tmp[pos] == '/')) {
										pos++;
									}
								}
								else {
									pos += 2;
									token.tokenSort = TokenSort.COMMENT;
									while (tmp[pos] != 0 && !(tmp[pos-1] == '*' && tmp[pos] == '/')) {
										pos++;
									}
								}
								if (tmp[pos] == '/') {
									pos++;
								}
								break;
							}
						case '=' : case '>' : case '<' : case '!' : case '~' : case '?' : case ':' : case '&' : case '|' :	// Operators 
						case '+' : case '-' : case '*' : case '^' : case '%' :
							token.tokenSort = TokenSort.OPERATORS;
							while (OPERATORS.contains(tmp[pos])) {
								pos++;
							}
							break;
						case '\'' :	// Chars
							token.tokenSort = TokenSort.TEXTCONSTANT;
							pos = CharUtils.parseStringExtended(tmp,pos+1,'\'',sb);
							sb.setLength(0);
							break;
						case '\"' : // Strings
							token.tokenSort = TokenSort.TEXTCONSTANT;
							pos = CharUtils.parseStringExtended(tmp,pos+1,'\"',sb);
							sb.setLength(0);
							break;
						case '@' : 	// Annotations
							token.tokenSort = TokenSort.ANNOTATION;
							pos++;
							while (BLANKS.contains(tmp[pos])) {
								pos++;
							}
							while (Character.isJavaIdentifierPart(content[pos])) {
								pos++;
							}
							while (BLANKS.contains(tmp[pos])) {
								pos++;
							}
							if (tmp[pos] == '(') {	// Skip expression brackets
								int	depth = 1;
								
								pos++;
								while (tmp[pos] != 0 && depth > 0) {
									switch (tmp[pos]) {
										case '{' : case '(' : case '[' : 
											depth++; 	pos++; 
											break;
										case '}' : case ')' : case ']' :
											depth--; 	pos++; 
											break;
										case '\'' :
											pos = CharUtils.parseString(tmp,pos+1,'\'',sb);
											sb.setLength(0);
											break;
										case '\"' :
											pos = CharUtils.parseString(tmp,pos+1,'\"',sb);
											sb.setLength(0);
											break;
										default :
											pos++;
									}
								}
							}
							break;
						case '0' : 	case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' : // Numeric constants
							int		next1 = CharUtils.parseNumber(tmp,pos,forLong,CharUtils.PREF_ANY,false), next2 = CharUtils.parseLongExtended(tmp,pos,forLong,false); 
							
							token.tokenSort = TokenSort.NUMCONSTANT;
							pos = Math.max(next1,next2);
							break;
						default :
							if (Character.isJavaIdentifierStart(tmp[pos])) {
								token.tokenSort = TokenSort.IDENTIFIER;
								while (tmp[pos] != 0 && Character.isJavaIdentifierPart(tmp[pos])) {
									pos++;
								}
							}
							else {
								token.tokenSort = TokenSort.UNCLASSIFIED;
								pos++;
							}
							break;
					}
					token.toToken = pos;
					
					if (token.tokenSort == TokenSort.IDENTIFIER) {	// Test reserved words and special constants
						final long	id = WORDS.seekName(token.tokenContent, token.fromToken, token.toToken);
						
						if (id > 0) {
							token.tokenSort = WORDS.getCargo(id);
						}
					}
					
					return token;
				}
			};
		}		
	}
}
