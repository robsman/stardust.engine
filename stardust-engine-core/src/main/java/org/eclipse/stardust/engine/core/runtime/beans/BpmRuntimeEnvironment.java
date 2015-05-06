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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.config.TimestampProvider;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.utils.ejb.EjbProperties;
import org.eclipse.stardust.engine.api.dto.RtDetailsFactory;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.persistence.Session;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.runtime.DefaultQueueConnectionProvider;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ExecutionPlan;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.internal.changelog.ChangeLogDigester;
import org.eclipse.stardust.engine.core.runtime.setup.DataClusterRuntimeInfo;
import org.eclipse.stardust.engine.core.runtime.utils.Authorization2Predicate;
import org.eclipse.stardust.engine.core.spi.artifact.ArtifactManager;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluatorRegistry;
import org.eclipse.stardust.engine.core.spi.jca.IJcaResourceProvider;
import org.eclipse.stardust.engine.core.spi.jms.IJmsResourceProvider;
import org.eclipse.stardust.engine.core.spi.jms.IQueueConnectionProvider;



/**
 * @author sauer
 * @version $Revision$
 */
public class BpmRuntimeEnvironment extends PropertyLayer
{
   private static final Logger trace = LogManager.getLogger(BpmRuntimeEnvironment.class);

   private ModelManager modelManager;

   private Session session;

   private IPreferenceStorageManager preferenceStore;

   private ArtifactManager artifactManager;

   private ActivityThreadContext activityThreadContext;

   private TimestampProvider timestampProvider;

   private ChangeLogDigester changeLogDigester;

   private IJcaResourceProvider jcaResourceProvider;

   private IJmsResourceProvider jmsResourceProvider;

   private Map<QueueConnectionFactory, QueueConnection> jmsQueueConnections = Collections.emptyMap();

   private Map<QueueConnection, QueueSession> jmsQueueSessions = Collections.emptyMap();

   private Map<QueueSession, QueueSender> jmsQueueSenders = Collections.emptyMap();

   private Map<IRepositoryInstance, IRepositoryService> repositoryServices = Collections.emptyMap();

   private Map<ConnectionFactory, Connection> jcaConnections = Collections.emptyMap();

   private Authorization2Predicate authorization2Predicate;

   private RtDetailsFactory detailsFactory;

   private Map<String, IModel> modelOverrides;

   private IActivityInstance currentActivityInstance;

   private ExecutionPlan executionPlan;

   private boolean deploymentBeanCreated = false;

   private long authorizedOnBehalfOf = 0;

   private EventBindingRecords eventBindingRecords;

   private ExtendedAccessPathEvaluatorRegistry evaluatorRegistry;

   private DataClusterRuntimeInfo dataClusterRuntimeInfo;

   public BpmRuntimeEnvironment(PropertyLayer predecessor)
   {
      super(predecessor);
   }

   public ModelManager getModelManager()
   {
      return modelManager;
   }

   public void setModelManager(ModelManager modelManager)
   {
      this.modelManager = modelManager;
   }

   public Session getAuditTrailSession()
   {
      return session;
   }

   public void setAuditTrailSession(Session session)
   {
      this.session = session;
   }

   public long getAuthorizedOnBehalfOf()
   {
      return authorizedOnBehalfOf;
   }

   public void setAuthorizedOnBehalfOf(long authorizedOnBehalfOf)
   {
      this.authorizedOnBehalfOf = authorizedOnBehalfOf;
   }

   public ActivityThreadContext getActivityThreadContext()
   {
      return activityThreadContext;
   }

   public void setActivityThreadContext(ActivityThreadContext activityThreadContext)
   {
      this.activityThreadContext = activityThreadContext;
   }

   public Authorization2Predicate getAuthorizationPredicate()
   {
      return authorization2Predicate;
   }

   public void setAuthorizationPredicate(Authorization2Predicate authorization2Predicate)
   {
      this.authorization2Predicate = authorization2Predicate;
   }

   public TimestampProvider getTimestampProvider()
   {
      return timestampProvider;
   }

   public void setTimestampProvider(TimestampProvider timestampProvider)
   {
      this.timestampProvider = timestampProvider;
   }

   public ExecutionPlan getExecutionPlan()
   {
      return executionPlan;
   }

   public void setExecutionPlan(ExecutionPlan executionPlan)
   {
      this.executionPlan = executionPlan;
   }

   public ChangeLogDigester getChangeLogDigester()
   {
      if (null == changeLogDigester)
      {
         this.changeLogDigester = ChangeLogDigester.instance();
      }

      return changeLogDigester;
   }

   public IJmsResourceProvider getJmsResourceProvider()
   {
      return jmsResourceProvider;
   }

   public void setJmsResourceProvider(IJmsResourceProvider jmsResourceProvider)
   {
      this.jmsResourceProvider = jmsResourceProvider;
   }

   public IJcaResourceProvider getJcaResourceProvider()
   {
      return jcaResourceProvider;
   }

   public void setJcaResourceProvider(IJcaResourceProvider jcaResourceProvider)
   {
      this.jcaResourceProvider = jcaResourceProvider;
   }

   public ExtendedAccessPathEvaluatorRegistry getEvaluatorRegistry()
   {
      return evaluatorRegistry;
   }

   public void setEvaluatorRegistry(ExtendedAccessPathEvaluatorRegistry evaluatorRegistry)
   {
      this.evaluatorRegistry = evaluatorRegistry;
   }

   public QueueConnectionFactory retrieveQueueConnectionFactory(String name)
   {
      if (jmsResourceProvider != null)
      {
         return jmsResourceProvider.resolveQueueConnectionFactory(name);
      }
      else
      {
         Parameters params = Parameters.instance();
         return params.getObject(name);
      }
   }

   public QueueConnection retrieveQueueConnection(QueueConnectionFactory factory)
         throws JMSException
   {

      QueueConnection connection = null;

      if ( !jmsQueueConnections.isEmpty())
      {
         connection = (QueueConnection) jmsQueueConnections.get(factory);
      }

      if (null == connection)
      {
         IQueueConnectionProvider queueConnectionProvider = ExtensionProviderUtils.getFirstExtensionProvider(IQueueConnectionProvider.class);

         if (queueConnectionProvider == null)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Could not load a custom QueueConnectionProvider. Will use DefaultQueueConnectionProvider.");
            }
            queueConnectionProvider = new DefaultQueueConnectionProvider();
         }

         connection = queueConnectionProvider.createQueueConnection(factory);

         if (jmsQueueConnections.isEmpty())
         {
            this.jmsQueueConnections = Collections.singletonMap(factory, connection);
         }
         else
         {
            if (1 == jmsQueueConnections.size())
            {
               this.jmsQueueConnections = CollectionUtils.copyMap(jmsQueueConnections);
            }
            jmsQueueConnections.put(factory, connection);
         }
      }

      return connection;
   }

   public QueueSession retrieveQueueSession(QueueConnection connection)
         throws JMSException
   {
      QueueSession session = null;

      if ( !jmsQueueSessions.isEmpty())
      {
         session = (QueueSession) jmsQueueSessions.get(connection);
      }

      if (null == session)
      {
         final Parameters params = Parameters.instance();
         if (params.getString(EjbProperties.SERVER_VENDOR_PROPERTY,
               EjbProperties.WEBLOGIC).compareToIgnoreCase(EjbProperties.WEBLOGIC) == 0)
         {
            // @todo (france, ub): how to handle the transacted WLS case?
            session = connection.createQueueSession(false,
                  javax.jms.Session.AUTO_ACKNOWLEDGE);
         }
         else
         {
            // Specified in EJB Specs, Acknowledge Mode should be 0, if in CMT.
            session = connection.createQueueSession(true, 0);
         }

         if (jmsQueueSessions.isEmpty())
         {
            this.jmsQueueSessions = Collections.singletonMap(connection, session);
         }
         else
         {
            if (1 == jmsQueueSessions.size())
            {
               this.jmsQueueSessions = CollectionUtils.copyMap(jmsQueueSessions);
            }
            jmsQueueSessions.put(connection, session);
         }
      }

      return session;
   }

   public QueueSender retrieveUnidentifiedQueueSender(QueueSession session)
         throws JMSException
   {
      QueueSender sender = null;

      if (null != jmsQueueSenders)
      {
         sender = (QueueSender) jmsQueueSenders.get(session);
      }

      if (null == sender)
      {
         sender = session.createSender(null);

         if (jmsQueueSenders.isEmpty())
         {
            this.jmsQueueSenders = Collections.singletonMap(session, sender);
         }
         else
         {
            if (1 == jmsQueueSenders.size())
            {
               this.jmsQueueSenders = CollectionUtils.copyMap(jmsQueueSenders);
            }
            jmsQueueSenders.put(session, sender);
         }
      }

      return sender;
   }

   public Queue resolveQueue(String name)
   {
      if (jmsResourceProvider != null)
      {
         return jmsResourceProvider.resolveQueue(name);
      }
      else
      {
         Parameters params = Parameters.instance();
         return params.getObject(name);
      }
   }

   public Connection retrieveJcaConnection(final ConnectionFactory connectionFactory) throws ResourceException
   {
      Connection connection = null;

      if ( !jcaConnections.isEmpty())
      {
         connection = jcaConnections.get(connectionFactory);
      }

      if (null == connection)
      {
         connection = connectionFactory.getConnection();

         if (jcaConnections.isEmpty())
         {
            jcaConnections = Collections.singletonMap(connectionFactory, connection);
         }
         else
         {
            if (1 == jcaConnections.size())
            {
               jcaConnections = CollectionUtils.copyMap(jcaConnections);
            }
            jcaConnections.put(connectionFactory, connection);
         }
      }

      return connection;
   }

   public void close()
   {
      detailsFactory = null;

      closeQueueSenders();
      closeQueueSessions();
      closeQueueConnections();

      closeRepositoryServices();
      closeJcaConnections();
   }

   private void closeQueueConnections()
   {
      if ( !jmsQueueConnections.isEmpty())
      {
         for (Iterator i = jmsQueueConnections.values().iterator(); i.hasNext();)
         {
            QueueConnection connection = (QueueConnection) i.next();
            try
            {
               connection.close();
            }
            catch (JMSException jmse)
            {
               trace.warn("Failed closing JMS queue connection.", jmse);
            }
         }

         this.jmsQueueConnections = Collections.EMPTY_MAP;
      }
   }

   private void closeQueueSessions()
   {
      if ( !jmsQueueSessions.isEmpty())
      {
         for (Iterator i = jmsQueueSessions.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Entry) i.next();

            final QueueConnection connection = (QueueConnection) entry.getKey();
            final QueueSession session = (QueueSession) entry.getValue();
            if ( !jmsQueueConnections.containsValue(connection))
            {
               trace.warn("Closing JMS queue session for unknown connection "
                     + connection);

               try
               {
                  session.close();
               }
               catch (JMSException jmse)
               {
                  trace.warn("Failed closing JMS queue session.", jmse);
               }
            }
         }

         this.jmsQueueSessions = Collections.EMPTY_MAP;
      }
   }

   private void closeQueueSenders()
   {
      if ( !jmsQueueSenders.isEmpty())
      {
         for (Iterator i = jmsQueueSenders.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Entry) i.next();

            final QueueSession session = (QueueSession) entry.getKey();
            final QueueSender sender = (QueueSender) entry.getValue();
            if ( !jmsQueueSessions.containsValue(session))
            {
               trace.warn("Closing JMS queue sender for unknown session " + session);

               try
               {
                  sender.close();
               }
               catch (JMSException jmse)
               {
                  trace.warn("Failed closing JMS queue sender.", jmse);
               }
            }
         }

         this.jmsQueueSenders = Collections.EMPTY_MAP;
      }
   }

   public void registerRepositoryService(IRepositoryInstance instance, IRepositoryService service)
   {
      if (repositoryServices.isEmpty())
      {
         this.repositoryServices = Collections.singletonMap(instance, service);
      }
      else
      {
         if (1 == repositoryServices.size())
         {
            this.repositoryServices = CollectionUtils.copyMap(repositoryServices);
         }
         repositoryServices.put(instance, service);
      }
   }

   public IRepositoryService getRepositoryService(IRepositoryInstance instance)
   {
      return repositoryServices.get(instance);
   }

   private void closeRepositoryServices()
   {
      if ( !repositoryServices.isEmpty())
      {
         for (Entry<IRepositoryInstance, IRepositoryService> entry : repositoryServices.entrySet())
         {
            entry.getKey().close(entry.getValue());
         }
      }

      this.repositoryServices = Collections.EMPTY_MAP;
   }

   private void closeJcaConnections()
   {
      if ( !jcaConnections.isEmpty())
      {
         for (final Connection c : jcaConnections.values())
         {
            try
            {
               c.close();
            }
            catch (final ResourceException e)
            {
               trace.warn("Failed closing a JCA connection.", e);
            }
         }

         this.jcaConnections = Collections.emptyMap();
      }
   }

   public RtDetailsFactory getDetailsFactory()
   {
      return detailsFactory;
   }

   public void initDetailsFactory()
   {
      detailsFactory = new RtDetailsFactory();
   }

   public Map<String, IModel> getModelOverrides()
   {
      return modelOverrides;
   }

   public void setModelOverrides(Map<String, IModel> overrides)
   {
      this.modelOverrides = overrides;
   }

   public IPreferenceStorageManager getPreferenceStore()
   {
      return preferenceStore;
   }

   public ArtifactManager getArtifactManager()
   {
      return artifactManager;
   }

   public void setArtifactManager(ArtifactManager artifactManager)
   {
      this.artifactManager = artifactManager;
   }

   public void setPreferenceStore(IPreferenceStorageManager preferenceStore)
   {
      this.preferenceStore = preferenceStore;
   }

   public IActivityInstance getCurrentActivityInstance()
   {
      return currentActivityInstance;
   }

   public void setCurrentActivityInstance(IActivityInstance activityInstance)
   {
      currentActivityInstance = activityInstance;
   }

   public boolean isDeploymentBeanCreated()
   {
      return deploymentBeanCreated;
   }

   public void setDeploymentBeanCreatedt(boolean deploymentBeanCreated)
   {
      this.deploymentBeanCreated = deploymentBeanCreated;
   }

   public EventBindingRecords getEventBindingRecords()
   {
      if (eventBindingRecords == null)
      {
         eventBindingRecords = new EventBindingRecords();
      }
      return eventBindingRecords;
   }

   public DataClusterRuntimeInfo getDataClusterRuntimeInfo()
   {
      return dataClusterRuntimeInfo;
   }

   public void setDataClusterRuntimeInfo(DataClusterRuntimeInfo dataClusterRuntimeInfo)
   {
      this.dataClusterRuntimeInfo = dataClusterRuntimeInfo;
   }

   /**
    * <p>
    * Returns whether the {@link MultipleTryInterceptor} will trigger no additional retry. This may
    * have two reasons
    * <ul>
    *    <li>there's no retry configured</li>
    *    <li>the retry count has been exceeded</li>
    * </ul>
    * </p>
    *
    * @return whether the {@link MultipleTryInterceptor} will trigger no additional retry
    */
   public boolean isLastTry()
   {
      final Integer triesLeft = (Integer) get(MultipleTryInterceptor.TRIES_LEFT_PROPERTY_KEY);
      if (triesLeft == null)
      {
         return true;
      }

      return triesLeft.intValue() <= 0;
   }
}
