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
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws.interactions;

import java.util.Set;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class InteractionsServiceConfigurator implements SOAPHandler<SOAPMessageContext>
{

   public Set<QName> getHeaders()
   {
      return null;
   }

   public boolean handleMessage(SOAPMessageContext smc)
   {
      boolean inbound = Boolean.FALSE.equals(smc.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY));
      
      if (inbound)
      {
         ServletContext context = (ServletContext) smc.get(SOAPMessageContext.SERVLET_CONTEXT);
         
         if (null != context)
         {
            InteractionRegistry registry = (InteractionRegistry) context.getAttribute(InteractionRegistry.BEAN_ID);
            if (null != registry)
            {
               InteractionsServiceFacade.bindRegistry(registry);
            }
         }
      }
      else
      {
         InteractionsServiceFacade.unbindRegistry();
      }
      
      return true;
   }

   public boolean handleFault(SOAPMessageContext smc)
   {
      return true;
   }

   public void close(MessageContext mc)
   {
   }

}
