package jcw.interfaces;

@FunctionalInterface
public interface IMessagePrinter {
	void message(String format, Object... parameters);
}