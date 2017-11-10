package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.jcw.interfaces.TokenSort;

class JCParser implements Iterable<TokenDescriptor<TokenSort>> {
	private static final int		TYPICAL_CONTENT_SIZE = 65536;
	
	private static final char[][]	RESERVED_WORDS = new char[][]{
										"abstract".toCharArray(),	"assert".toCharArray(),			"boolean".toCharArray(),	"break".toCharArray(),
										"byte".toCharArray(),		"case".toCharArray(),			"catch".toCharArray(),		"char".toCharArray(),
										"class".toCharArray(),		"const".toCharArray(),			"continue".toCharArray(),	"default".toCharArray(),
										"do".toCharArray(),			"double".toCharArray(),			"else".toCharArray(),		"enum".toCharArray(),
										"extends".toCharArray(),	"final".toCharArray(),			"finally".toCharArray(),	"float".toCharArray(),
										"for".toCharArray(),		"goto".toCharArray(),			"if".toCharArray(),			"implements".toCharArray(),
										"import".toCharArray(),		"instanceof".toCharArray(),		"int".toCharArray(),		"interface".toCharArray(),
										"long".toCharArray(),		"native".toCharArray(),			"new".toCharArray(),		"package".toCharArray(),
										"private".toCharArray(),	"protected".toCharArray(),		"public".toCharArray(),		"return".toCharArray(),
										"short".toCharArray(),		"static".toCharArray(),			"strictfp".toCharArray(),	"super".toCharArray(),
										"switch".toCharArray(),		"synchronized".toCharArray(),	"this".toCharArray(),		"throw".toCharArray(),
										"throws".toCharArray(),		"transient".toCharArray(),		"try".toCharArray(),		"void".toCharArray(),
										"volatile".toCharArray(),	"while".toCharArray()	
									};
	private static final char[][]	SPECIAL_CONSTANTS = new char[][]{
										"false".toCharArray(),		"null".toCharArray(),			"true".toCharArray()
									};
    private static final SetClass	EOL = new SetClass("\n\r\0");
    private static final SetClass	BIN = new SetClass("01_");
    private static final SetClass	OCT = new SetClass("01234567_");
    private static final SetClass	DEC = new SetClass("0123456789_");
    private static final SetClass	HEX = new SetClass("0123456789ABCDEFabcdef_");
	private static final SetClass	BLANKS = new SetClass(" \n\r\f\t");
	private static final SetClass	SEPARATORS = new SetClass("(){}[];,.");
	private static final SetClass	OPERATORS = new SetClass("=><!~?:&|+-*/^%");
	
	private final char[]			content;
	private volatile boolean		finished = false;

	JCParser(final InputStream is) throws IOException {
		this(is,TYPICAL_CONTENT_SIZE);
	}
	
	JCParser(final InputStream is, final int typicalContentLength) throws IOException {
		if (is == null) {
			throw new IllegalArgumentException("Input stream can't be null");
		}
		else if (typicalContentLength < 1024) {
			throw new IllegalArgumentException("Typical content length ["+typicalContentLength+"] is too small. Need be at least 1024");
		}
		else {
			try(final Reader	rdr = new InputStreamReader(is)) {
				this.content = loadContent(rdr,typicalContentLength);
			}
		}
	}

	JCParser(final Reader rdr) throws IOException {
		this(rdr,TYPICAL_CONTENT_SIZE);
	}
	
	JCParser(final Reader rdr, final int typicalContentLength) throws IOException {
		if (rdr == null) {
			throw new IllegalArgumentException("Input stream reader can't be null");
		}
		else if (typicalContentLength < 1024) {
			throw new IllegalArgumentException("Typical content length ["+typicalContentLength+"] is too small. Need be at least 1024");
		}
		else {
			this.content = loadContent(rdr,typicalContentLength);
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
							while (BLANKS.inside(tmp[pos])) {
								pos++;
							}
							break;
						case '(' : case ')' : case '{' : case '}' : case '[' : case ']' : case ';' : case ',' : case '.' :	// Separators
							token.tokenSort = TokenSort.PUNCTUATION;
							while (SEPARATORS.inside(tmp[pos])) {
								pos++;
							}
							break;
						case '/' :	// Comments or operators
							if (tmp[pos+1] == '/') {
								token.tokenSort = TokenSort.COMMENT;
								while (!EOL.inside(tmp[pos])) {
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
								break;
							}
						case '=' : case '>' : case '<' : case '!' : case '~' : case '?' : case ':' : case '&' : case '|' :	// Operators 
						case '+' : case '-' : case '*' : case '^' : case '%' :
							token.tokenSort = TokenSort.OPERATORS;
							while (OPERATORS.inside(tmp[pos])) {
								pos++;
							}
							break;
						case '\'' :	// Chars
							break;
						case '\"' : // Strings
							break;
						case '@' : 	// Annotations
							break;
						case '0' : 	// Numeric constants
							token.tokenSort = TokenSort.NUMCONSTANT;
							switch (tmp[pos+1]) {
								case 'b': case 'B' :
									pos += 2;
									while (BIN.inside(tmp[pos])) {
										pos++;
									}
									break;
								case 'x': case 'X' :
									pos += 2;
									while (HEX.inside(tmp[pos])) {
										pos++;
									}
									break;
								default :
									while (OCT.inside(tmp[pos])) {
										pos++;
									}
									break;
							}
						case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
							token.tokenSort = TokenSort.NUMCONSTANT;
							while (DEC.inside(tmp[pos])) {
								pos++;
							}
							if (tmp[pos] == '.') {
								pos++;
								while (DEC.inside(tmp[pos])) {
									pos++;
								}
							}
							if (tmp[pos] == 'e' || tmp[pos] == 'E') {
								pos++;
								if (tmp[pos] == '+' || tmp[pos] == '-') {
									pos++;
								}
								while (DEC.inside(tmp[pos])) {
									pos++;
								}
							}
							break;
						default :
							if (Character.isJavaIdentifierStart(tmp[pos])) {
								token.tokenSort = TokenSort.IDENTIFIER;
								while (Character.isJavaIdentifierPart(content[pos])) {
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
						
					}
					
					return token;
				}
			};
		}		
	}

	private char[] loadContent(final Reader rdr, final int typicalContentLength) throws IOException {
		final char[]		result;
		final List<char[]>	content = new ArrayList<>();
		int					len = 0, displ, commonDispl;
		
		do {char[]		buffer = new char[typicalContentLength];

			displ = 0;
			while (displ < buffer.length - 1 && (len = rdr.read(buffer,displ,buffer.length-displ)) > 0) {
				displ += len;
			}
			content.add(buffer);
		} while (len > 0);
	
		if (content.size() > 1) {
			int	commonLen = displ + 1;
			
			for (int index = 0; index < content.size() - 1; index++) {
				commonLen += content.get(index).length;
			}
			
			result = new char[commonLen];
			commonDispl = 0;
			
			for (int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
				final char[]	piece = content.get(index);
				final int		pieceLen = index == maxIndex - 1 ? displ : piece.length;
				
				System.arraycopy(piece,0,result,commonDispl,piece.length);
				commonDispl += piece.length;
			}
		}
		else {
			result = content.get(0);
			commonDispl = displ;
		}
		
		result[commonDispl] = 0;
		return result;
	}
	
	private static class SetClass {
		final long[]	mask = new long[2];
		
		SetClass(final String maskContent) {
			char	symbol;
			
			for (int index = 0, maxIndex = maskContent.length(); index < maxIndex; index++) {
				if ((symbol = maskContent.charAt(index)) < 128) {
					if (symbol < 64) {
						mask[0] |= (1 << symbol);
					}
					else {
						mask[1] |= (1 << (symbol - 64));
					}
				}
			}
		}
		
		boolean inside(final char symbol) {
			if (symbol < 128) {
				if (symbol < 64) {
					return (mask[0] & (1 << symbol)) != 0;
				}
				else {
					return (mask[1] & (1 << (symbol - 64))) != 0;
				}
			}
			else {
				return false;
			}
		}
	}
}
