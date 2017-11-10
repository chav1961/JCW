package jcw.exceptions;

public class CommandLineParametersException extends Exception {
	private static final long serialVersionUID = -1146095723752754080L;

	public CommandLineParametersException() {
	}

	public CommandLineParametersException(String arg0) {
		super(arg0);
	}

	public CommandLineParametersException(Throwable arg0) {
		super(arg0);
	}

	public CommandLineParametersException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
