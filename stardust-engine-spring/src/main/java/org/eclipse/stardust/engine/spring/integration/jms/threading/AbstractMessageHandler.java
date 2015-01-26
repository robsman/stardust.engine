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

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.MessageListener;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.rt.IJobManager;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.api.spring.ISpringServiceBean;
import org.eclipse.stardust.engine.core.runtime.beans.ActionCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceJobManager;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserDomain;
import org.eclipse.stardust.engine.core.runtime.beans.UserBean;
import org.eclipse.stardust.engine.core.runtime.beans.UserRealmBean;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.jms.app.RecordedTimestampProvider;
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
   private static final Logger trace = LogManager.getLogger(AbstractMessageHandler.class);
   
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
   
   protected void bootStrapEngine(ActionCarrier carrier, MapMessage mapMessage)
   {
      final short partitionOid = carrier.getPartitionOid();
      final long userDomainOid = carrier.getUserDomainOid();
      final Parameters params = Parameters.instance();
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      
      IAuditTrailPartition partition = LoginUtils.findPartition(params, partitionOid);
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION_OID, partitionOid);

      IUserDomain userDomain = LoginUtils.findUserDomain(params, partition, userDomainOid);
      rtEnv.setProperty(SecurityProperties.CURRENT_DOMAIN, userDomain);
      rtEnv.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, userDomainOid);

      UserRealmBean transientRealm = UserRealmBean.createTransientRealm(
            PredefinedConstants.SYSTEM_REALM, PredefinedConstants.SYSTEM_REALM, partition);
      IUser transientUser = UserBean.createTransientUser(PredefinedConstants.SYSTEM,
            PredefinedConstants.SYSTEM_FIRST_NAME, PredefinedConstants.SYSTEM_LAST_NAME,
            transientRealm);
      rtEnv.setProperty(SecurityProperties.CURRENT_USER, transientUser);

      // optionally taking timestamp override into account
      boolean recordedEventTime = params.getBoolean(
            KernelTweakingProperties.EVENT_TIME_OVERRIDABLE, false);
      
      if (recordedEventTime)
      {
         try
         {
            if (mapMessage.propertyExists(RecordedTimestampProvider.PROP_EVENT_TIME))
            {
               long eventTime = mapMessage.getLongProperty(RecordedTimestampProvider.PROP_EVENT_TIME);
               rtEnv.setTimestampProvider(new RecordedTimestampProvider(eventTime));
            }
         }
         catch (JMSException jmse)
         {
            trace.warn("Failed ", jmse);
         }
      }
   }
}
