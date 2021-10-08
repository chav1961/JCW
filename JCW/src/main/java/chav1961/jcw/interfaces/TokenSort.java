package chav1961.jcw.interfaces;

public enum TokenSort {
	WHITESPACE(true),
	COMMENT(false),
	DOCCONTENT(false),
	KEYWORD(true),
	PUNCTUATION(true),
	OPERATORS(true),
	NUMCONSTANT(true),
	TEXTCONSTANT(false),
	SPECIALCONSTANT(true),
	IDENTIFIER(false),
	UNCLASSIFIED(true);
	
	private final boolean ignoreOnSearch;
	
	TokenSort(final boolean ignoreOnSearch){
		this.ignoreOnSearch = ignoreOnSearch;
	}
	
	public boolean needIgnoreOnSearch() {
		return ignoreOnSearch;
	}
}
