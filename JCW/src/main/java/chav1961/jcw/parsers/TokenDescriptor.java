package chav1961.jcw.parsers;

import java.util.Arrays;

import chav1961.jcw.interfaces.TokenKeywords;
import chav1961.jcw.interfaces.TokenSort;

public class TokenDescriptor {
	public TokenSort		tokenSort;
	public TokenKeywords	tokenKeywordSort;
	public char[]			tokenContent;
	
	public TokenDescriptor(final TokenSort tokenSort, final char[] tokenContent, final int fromToken, final int toToken) {
		this.tokenSort = tokenSort;
		this.tokenKeywordSort = TokenKeywords.undefined;
		this.tokenContent = tokenContent.clone();
	}

	public TokenDescriptor(final TokenSort tokenSort, final TokenKeywords keywordSort) {
		this.tokenSort = tokenSort;
		this.tokenKeywordSort = keywordSort;
		this.tokenContent = null;
	}

	@Override
	public String toString() {
		return "TokenDescriptor [tokenSort=" + tokenSort + ", tokenKeywordSort=" + tokenKeywordSort + ", tokenContent=" + Arrays.toString(tokenContent) + "]";
	}
}
