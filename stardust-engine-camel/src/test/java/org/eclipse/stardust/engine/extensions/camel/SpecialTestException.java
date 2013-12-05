package org.eclipse.stardust.engine.extensions.camel;

public class SpecialTestException extends Exception {

	private static final long serialVersionUID = 1L;

	public SpecialTestException(String string, Exception e) {
		super(string, e);
	}

}
