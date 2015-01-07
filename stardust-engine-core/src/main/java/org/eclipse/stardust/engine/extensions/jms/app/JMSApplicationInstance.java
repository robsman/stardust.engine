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

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.LogUtils;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.IActivity;
import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ApplicationInvocationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.StatelessAsynchronousApplicationInstance;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;



/**
 * Implementation of ApplicationInstance for the JMS application type
 */
public class JMSApplicationInstance implements StatelessAsynchronousApplicationInstance
{
   private static final Logger trace = LogManager.getLogger(JMSApplicationInstance.class);

   private static final String CACHED_CONNECTION_FACTORY = JMSApplicationInstance.class.getName()
         + ".CachedConnectionFactory";

   private static final String CACHED_QUEUE = JMSApplicationInstance.class.getName()
         + ".CachedQueue";

   private static final String CACHED_MSG_PROVIDER = JMSApplicationInstance.class.getName()
         + ".CachedMessageProvider";

   public ApplicationInvocationContext bootstrap(ActivityInstance activityInstance)
   {
      JmsInvocationContext jmsContext = new JmsInvocationContext(activityInstance);

      IActivity activity = null;
      ModelManager modelManager = ModelManagerFactory.getCurrent();

      if (null != modelManager)
      {
         activity = modelManager.findActivity(activityInstance.getModelOID(),
               activityInstance.getActivity().getRuntimeElementOID());
      }

      if (null != activity)
      {
         jmsContext.application = activity.getApplication();

         jmsContext.msgProvider = (MessageProvider) activity.getApplication()
               .getRuntimeAttribute(CACHED_MSG_PROVIDER);
      }

      if (null == jmsContext.msgProvider)
      {
         String messageProviderClass = (String) activityInstance.getActivity()
               .getApplication()
               .getAttribute(PredefinedConstants.MESSAGE_PROVIDER_PROPERTY);
         // can be empty for non-producer JMS applications
         if ( !StringUtils.isEmpty(messageProviderClass))
         {
            jmsContext.msgProvider = (MessageProvider) Reflect.createInstance(messageProviderClass);

            if ((jmsContext.msgProvider instanceof Stateless)
                  && ((Stateless) jmsContext.msgProvider).isStateless()
                  && (null != activity))
            {
               activity.getApplication().setRuntimeAttribute(CACHED_MSG_PROVIDER,
                     jmsContext.msgProvider);
            }
         }
      }

      return jmsContext;
   }

   public void send(ApplicationInvocationContext c) throws InvocationTargetException
   {
      JmsInvocationContext jmsContext = (JmsInvocationContext) c;

      if (trace.isDebugEnabled())
      {
         trace.debug("Ready to send message");
      }

      try
      {
         // @todo/hiob (ub) get from the application
         connectJmsResources(jmsContext);

         final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

         // sender, session and connection will be closed by RT Environment at end of TX
         final QueueConnection connection = rtEnv.retrieveQueueConnection(jmsContext.queueConnectionFactory);
         final QueueSession session = rtEnv.retrieveQueueSession(connection);
         final QueueSender sender = rtEnv.retrieveUnidentifiedQueueSender(session);

         Message message = jmsContext.msgProvider.createMessage(session,
               jmsContext.getActivityInstance(), jmsContext.inAccessPointValues);

         sender.send(jmsContext.queue, message);

         if (trace.isDebugEnabled())
         {
            trace.debug("Message sent");
         }
      }
      catch (Exception e)
      {
         LogUtils.traceException(e, true);
      }
   }

   public Map receive(ApplicationInvocationContext c, Map data, Iterator outDataTypes)
   {
      // @todo optimize
      return data;
   }

   public boolean isSending(ApplicationInvocationContext c)
   {
      JmsInvocationContext jmsContext = (JmsInvocationContext) c;

      Application application = jmsContext.getActivityInstance().getActivity().getApplication();
      return ((JMSDirection) application.
            getAttribute(PredefinedConstants.TYPE_ATT)).isSending();
   }

   public boolean isReceiving(ApplicationInvocationContext c)
   {
      JmsInvocationContext jmsContext = (JmsInvocationContext) c;

      Application application = jmsContext.getActivityInstance().getActivity().getApplication();
      return ((JMSDirection) application.
            getAttribute(PredefinedConstants.TYPE_ATT)).isReceiving();
   }

   public void setInAccessPointValue(ApplicationInvocationContext c, String name, Object value)
   {
      JmsInvocationContext jmsContext = (JmsInvocationContext) c;

      jmsContext.inAccessPointValues.put(name, value);
   }

   public Object getOutAccessPointValue(ApplicationInvocationContext c, String name)
   {
      JmsInvocationContext jmsContext = (JmsInvocationContext) c;

      return jmsContext.outAccessPointValues.get(name);
   }

   public void cleanup(ApplicationInvocationContext c)
   {
   }

   private void connectJmsResources(JmsInvocationContext jmsContext)
         throws NamingException
   {
      if ((null == jmsContext.queueConnectionFactory) && (null == jmsContext.queue))
      {
         // TODO port to transient attributes

         if (null != jmsContext.application)
         {
            jmsContext.queueConnectionFactory = (QueueConnectionFactory) jmsContext.application.getRuntimeAttribute(CACHED_CONNECTION_FACTORY);
            jmsContext.queue = (Queue) jmsContext.application.getRuntimeAttribute(CACHED_QUEUE);
         }

         if ((null == jmsContext.queueConnectionFactory) || (null == jmsContext.queue))
         {
            Map properties = jmsContext.getActivityInstance().getActivity()
                  .getApplication()
                  .getAllAttributes();
            String queueConnectionFactoryJNDI = (String) properties.get(
                  PredefinedConstants.QUEUE_CONNECTION_FACTORY_NAME_PROPERTY);
            String queueJNDI = (String) properties.get(
                  PredefinedConstants.QUEUE_NAME_PROPERTY);

            if (trace.isDebugEnabled())
            {
               trace.debug("Application configured with factory "
                     + queueConnectionFactoryJNDI + " and queue " + queueJNDI);
            }

            BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

            IJmsResourceProvider jmsResourceProvider = rtEnv.getJmsResourceProvider();
            if (null != jmsResourceProvider)
            {
               jmsContext.queueConnectionFactory = jmsResourceProvider.resolveQueueConnectionFactory(queueConnectionFactoryJNDI);
               jmsContext.queue = jmsResourceProvider.resolveQueue(queueJNDI);
            }
            else
            {
               Context jndiContext = new InitialContext();
               jmsContext.queueConnectionFactory = (QueueConnectionFactory) jndiContext.lookup(queueConnectionFactoryJNDI);
               jmsContext.queue = (Queue) jndiContext.lookup(queueJNDI);
            }

            if (null != jmsContext.application)
            {
               jmsContext.application.setRuntimeAttribute(CACHED_CONNECTION_FACTORY, jmsContext.queueConnectionFactory);
               jmsContext.application.setRuntimeAttribute(CACHED_QUEUE, jmsContext.queue);
            }
         }
      }
   }

   static class JmsInvocationContext extends ApplicationInvocationContext
   {
      private IApplication application;

      private MessageProvider msgProvider;

      private Map inAccessPointValues = CollectionUtils.newMap();
      private Map outAccessPointValues = CollectionUtils.newMap();

      private QueueConnectionFactory queueConnectionFactory = null;
      private Queue queue = null;

      public JmsInvocationContext(ActivityInstance ai)
      {
         super(ai);
      }
   }

}
