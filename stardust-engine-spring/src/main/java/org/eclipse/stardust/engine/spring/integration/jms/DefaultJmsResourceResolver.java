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
package org.eclipse.stardust.engine.spring.integration.jms;

import java.util.Iterator;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;



/**
 * @author sauer
 * @version $Revision: $
 */
public class DefaultJmsResourceResolver
      implements IJmsResourceProvider, ApplicationContextAware, InitializingBean
{
   
   public static final String COMP_ENV_PREFIX = "java:comp/env/";

   private ApplicationContext appContext;
   
   private Map/*<String, QueueConnectionFactory>*/ queueConnectionFactories;

   private Map/*<String, Queue>*/ queues;

   public QueueConnectionFactory resolveQueueConnectionFactory(String name)
   {
      if ( !StringUtils.isEmpty(name) && name.startsWith(COMP_ENV_PREFIX))
      {
         name = name.substring(COMP_ENV_PREFIX.length());
      }
      
      return (null != queueConnectionFactories)
            ? (QueueConnectionFactory) queueConnectionFactories.get(name)
            : null;
   }

   public Queue resolveQueue(String name)
   {
      if ( !StringUtils.isEmpty(name) && name.startsWith(COMP_ENV_PREFIX))
      {
         name = name.substring(COMP_ENV_PREFIX.length());
      }
      
      return (null != queues) ? (Queue) queues.get(name) : null;
   }
   
   public Map getQueueConnectionFactories()
   {
      return queueConnectionFactories;
   }

   public void setQueueConnectionFactories(Map queueConnectionFactories)
   {
      this.queueConnectionFactories = CollectionUtils.copyMap(queueConnectionFactories);
   }

   public Map getQueues()
   {
      return queues;
   }

   public void setQueues(Map queues)
   {
      this.queues = CollectionUtils.copyMap(queues);
   }

   public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException
   {
      this.appContext = applicationContext;
   }

   public void afterPropertiesSet() throws Exception
   {
      // automatically discover queue connection factory bindings
      Map qcfBindings = appContext.getBeansOfType(JmsResourceRefBinding.class);
      if ( !CollectionUtils.isEmpty(qcfBindings))
      {
         if (null == queueConnectionFactories)
         {
            this.queueConnectionFactories = CollectionUtils.newMap();
         }
         
         for (Iterator i = qcfBindings.values().iterator(); i.hasNext();)
         {
            JmsResourceRefBinding binding = (JmsResourceRefBinding) i.next();
            
            ConnectionFactory resRef = binding.getResourceRef();
            
            if ((resRef instanceof QueueConnectionFactory)
                  && !queueConnectionFactories.containsKey(binding.getName()))
            {
               queueConnectionFactories.put(binding.getName(), resRef);
            }
         }
      }

      // automatically discover queue bindings
      Map queueBindings = appContext.getBeansOfType(JmsResourceBinding.class);
      if ( !CollectionUtils.isEmpty(queueBindings))
      {
         if (null == queues)
         {
            this.queues = CollectionUtils.newMap();
         }
         
         for (Iterator i = queueBindings.values().iterator(); i.hasNext();)
         {
            JmsResourceBinding binding = (JmsResourceBinding) i.next();
            
            Destination res = binding.getResource();
            
            if ((res instanceof Queue) && !queues.containsKey(binding.getName()))
            {
               queues.put(binding.getName(), res);
            }
         }
      }
   }

}
