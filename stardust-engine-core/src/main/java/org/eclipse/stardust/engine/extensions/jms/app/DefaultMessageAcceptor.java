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

import java.io.Serializable;
import java.util.*;

import javax.jms.JMSException;
import javax.jms.Message;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.IActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.Match;
import org.eclipse.stardust.engine.extensions.jms.app.ResponseHandlerImpl.ResponseMatch;


/**
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public class DefaultMessageAcceptor implements MessageAcceptor, Stateless
{
   private static final Logger trace = LogManager.getLogger(DefaultMessageAcceptor.class);

   public DefaultMessageAcceptor()
   {
   }

   @Override
   public boolean isStateless()
   {
      return true;
   }

   @Override
   public Collection getAccessPoints(StringKey key)
   {
      return DefaultMessageHelper.getIntrinsicAccessPoints(key, Direction.OUT);
   }

   @Override
   public Iterator<IActivityInstance> getMatchingActivityInstances(Message message)
   {
      try
      {
         @SuppressWarnings("unchecked")
         Enumeration<String> propertyNames = message.getPropertyNames();
         while (propertyNames.hasMoreElements())
         {
            String name = propertyNames.nextElement();

            if (trace.isDebugEnabled())
            {
               trace.debug("header property '" + name + "' ==> '"
                     + message.getObjectProperty(name) + "'");
            }
         }

         if (trace.isDebugEnabled())
         {
            trace.debug("Getting matching activity instance for incoming message");
         }

         if (message.propertyExists(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Using activity instance OID.");
            }

            long activityInstanceOID = message.getLongProperty(
                  DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER);

            return Collections.<IActivityInstance> singletonList(
                  ActivityInstanceBean.findByOID(activityInstanceOID)).iterator();
         }
         else if (message.propertyExists(DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER)
               && message.propertyExists(DefaultMessageHelper.ACTIVITY_ID_HEADER))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Using process instance OID with activity id.");
            }

            long processInstanceOID = message.getLongProperty(
                  DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER);
            String activityID = message.getStringProperty(
                  DefaultMessageHelper.ACTIVITY_ID_HEADER);

            ActivityInstanceQuery query = ActivityInstanceQuery.findForProcessInstance(processInstanceOID);
            query.where(new ActivityFilter(activityID));
            return executeRawQuery(query);
         }
         else if (message.propertyExists(DefaultMessageHelper.PROCESS_ID_HEADER)
               && message.propertyExists(DefaultMessageHelper.ACTIVITY_ID_HEADER)
               && message.propertyExists(DefaultMessageHelper.DATA_ID_HEADER))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Looking up by data.");
            }

            String processID = message.getStringProperty(
                  DefaultMessageHelper.PROCESS_ID_HEADER);
            String activityID = message.getStringProperty(
                  DefaultMessageHelper.ACTIVITY_ID_HEADER);
            String dataID = message.getStringProperty(
                  DefaultMessageHelper.DATA_ID_HEADER);

            Object dataValue = null;
            if (message.propertyExists(DefaultMessageHelper.STR_DATA_VALUE_HEADER))
            {
               dataValue = message.getObjectProperty(
                     DefaultMessageHelper.STR_DATA_VALUE_HEADER);
            }
            else if (message.propertyExists(
                  DefaultMessageHelper.SER_DATA_VALUE_HEADER))
            {
               dataValue = message.getObjectProperty(
                     DefaultMessageHelper.SER_DATA_VALUE_HEADER);
            }

            if (dataValue != null)
            {
               if (dataValue instanceof Serializable)
               {
                  ActivityInstanceQuery query = findInStateHavingData(processID,
                        activityID, dataID, (Serializable) dataValue,
                        ActivityInstanceState.Hibernated);

                  return executeRawQuery(query);
               }
            }
         }
      }
      catch (JMSException e)
      {
         throw new PublicException(e);
      }

      // no matches

      return Collections.EMPTY_LIST.iterator();
   }
   
   /**
    * 
    * @param processID
    * @param activityID
    * @param dataID
    * @param dataValue
    * @param activityState
    * @return
    */
   private ActivityInstanceQuery findInStateHavingData(String processID,
         String activityID, String dataID, Serializable dataValue,
         ActivityInstanceState activityState)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(activityState);
      query.where(ActivityFilter.forProcess(activityID, processID));
      query.where(DataFilter.isEqual(dataID, dataValue));
      
      return query;
   }

   private Iterator<IActivityInstance> executeRawQuery(ActivityInstanceQuery query)
   {
      ResultIterator rawResult = new ActivityInstanceQueryEvaluator(query,
            new EvaluationContext(ModelManagerFactory.getCurrent(), null))
            .executeFetch();
      try
      {
         RawQueryResult filteredResult = ProcessQueryPostprocessor
               .findMatchingActivityInstances(query, rawResult);

         List<IActivityInstance> matchingInstances = CollectionUtils.newList();
         for (Iterator<IActivityInstance> i = filteredResult.iterator(); i.hasNext();)
         {
            matchingInstances.add(i.next());
         }
         return matchingInstances.iterator();
      }
      finally
      {
         rawResult.close();
      }
   }

   @Override
   public Map<String, Object> getData(Message message, StringKey id, Iterator accessPoints)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Getting data from incoming message");
      }

      return DefaultMessageHelper.getData(message, accessPoints);
   }

   @Override
   public String getName()
   {
      return "Default acceptor";
   }

   @Override
   public boolean hasPredefinedAccessPoints(StringKey id)
   {
      return DefaultMessageHelper.hasPredefinedAccessPoints(id);
   }

   @Override
   public Collection getMessageTypes()
   {
      return DefaultMessageHelper.getMessageIds();
   }

   @Override
   public Match finalizeMatch(IActivityInstance activityInstance)
   {
      if (activityInstance.isHibernated()) {
         return new ResponseMatch(this, activityInstance);
      }
      return null;
   }

   @Override
   public List<Match> getTriggerMatches(Message message)
   {
      return Collections.emptyList();
   }

   @Override
   public List<Match> getMessageStoreMatches(Message message)
   {
      return Collections.emptyList();
   }
}
