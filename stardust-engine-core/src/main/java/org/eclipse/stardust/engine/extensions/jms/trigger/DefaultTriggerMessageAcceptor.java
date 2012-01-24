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
package org.eclipse.stardust.engine.extensions.jms.trigger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.*;

import org.eclipse.stardust.common.*;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ParameterMapping;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;


public class DefaultTriggerMessageAcceptor implements TriggerMessageAcceptor, Stateless
{
   private static final Logger trace = LogManager.getLogger(DefaultTriggerMessageAcceptor.class);

   private final String PROCESS_ID_HEADER = "processID";
   private final String ACTIVITY_ID_HEADER = "activityID";
   
   private static final List ALL_DEFAULT_MESSAGE_TYPES = Collections.unmodifiableList(
         CollectionUtils.newList(DefaultMessageHelper.getMessageIds())); 

   public boolean isStateless()
   {
      return true;
   }

   public Collection getMessageTypes()
   {
      return ALL_DEFAULT_MESSAGE_TYPES;
   }

   public String getName()
   {
      return "Default trigger acceptor";
   }

   public boolean hasPredefinedParameters(StringKey messageType)
   {
      return DefaultMessageHelper.hasPredefinedAccessPoints(messageType);
   }

   public Collection getPredefinedParameters(StringKey messageType)
   {
      return DefaultMessageHelper.getIntrinsicAccessPoints(messageType, Direction.OUT);
   }

   private boolean isMatchingTrigger(Trigger trigger, Message message)
   {
      boolean matches = false;

      if ((message instanceof TextMessage) || (message instanceof ObjectMessage)
            || (message instanceof MapMessage) || (message instanceof StreamMessage))
      {
         try
         {
            if (message.getStringProperty(ACTIVITY_ID_HEADER) != null)
            {
               return false;
            }
            String sentProcessId = message.getStringProperty(PROCESS_ID_HEADER);

            if (sentProcessId == null)
            {
               return false;
            }
            if (trace.isDebugEnabled())
            {
               trace.debug("testing trigger message with PID " + sentProcessId);
            }

            if (sentProcessId.startsWith("{"))
            {
               if (CompareHelper.areEqual(sentProcessId, trigger.getProcessDefinition().getQualifiedId()))
               {
                  return true;
               }
            }
            matches = sentProcessId.equals(trigger.getProcessDefinition().getId());
         }
         catch (JMSException e)
         {
            trace.warn("Error while accepting trigger message.", e);
         }
      }
      return matches;
   }

   public Map acceptMessage(Message message, Trigger trigger)
   {
      if ( !isMatchingTrigger(trigger, message))
      {
         return null;
      }
      return DefaultMessageHelper.getData(message, new TransformingListIterator(
            trigger.getAllParameterMappings(), new Functor()
            {
               public Object execute(Object source)
               {
                  return ((ParameterMapping) source).getParameter();
               }
            }, new Predicate()
            {
               public boolean accept(Object o)
               {
                  return o != null;
               }
            }));
   }
}
