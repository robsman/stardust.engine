package org.eclipse.stardust.engine.extensions.camel.runtime;

import java.io.Serializable;
import java.security.Principal;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.engine.http.connector.ConnectedRequest;
import org.restlet.engine.http.connector.ServerConnection;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

public class RestletRequest extends ConnectedRequest implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public RestletRequest(ConnectedRequest request) {
		super(request);
	}

	public RestletRequest(Context context, ServerConnection serverConnection, Method method,
			String string1, String string2, Series<Parameter> series,
			Representation representation, boolean bool, Principal principal) {
		super(context, serverConnection, method, string1, string2, series, representation, bool, principal);
	}
}
