/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.repository.jcr;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.vfs.impl.utils.RepositoryHelper;

public class InMemoryInitialContextHolder
{
   private static InitialContext context;

   public synchronized static InitialContext getContext()
   {
      if (context == null)
      {
         Hashtable<Object, Object> env = new Hashtable<Object, Object>();
         env.put(Context.INITIAL_CONTEXT_FACTORY,
               RepositoryHelper.DUMMY_INITIAL_CONTEXT_FACTORY_CLASS_NAME);

         env.put(Context.PROVIDER_URL, "localhost");
         try
         {
            context = new InitialContext(env);
         }
         catch (NamingException e)
         {
            throw new RuntimeException(e);
         }
      }

      return context;
   }
}
