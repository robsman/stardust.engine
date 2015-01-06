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
package org.eclipse.stardust.engine.api.spring;

import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceJobManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ForkingServiceSpringBeanFactory
      implements ForkingServiceFactory, BeanFactoryAware
{
   private BeanFactory beanFactory;

   public ForkingServiceSpringBeanFactory(J2eeContainerType type, BeanFactory beanFactory)
   {
      setBeanFactory(beanFactory);
   }

   public BeanFactory getBeanFactory()
   {
      return beanFactory;
   }

   public void setBeanFactory(BeanFactory beanFactory) throws BeansException
   {
      this.beanFactory = beanFactory;
   }

   public ForkingService get()
   {
      return (ForkingService) beanFactory.getBean(
            SpringConstants.BEAN_ID_FORKING_SERVICE, ForkingService.class);
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
