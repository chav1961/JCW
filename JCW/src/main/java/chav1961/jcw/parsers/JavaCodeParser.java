package chav1961.jcw.parsers;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import chav1961.jcw.interfaces.TokenKeywords;
import chav1961.jcw.interfaces.TokenSort;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class JavaCodeParser implements Iterable<TokenDescriptor> {
	private static final SyntaxTreeInterface<TokenDescriptor>	WORDS = new AndOrTree<>();	
	
	static {
		WORDS.placeName("abstract",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._abstract));		
		WORDS.placeName("assert",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._assert));
		WORDS.placeName("boolean",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._boolean));
		WORDS.placeName("break",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._break));
		WORDS.placeName("byte",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._byte));
		WORDS.placeName("case",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._case));
		WORDS.placeName("catch",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._catch));
		WORDS.placeName("char",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._char));
		WORDS.placeName("class",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._class));
		WORDS.placeName("const",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._const));
		WORDS.placeName("continue",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._continue));
		WORDS.placeName("default",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._default));
		WORDS.placeName("do",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._do));
		WORDS.placeName("double",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._double));
		WORDS.placeName("else",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._else));
		WORDS.placeName("enum",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._enum));
		WORDS.placeName("extends",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._extends));
		WORDS.placeName("false",new TokenDescriptor(TokenSort.SPECIALCONSTANT, TokenKeywords._false));
		WORDS.placeName("final",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._final));
		WORDS.placeName("finally",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._finally));
		WORDS.placeName("float",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._float));
		WORDS.placeName("for",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._for));
		WORDS.placeName("goto",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._goto));
		WORDS.placeName("if",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._if));
		WORDS.placeName("implements",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._implements));
		WORDS.placeName("import",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._import));
		WORDS.placeName("instanceof",new TokenDescriptor(TokenSort.OPERATORS, TokenKeywords._instanceof));
		WORDS.placeName("int",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._int));
		WORDS.placeName("interface",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._interface));
		WORDS.placeName("long",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._long));
		WORDS.placeName("native",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._native));
		WORDS.placeName("new",new TokenDescriptor(TokenSort.OPERATORS, TokenKeywords._new));
		WORDS.placeName("null",new TokenDescriptor(TokenSort.SPECIALCONSTANT, TokenKeywords._null));
		WORDS.placeName("package",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._package));
		WORDS.placeName("private",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._private));
		WORDS.placeName("protected",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._protected));
		WORDS.placeName("public",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._public));
		WORDS.placeName("return",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._return));
		WORDS.placeName("short",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._short));
		WORDS.placeName("static",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._static));
		WORDS.placeName("strictfp",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._strictfp));
		WORDS.placeName("super",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._super));
		WORDS.placeName("switch",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._switch));
		WORDS.placeName("synchronized",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._synchronized));
		WORDS.placeName("this",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._this));
		WORDS.placeName("throw",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._throw));
		WORDS.placeName("throws",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._throws));
		WORDS.placeName("transient",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._transient));
		WORDS.placeName("true",new TokenDescriptor(TokenSort.SPECIALCONSTANT, TokenKeywords._true));
		WORDS.placeName("try",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._try));
		WORDS.placeName("void",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._void));
		WORDS.placeName("volatile",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._volatile));
		WORDS.placeName("while",new TokenDescriptor(TokenSort.KEYWORD, TokenKeywords._while));
	}

	private final List<TokenDescriptor>	tokens = new ArrayList<>();
	private final StringBuilder			content = new StringBuilder(), commentContent = new StringBuilder();
	private final long[]				numbers = new long[2];
	private boolean						insideComment = false;
	private boolean						insideDoc = false;

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
	public Iterator<TokenDescriptor> iterator() {
		return tokens.iterator();
	}
	
	public String getContent() {
		return content.toString();
	}
	
	
	void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length, final List<TokenDescriptor> tokens) throws IOException, SyntaxException {
		final int	start = from, to = from + length;
		
		content.append(data, from, length);
		
		while (from < to && data[from] != '\n' && data[from] != '\r') {
			from = CharUtils.skipBlank(data, from, true);
			
			if (insideComment) {
				if (data[from] == '*' && data[from+1] == '/') {
					final char[]	content = new char[commentContent.length()];
					commentContent.getChars(0, content.length, content, 0);
					
					tokens.add(new TokenDescriptor(insideDoc ? TokenSort.DOCCONTENT : TokenSort.COMMENT, content, 0, content.length));
					insideDoc = false;
					insideComment = false;
					from += 2;
				}
				else {
					commentContent.append(data[from++]);
				}
			}
			else {
				if (data[from] == '/' && data[from+1] == '/') {
					tokens.add(new TokenDescriptor(TokenSort.COMMENT, data, from, to));
					return;
				}
				if (data[from] == '/' && data[from+1] == '*' && data[from+2] == '*') {
					from += 3;
					insideComment = true;
					insideDoc = true;
					commentContent.setLength(0);
				}
				else if (data[from] == '/' && data[from+1] == '*') {
					from += 2;
					insideComment = true;
					insideDoc = false;
					commentContent.setLength(0);
				}
				else {
					switch (data[from]) {
						case '(' : case ')' : case '{' : case '}' : case '[' : case ']' : case ';' : case ',' :
							tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, new char[]{data[from]}, 0, 0));
							from++;
							break;
						case '.'	:
							if (data[from+1] == '.' && data[from+2] == '.') {
								tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, Arrays.copyOfRange(data, from, from+3), 0, 0));
								from += 3;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case ':'	:
							if (data[from+1] == ':') {
								tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '~' : case '?' :
							tokens.add(new TokenDescriptor(TokenSort.PUNCTUATION, new char[]{data[from]}, 0, 0));
							from++;
							break;
						case '='	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '>'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '>' && data[from+2] == '>') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+3), 0, 0));
								from += 3;
								break;
							}
							else if (data[from+1] == '>') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '<'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '<') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '!'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '&'	:
							if (data[from+1] == '&') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '|'	:
							if (data[from+1] == '|') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '+'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '+') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '-'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else if (data[from+1] == '-') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '*'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '/'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '^'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '%'	:
							if (data[from+1] == '=') {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, Arrays.copyOfRange(data, from, from+2), 0, 0));
								from += 2;
								break;
							}
							else {
								tokens.add(new TokenDescriptor(TokenSort.OPERATORS, new char[]{data[from]}, 0, 0));
								from++;
								break;
							}
						case '0' : case '1' : case '2' : case '3' : case '4' : case '5' : case '6' : case '7' : case '8' : case '9' :
							int	digiFrom = from;
							
							from = CharUtils.parseNumber(data, from, numbers, CharUtils.PREF_ANY, false);
							
							final char[]	digiContent = Arrays.copyOfRange(data, digiFrom, from);
							tokens.add(new TokenDescriptor(TokenSort.NUMCONSTANT, digiContent, (int)(displacement + (digiFrom - start)), (int)(displacement + (from - start))));
							break;
						case '\"' :
							final StringBuilder	sbString = new StringBuilder();
							
							from = CharUtils.parseStringExtended(data, from+1, '\"', sbString);
							tokens.add(new TokenDescriptor(TokenSort.TEXTCONSTANT, sbString.toString().toCharArray(), 0, 0));
							break;
						case '\'' :
							final StringBuilder	sbChar = new StringBuilder();
							
							from = CharUtils.parseStringExtended(data, from+1, '\'', sbChar);
							tokens.add(new TokenDescriptor(TokenSort.TEXTCONSTANT, sbChar.toString().toCharArray(), 0, 0));
							break;
						default : 
							if (Character.isJavaIdentifierStart(data[from])) {
								int	begin = from;
								
								while (Character.isJavaIdentifierPart(data[from])) {
									from++;
								}
								final long	item = WORDS.seekName(data, begin, from);
								
								if (item < 0) {
									final char[]	content = Arrays.copyOfRange(data, begin, from);
									
									tokens.add(new TokenDescriptor(TokenSort.IDENTIFIER, content, (int)(displacement + (begin - start)), (int)(displacement + (from - start))));
								}
								else {
									tokens.add(WORDS.getCargo(item));
								}
							}
							else {
								from++;
							}
					}
				}
			}
		}
		if (insideComment) {
			commentContent.append('\n');
		}
	}
}
