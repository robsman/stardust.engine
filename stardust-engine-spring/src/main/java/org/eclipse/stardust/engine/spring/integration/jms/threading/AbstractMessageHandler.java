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
package org.eclipse.stardust.engine.spring.integration.jms.threading;

import javax.jms.MessageListener;

import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.spring.ISpringServiceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceJobManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * @author sauer
 * @version $Revision: $
 */
public abstract class AbstractMessageHandler
      implements MessageListener, ISpringServiceBean,
      ApplicationContextAware, BeanFactoryAware
{

   private final ForkingServiceFactory embeddedForkingServiceFactory = new EmbeddedForkingServiceFactory();
   
   private ApplicationContext applicationContext;
   
   private BeanFactory beanFactory;

   private PlatformTransactionManager transactionManager;

   private ForkingService forkingService;
   
   public ApplicationContext getApplicationContext()
   {
      return applicationContext;
   }

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.applicationContext = applicationContext;
   }

   public BeanFactory getBeanFactory()
   {
      return beanFactory;
   }

   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
   {
      this.beanFactory = beanFactory;
   }

   public PlatformTransactionManager getTransactionManager()
   {
      return transactionManager;
   }

   public void setTransactionManager(PlatformTransactionManager transactionManager)
   {
      this.transactionManager = transactionManager;
   }

   public ForkingService getForkingService()
   {
      return forkingService;
   }
   
   public void setForkingService(ForkingService forkingService)
   {
      this.forkingService = forkingService;
   }
   
   public ForkingServiceFactory getForkingServiceFactory()
   {
      return embeddedForkingServiceFactory;
   }

   private class EmbeddedForkingServiceFactory implements ForkingServiceFactory
   {
      public ForkingService get()
      {
         return forkingService;
      }

      public IJobManager getJobManager()
      {
         return new ForkingServiceJobManager(get());
      }

      public void release(ForkingService service)
      {
         // nothing to be done
      }

      public void release(IJobManager jobManager)
      {
         if (jobManager instanceof ForkingServiceJobManager)
         {
            release(((ForkingServiceJobManager) jobManager).getForkingService());
         }
      }
   }
}
