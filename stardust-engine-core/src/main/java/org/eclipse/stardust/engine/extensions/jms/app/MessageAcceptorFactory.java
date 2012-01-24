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

import org.eclipse.stardust.common.error.InternalException;


/**
 * Bootstraps all possible message acceptors.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class MessageAcceptorFactory
{

   private static MessageAcceptorFactory instance;

   private HashMap messageAcceptorMap = new HashMap();

   private MessageAcceptorFactory()
   {
      messageAcceptorMap.put(DefaultMessageAcceptor.class.getName(),
            new DefaultMessageAcceptor());
   }

   public MessageAcceptor getMessageAcceptor(String acceptorName)
   {
      if (messageAcceptorMap.containsKey(acceptorName))
      {
         return (MessageAcceptor) messageAcceptorMap.get(acceptorName);
      }
      else
      {
         throw new InternalException("Unknown message acceptor : " + acceptorName);
      }
   }

   /**
    * Bootstraps the <code>MessageAcceptorFactory</code> singleton.
    */
   public static MessageAcceptorFactory bootstrap()
   {
      if (instance == null)
      {
         instance = new MessageAcceptorFactory();
      }
      return instance;
   }

   public static MessageAcceptorFactory instance()
   {
      return bootstrap();
   }

   public Collection getMessageAcceptors()
   {
      return messageAcceptorMap.values();
   }
}
