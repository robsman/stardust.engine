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
 * (C) 2000 - 2010 SunGard CSA LLC
 */
package org.eclipse.stardust.engine.ws.configurer;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.ws.WebServiceEnv;



/**
 * @author nicolas.werlein
 * @version $Revision: $
 */
public class WebServiceEnvUsernameHttpBasicAuthConfigurer implements SOAPHandler<SOAPMessageContext>
{
   private static final Logger TRACE = LogManager.getLogger(WebServiceEnvSessionPropConfigurer.class);
   
   public Set<QName> getHeaders()
   {
      return Collections.emptySet();
   }

   public void close(final MessageContext ctx)
   {
      /* nothing to do */
   }

   public boolean handleFault(final SOAPMessageContext ctx)
   {
      /* nothing to do */
      return true;
   }

   public boolean handleMessage(final SOAPMessageContext ctx)
   {
      boolean inbound = Boolean.FALSE.equals(ctx.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY));
      if ( !inbound)
      {
         return true;
      }
      
      String userId = null;
      String password = null;
      
      @SuppressWarnings("unchecked")
      Map<String, List<String>> httpHeaders = (Map<String, List<String>>) ctx.get(SOAPMessageContext.HTTP_REQUEST_HEADERS);
      List<String> authHeaders = httpHeaders.get("Authorization");
      if ( !isEmpty(authHeaders))
      {
         for (String authHeader : authHeaders)
         {
            TRACE.debug("Found HTTP authentication header.");
            
            if (authHeader.startsWith("Basic"))
            {
               TRACE.debug("Extracting HTTP Basic authentication header.");

               String authToken = authHeader.substring("Basic".length()).trim();
               if ( !isEmpty(authToken))
               {
                  String authDecoded = new String(Base64.decode(authToken.getBytes()));
                  int idx = authDecoded.indexOf(':');
                  if (idx == -1)
                  {
                     userId = authDecoded;
                  }
                  else
                  {
                     userId = authDecoded.substring(0, idx);
                      if (idx < (authDecoded.length() - 1))
                      {
                          password = authDecoded.substring(idx + 1);
                      }
                  }
                  break;
               }
            }
            else
            {
               TRACE.info("Unsupported HTTP authentication header.");
            }
         }
      }
      else
      {
         TRACE.debug("Sending authentication challenge to client.");
         
         // challenge client for credentials
         ctx.put(SOAPMessageContext.HTTP_RESPONSE_CODE, HttpServletResponse.SC_UNAUTHORIZED);
         ctx.put(SOAPMessageContext.HTTP_RESPONSE_HEADERS, Collections.singletonMap("WWW-Authenticate", Arrays.asList("Basic realm=\"Eclipse Process Manager\"")));

         throw new WebServiceException("No Authorization header provided.");
      }
      
      if (isEmpty(userId))
      {
         throw new WebServiceException("Not authorized.");
      }
      
      WebServiceEnv.setCurrentCredentials(userId, password);
      
      return true;
   }
}
