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

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.ExpectedFailureException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessDefinitionDetails;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.persistence.PhantomException;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.extensions.jms.trigger.DefaultTriggerMessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.trigger.TriggerMessageAcceptor;
import org.eclipse.stardust.engine.extensions.jms.utils.JMSUtils;



/**
 * @author rsauer
 * @version $Revision$
 */
public class ResponseHandlerImpl extends SecurityContextAwareAction
{
   private static final Logger trace = LogManager.getLogger(ResponseHandlerImpl.class);
   
   private static final String CACHED_JMS_TRIGGERS = ResponseHandlerImpl.class.getName() + ".JmsTriggers";
   
   private static final TriggerMessageAcceptor DEFAULT_TRIGGER_MESSAGE_ACCEPTOR = new DefaultTriggerMessageAcceptor();

   private static final MessageAcceptor DEFAULT_MESSAGE_ACCEPTOR = new DefaultMessageAcceptor();

   private final String source;
   private final Message message;
   
   private final String partitionId;
   private final String userDomainId;

   private List<Trigger> activeTriggers = null;

   public ResponseHandlerImpl(ResponseHandlerCarrier carrier)
   {
      super(carrier);
      
      this.partitionId = carrier.getPartitionId();
      this.userDomainId = carrier.getUserDomainId();
      
      this.source = carrier.getSource();
      this.message = carrier.getMessage();
   }
   
   private void configureRuntimeEnvironment(BpmRuntimeEnvironment rtEnv)
   {
      final Parameters params = Parameters.instance();

      IAuditTrailPartition partition = LoginUtils.findPartition(params, partitionId);
      setPartitionOid(partition.getOID());
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
      rtEnv.setProperty(SecurityProperties.CURRENT_PARTITION_OID, Short.valueOf(getPartitionOid()));

      IUserDomain userDomain = LoginUtils.findUserDomain(params, partition, userDomainId);
      setUserDomainOid(userDomain.getOID());
      rtEnv.setProperty(SecurityProperties.CURRENT_DOMAIN, userDomain);
      rtEnv.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, Long.valueOf(getUserDomainOid()));

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
            if (message.propertyExists(RecordedTimestampProvider.PROP_EVENT_TIME))
            {
               long eventTime = message.getLongProperty(RecordedTimestampProvider.PROP_EVENT_TIME);

               rtEnv.setTimestampProvider(new RecordedTimestampProvider(eventTime));
            }
         }
         catch (JMSException jmse)
         {
            trace.warn("Failed ", jmse);
         }
      }
   }

   public Object execute()
   {
      final BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();

      configureRuntimeEnvironment(rtEnv);

      if (trace.isDebugEnabled())
      {
         trace.debug("Handling response received from source '" + source + "'.");
      }

      AdministrationServiceImpl session = new AdministrationServiceImpl();

      initializeFromModel();

      Match match = findMatchForMessage(message);
      if (null != match)
      {
         match.process(session, message);
      }
      else
      {
         throw new ExpectedFailureException(
               BpmRuntimeError.JMS_NO_MESSAGE_ACCEPTORS_FOUND.raise(JMSUtils
                     .messageToString(message)));
      }
      
      return null;
   }

   private void initializeFromModel()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Bootstrapping trigger acceptors");
      }
      
      String processId = null;
      try
      {
         processId = message.getStringProperty("processID");
      }
      catch (JMSException e)
      {
      }

      IModel findModel = null;
      String namespace = null;         
      if(processId != null)
      {
         if (processId.startsWith("{"))
         {
            QName qname = QName.valueOf(processId);
            namespace = qname.getNamespaceURI(); 
         }         
      }

      if(namespace != null)
      {
         findModel = ModelManagerFactory.getCurrent().findActiveModel(namespace);
      }
      else
      {
         findModel = ModelManagerFactory.getCurrent().findActiveModel();
      }
      
      final IModel model = findModel;
      if (null == model)
      {
         throw new PublicException("No active model found.");
      }
      
      this.activeTriggers = (List<Trigger>) model.getRuntimeAttribute(CACHED_JMS_TRIGGERS);
      if (null == activeTriggers)
      {
         List<ProcessDefinition> processes = DetailsFactory.<ProcessDefinition, ProcessDefinitionDetails>
            createCollection(model.getProcessDefinitions(), IProcessDefinition.class, ProcessDefinitionDetails.class);
         
         this.activeTriggers = newArrayList(processes.size());
         for (ProcessDefinition process : processes)
         {
            for (Trigger trigger : (List<Trigger>) process.getAllTriggers())
            {
               if (PredefinedConstants.JMS_TRIGGER.equals(trigger.getType()))
               {
                  activeTriggers.add(trigger);
               }
            }
         }
         this.activeTriggers = Collections.unmodifiableList(activeTriggers);
         
         model.setRuntimeAttribute(CACHED_JMS_TRIGGERS, activeTriggers);
      }
   }

   private Match findMatchForMessage(Message message)
   {
      Match match = null;

      // try extensions first
      List<MessageAcceptor> customAcceptors = ExtensionProviderUtils.getExtensionProviders(MessageAcceptor.class);
      for (MessageAcceptor acceptor : customAcceptors)
      {
         match = findMatchForMessage(message, acceptor);
         if (null != match)
         {
            break;
         }
      }
      
      if (null == match)
      {
         // no match so far, fall back to default acceptor
         match = findMatchForMessage(message, DEFAULT_MESSAGE_ACCEPTOR);
      }

      if (null == match)
      {
         // still no match, now try JMS triggers
         for (Trigger trigger : activeTriggers)
         {
            match = findMatchForTriggerMessage(message, trigger);
            if (null != match)
            {
               break;
            }
         }
      }
      
      return match;
   }

   private Match findMatchForMessage(Message message, MessageAcceptor acceptor)
   {
      try
      {
         for (Iterator<IActivityInstance> activityItr = acceptor.getMatchingActivityInstances(message); activityItr.hasNext();)
         {
            IActivityInstance activityInstance = activityItr.next();
            try
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Locking activity instance " + activityInstance);
               }

               activityInstance.lock();
               ((ActivityInstanceBean) activityInstance).reloadAttribute(ActivityInstanceBean.FIELD__STATE);
               if (activityInstance.isHibernated())
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Selecting activity instance " + activityInstance);
                  }
                  return new ResponseMatch(acceptor, activityInstance);
               }
               else
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("Skipping already handled activity instance "
                           + activityInstance);
                  }
               }
            }
            catch (ConcurrencyException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Skipping locked activity instance " + activityInstance);
               }
            }
            catch (PhantomException e)
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("Skipping zombie activity instance " + activityInstance);
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.warn("The acceptor " + acceptor + " was not able to get a matching "
               + "criteria. Maybe it is not responsible for the message '"
               + JMSUtils.messageToString(message) + "'." + e.getMessage());
      }

      return null;
   }
   
   private Match findMatchForTriggerMessage(Message message, Trigger trigger)
   {
      TriggerMessageAcceptor acceptor = getAcceptorForTrigger(trigger);

      try
      {
         Map acceptedData = acceptor.acceptMessage(message, trigger);
         if (acceptedData != null)
         {
            return new TriggerMatch(acceptor, trigger, acceptedData);
         }
      }
      catch (Exception e)
      {
         trace.warn("The acceptor " + acceptor + " was not able to get a matching "
               + "criteria.Maybe it is not responsible for the message '"
               + JMSUtils.messageToString(message) + "'.", e);
      }
      
      return null;
   }

   private TriggerMessageAcceptor getAcceptorForTrigger(Trigger trigger)
   {
      return DEFAULT_TRIGGER_MESSAGE_ACCEPTOR;
/*      try
      {
         String acceptorClassName = (String) trigger.getAttribute(
               PredefinedConstants.ACCEPTOR_CLASS_ATT);

         Class acceptorClass = Class.forName(acceptorClassName);

         return (TriggerMessageAcceptor) acceptorClass.newInstance();
      }
      catch (ClassNotFoundException e)
      {
         throw new PublicException("Invalid JMS trigger acceptor.", e);
      }
      catch (InstantiationException e)
      {
         throw new PublicException("Invalid JMS trigger acceptor.", e);
      }
      catch (IllegalAccessException e)
      {
         throw new PublicException("Invalid JMS trigger acceptor.", e);
      }*/
   }

   private interface Match
   {
      void process(AdministrationServiceImpl session, Message message);
   }

   private class ResponseMatch implements Match
   {
      private final MessageAcceptor acceptor;
      private final IActivityInstance activityInstance;

      private ResponseMatch(MessageAcceptor acceptor, IActivityInstance activityInstance)
      {
         this.acceptor = acceptor;
         this.activityInstance = activityInstance;
      }

      public void process(AdministrationServiceImpl session, Message message)
      {
         IApplication application = activityInstance.getActivity().getApplication();
         Assert.isNotNull(application, "Application for receive message not defined");

         if (((JMSDirection) application.
               getAttribute(PredefinedConstants.TYPE_ATT)).isReceiving())
         {
            StringKey id = (StringKey) application.getAttribute(
                  PredefinedConstants.RESPONSE_MESSAGE_TYPE_PROPERTY);
            Iterator outAccessPoints = application.getAllOutAccessPoints();
            Map<String, Object> data = acceptor.getData(message, id, outAccessPoints);

            trace.debug("Executing activity thread for incoming message; hibernated "
                  + "activity instance = " + activityInstance.getOID());

            activityInstance.lock();
            activityInstance.activate();

            ActivityThread at = new ActivityThread(
                  null, null, activityInstance, null, data, false);
            at.run();
         }
         else
         {
            throw new PublicException("Matching " + activityInstance
                  + " found, but it is not of receiving nature.");
         }
      }
   }

   private class TriggerMatch implements Match
   {
      private final Trigger triggerDetails;
      private final Map acceptedData;

      private TriggerMatch(TriggerMessageAcceptor acceptor, Trigger triggerDetails,
            Map acceptedData)
      {
         this.triggerDetails = triggerDetails;
         this.acceptedData = acceptedData;
      }

      public void process(AdministrationServiceImpl session, Message message)
      {
         String processId = triggerDetails.getProcessDefinition().getId();

         IModel model = ModelManagerFactory.getCurrent().findModel(triggerDetails.getModelOID());
         IProcessDefinition processDefinition = model.findProcessDefinition(processId);
         ITrigger trigger = processDefinition.findTrigger(triggerDetails.getId());
         
         if (trace.isDebugEnabled())
         {
            trace.debug("Executing activity thread for incoming message");
         }

         boolean isSync = triggerDetails.isSynchronous();
         
         if ( !isSync)
         {
            String syncFlag = Parameters.instance().getString(Modules.ENGINE + "."
                  + triggerDetails.getId() + "."
                  + org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties.THREAD_MODE,
                  "");
            if (syncFlag.length() != 0)
            {
               isSync = Boolean.valueOf(syncFlag).booleanValue();
            }
         }

         Map startData = TriggerDaemon.performParameterMapping(trigger, acceptedData);
         new WorkflowServiceImpl().startProcess(processDefinition, startData, isSync);
      }
   }
}