package org.eclipse.stardust.engine.extensions.camel.app;

import org.eclipse.stardust.common.StringKey;

public class CamelMessageLocation  extends StringKey{

	private static final long serialVersionUID = 1L;
	
	public static final CamelMessageLocation HEADER = new CamelMessageLocation(
			"HEADER", "Header");
	public static final CamelMessageLocation BODY = new CamelMessageLocation(
			"BODY", "Body");

	public CamelMessageLocation(String id, String defaultName) {
		super(id, defaultName);
	}

}
