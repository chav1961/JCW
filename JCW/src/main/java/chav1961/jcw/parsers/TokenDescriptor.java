package chav1961.jcw.parsers;

public class TokenDescriptor<T extends Enum<?>> {
	public T		tokenSort;
	public char[]	tokenContent;
	public int		fromToken;
	public int		toToken;
	
	public TokenDescriptor(final T tokenSort, final char[] tokenContent, final int fromToken, final int toToken) {
		this.tokenSort = tokenSort;
		this.tokenContent = tokenContent;
		this.fromToken = fromToken;
		this.toToken = toToken;
	}

	@Override
	public String toString() {
		return "TokenDescriptor [tokenSort=" + tokenSort + ", tokenContent=" + new String(tokenContent, fromToken, toToken - fromToken) + "]";
	}
}
