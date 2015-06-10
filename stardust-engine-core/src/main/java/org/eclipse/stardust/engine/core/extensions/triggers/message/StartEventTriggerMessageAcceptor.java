package org.eclipse.stardust.engine.core.extensions.triggers.message;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.eclipse.stardust.common.Functor;
import org.eclipse.stardust.common.Predicate;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.TransformingListIterator;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ParameterMapping;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.Trigger;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.engine.extensions.jms.app.MessageType;
import org.eclipse.stardust.engine.extensions.jms.trigger.TriggerMessageAcceptor;

public class StartEventTriggerMessageAcceptor implements TriggerMessageAcceptor
{

   private static final Logger trace = LogManager.getLogger(StartEventTriggerMessageAcceptor.class);

   @Override
   public Collection getMessageTypes()
   {
      return Arrays.asList(MessageType.OBJECT, MessageType.TEXT, MessageType.STREAM,
            MessageType.MAP);
   }

   @Override
   public String getName()
   {
      return getClass().getSimpleName();
   }

   @Override
   public boolean hasPredefinedParameters(StringKey messageType)
   {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public Collection getPredefinedParameters(StringKey messageType)
   {
      // TODO Auto-generated method stub
      return null;
   }

   private boolean isMatchingTrigger(Trigger trigger, Message message)
   {
      boolean matches = false;

      if ((message instanceof TextMessage) || (message instanceof ObjectMessage)
            || (message instanceof MapMessage) || (message instanceof StreamMessage))
      {
         try
         {
            String startEventId = message.getStringProperty("StartEventId");
            if (!trigger.getType().equals(PredefinedConstants.JMS_TRIGGER) ||
                  isEmpty(startEventId) || !startEventId.equals(trigger.getId()))
            {
               return false;
            }

            Object messageType = trigger.getAttribute(PredefinedConstants.MESSAGE_TYPE_ATT);
            if (messageType == MessageType.TEXT) {
               return message instanceof TextMessage;
            } else if (messageType == MessageType.OBJECT) {
               return message instanceof ObjectMessage;
            } else if (messageType == MessageType.MAP) {
               return message instanceof MapMessage;
            } else if (messageType == MessageType.STREAM) {
               return message instanceof StreamMessage;
            }

            return true;
         }
         catch (JMSException e)
         {
            trace.warn("Error while accepting trigger message.", e);
         }
      }
      return matches;
   }

   @Override
   public Map acceptMessage(Message message, Trigger trigger)
   {
      if (!isMatchingTrigger(trigger, message))
      {
         return null;
      }
      return DefaultMessageHelper.getData(message,
            new TransformingListIterator(trigger.getAllParameterMappings(), new Functor()
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
