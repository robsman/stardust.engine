/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2011 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.rest.processinterface;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * <p>
 * This class maps all instances of <code>java.lang.RuntimeException</code>
 * to the HTTP status code 400 Bad Request.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
@Provider
public class RuntimeExceptionProvider implements ExceptionMapper<RuntimeException>
{
   public Response toResponse(final RuntimeException e)
   {
      if (e instanceof WebApplicationException)
      {
         return ((WebApplicationException) e).getResponse();
      }
      
      final Response response = Response
         .status(Status.BAD_REQUEST)
         .entity(e.getMessage())
         .build();
      return response;
   }
}
