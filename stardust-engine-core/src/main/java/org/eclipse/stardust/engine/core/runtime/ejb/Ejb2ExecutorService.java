/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Florin.Herinean (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.engine.core.runtime.ejb;

import java.util.Map;

import javax.ejb.RemoveException;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.ejb2.beans.LocalForkingService;
import org.eclipse.stardust.engine.api.ejb2.beans.LocalForkingServiceHome;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;

public class Ejb2ExecutorService implements ExecutorService
{
   private static final Logger trace = LogManager.getLogger(EJBForkingService.class);

   private static final String KEY_CACHED_FORKING_SERVICE_HOME = EJBForkingService.class.getName() + ".CachedForkingServiceHome";

   private LocalForkingService inner;

   public Ejb2ExecutorService(LocalForkingService inner)
   {
      this.inner = inner;
   }

   public Ejb2ExecutorService()
   {
      try
      {
         final GlobalParameters globals = GlobalParameters.globals();

         LocalForkingServiceHome home = (LocalForkingServiceHome) globals.get(KEY_CACHED_FORKING_SERVICE_HOME);
         if (null == home)
         {
            InitialContext context = new InitialContext();
            Object rawHome = context.lookup("java:comp/env/ejb/ForkingService");
            home = (LocalForkingServiceHome) PortableRemoteObject.narrow(rawHome,
                  LocalForkingServiceHome.class);

            if (null != home)
            {
               globals.set(KEY_CACHED_FORKING_SERVICE_HOME, home);
            }
         }

         inner = home.create();
      }
      catch (Exception e)
      {
         throw new ExecuterServiceException(e);
      }
   }

   @Override
   public DataSource getDataSource()
   {
      // TODO: implement if needed
      return null;
   }

   @Override
   public Object getRepository()
   {
      // TODO: implement if needed
      return null;
   }

   @Override
   public ExecutorService getForkingService()
   {
      return this;
   }

   @Override
   public void remove()
   {
      // TODO: implement if needed
   }

   @Override
   public LoggedInUser login(String username, String password, Map properties)
   {
      // TODO: implement if needed
      return null;
   }

   @Override
   public void logout()
   {
      // TODO: implement if needed
   }

   @Override
   public QueueConnectionFactory getQueueConnectionFactory()
   {
      final Parameters params = Parameters.instance();
      QueueConnectionFactory factory = params.getObject(JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY);
      if (factory == null)
      {
         throw new InternalException("Reference '"
               + JmsProperties.QUEUE_CONNECTION_FACTORY_PROPERTY + "' is not set.");
      }
      return factory;
   }

   @Override
   public Queue getQueue(String queueName)
   {
      final Parameters params = Parameters.instance();
      Queue queue = params.getObject(queueName);
      if (queue == null)
      {
         throw new InternalException("Reference '" + queueName + "' is not set.");
      }
      return queue;
   }

   @Override
   public Object run(Action<?> action) throws WorkflowException
   {
      return inner.run(action);
   }

   @Override
   public Object run(Action< ? > action, ExecutorService proxyService)
         throws WorkflowException
   {
      return inner.run(action);
   }

   @Override
   public void release()
   {
      if (null != inner)
      {
         try
         {
            inner.remove();
         }
         catch (RemoveException e)
         {
            trace.debug("Failed releasing inner session bean.", e);
         }
      }
   }
}
