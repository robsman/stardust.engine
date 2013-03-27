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

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.ws.WebServiceEnv;
import org.eclipse.stardust.engine.ws.processinterface.GenericWebServiceEnv;
import org.w3c.dom.Element;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class WebServiceEnvSessionPropConfigurer implements SOAPHandler<SOAPMessageContext>
{
   private static final String REF_PARAM_PARTITION = "stardust-bpm-partition";
   private static final String REF_PARAM_REALM = "stardust-bpm-realm";
   private static final String REF_PARAM_DOMAIN = "stardust-bpm-domain";
   private static final String REF_PARAM_MODEL = "stardust-bpm-model";
   
   private static final Map<String, String> refParameter2SecurityProperty;
   
   private boolean continueAfterCall = true;
   
   static
   {
      refParameter2SecurityProperty = CollectionUtils.newHashMap();
      refParameter2SecurityProperty.put(REF_PARAM_PARTITION, SecurityProperties.PARTITION);
      refParameter2SecurityProperty.put(REF_PARAM_REALM, SecurityProperties.REALM);
      refParameter2SecurityProperty.put(REF_PARAM_DOMAIN, SecurityProperties.DOMAIN);
   }
   
   public WebServiceEnvSessionPropConfigurer(boolean continueAfterCall)
   {
      this.continueAfterCall = continueAfterCall;
   }

   public WebServiceEnvSessionPropConfigurer()
   {
   }

   public Set<QName> getHeaders()
   {
      return Collections.emptySet();
   }

   public boolean handleMessage(final SOAPMessageContext ctx)
   {
      boolean inbound = Boolean.FALSE.equals(ctx.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY));
      if (inbound)
      {
         @SuppressWarnings("unchecked")
         final List<Element> refParameters = (List<Element>) ctx.get(SOAPMessageContext.REFERENCE_PARAMETERS);
         final Map<String, String> sessionProperties = newHashMap();
         for (final Element e : refParameters)
         {
            String localName = e.getLocalName();
            if (REF_PARAM_MODEL.equals(localName))
            {
               GenericWebServiceEnv.instance().setModelId(
                     e.getFirstChild().getTextContent());
            }
            else if (REF_PARAM_PARTITION.equals(localName))
            {
               GenericWebServiceEnv.instance().setPartitionId(
                     e.getFirstChild().getTextContent()); 
            }

            final String key = refParameter2SecurityProperty.get(localName);
            if (key != null)
            {
               sessionProperties.put(key, e.getFirstChild().getTextContent());
            }
         }
         
         // No Addressing header but partition might already be set.
         String partitionId = GenericWebServiceEnv.instance().getPartitionId();
         if (sessionProperties.isEmpty() && null != partitionId)

         {
            sessionProperties.put(SecurityProperties.PARTITION, partitionId);
         }

         WebServiceEnv.setCurrentSessionProperties(sessionProperties);
      }
      else
      {
         WebServiceEnv.removeCurrent();
         GenericWebServiceEnv.instance().removeEnv();
      }
      
      return continueAfterCall;
   }

   public boolean handleFault(final SOAPMessageContext ctx)
   {
      WebServiceEnv.removeCurrent();
      GenericWebServiceEnv.instance().removeEnv();
      return true;
   }

   public void close(MessageContext mc)
   {
      /* nothing to do */
   }
}
