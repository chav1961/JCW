package chav1961.jcw.parsers;

public class TokenDescriptor<T extends Enum<?>> {
	public T		tokenSort;
	public char[]	tokenContent;
	public int		fromToken;
	public int		toToken;
	
	@Override
	public String toString() {
		return "TokenDescriptor [tokenSort=" + tokenSort + ", tokenContent=" + new String(tokenContent, fromToken, toToken - fromToken) + "]";
	}
}
