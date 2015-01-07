/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.jms.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;



/**
 * Bootstraps all possible message providers.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MessageProviderFactory
{
   public static final Logger trace = LogManager.getLogger(MessageProviderFactory.class);

   private static final String MESSAGE_PROVIDERS_PROPERTY = "MessageProviders";

   private static MessageProviderFactory instance;

   private HashMap messageProviderMap = new HashMap();

   private MessageProviderFactory()
   {
      messageProviderMap.put(DefaultMessageProvider.class.getName(),
            new DefaultMessageProvider());
      Collection  providers = Parameters.instance().getStrings(MESSAGE_PROVIDERS_PROPERTY);
      for (Iterator i = providers.iterator(); i.hasNext();)
      {
         String className = (String) i.next();
         MessageProvider provider = null;
         try
         {
            Class type = Reflect.getClassFromClassName(className);
            provider = (MessageProvider)type.newInstance();
         }
         catch (Exception e)
         {
            throw new InternalException(e);
         }
         messageProviderMap.put(className, provider);
      }
   }

   public MessageProvider getMessageProvider(String providerName)
   {
      if (messageProviderMap.containsKey(providerName))
      {
         return (MessageProvider) messageProviderMap.get(providerName);
      }
      else
      {
         throw new InternalException("Unknown message provider : " + providerName);
      }
   }

   /**
    * Bootstraps the <code>MessageProviderFactory</code> singleton.
    */
   public static MessageProviderFactory bootstrap()
   {
      if (instance == null)
      {
         instance = new MessageProviderFactory();
      }
      return instance;
   }

   public static MessageProviderFactory instance()
   {
      return bootstrap();
   }

   public Collection getMessageProviders()
   {
      return messageProviderMap.values();
   }
}
