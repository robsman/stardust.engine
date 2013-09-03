package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.io.Serializable;

import org.restlet.Request;
import org.restlet.Response;

public class RestletResponse extends Response implements Serializable {

	public RestletResponse(Request request) {
		super(request);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
