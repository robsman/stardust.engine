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
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;


/**
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public class DefaultMessageProvider implements MessageProvider, Stateless
{
   private static final Logger trace = LogManager.getLogger(DefaultMessageProvider.class);

   public boolean isStateless()
   {
      return true;
   }

   public Collection getIntrinsicAccessPoints(StringKey messageType)
   {
      return DefaultMessageHelper.getIntrinsicAccessPoints(messageType, Direction.IN);
   }

   public Message createMessage(Session jmsSession, ActivityInstance activityInstance,
         Map accessPoints)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Creating message now");
      }

      Application application = activityInstance.getActivity().getApplication();
      Message message = null;
      try
      {
         Object id = application.getAttribute(
               PredefinedConstants.REQUEST_MESSAGE_TYPE_PROPERTY);

         if (id.equals(MessageType.TEXT))
         {
            message = jmsSession.createTextMessage();
         }
         else if (id.equals(MessageType.MAP))
         {
            message = jmsSession.createMapMessage();
         }
         else if (id.equals(MessageType.OBJECT))
         {
            message = jmsSession.createObjectMessage();
         }
         else if (id.equals(MessageType.STREAM))
         {
            message = jmsSession.createStreamMessage();
         }
         else
         {
            throw new PublicException("Message type for id " + id + " not supported");
         }
         Boolean ioh = (Boolean) application
               .getAttribute(PredefinedConstants.INCLUDE_OID_HEADERS_PROPERTY);
         fillHeader(message, accessPoints, activityInstance, 
               null != ioh ? ioh.booleanValue() : false);
         mergeDefaultSecurityContextToHeader(message);
         fillMessage(message, application, accessPoints);
      }
      catch (JMSException e)
      {
         trace.warn("", e);
         throw new PublicException(e.getMessage());
      }

      return message;
   }

   public String getName()
   {
      return "Default provider";
   }
   
   public boolean hasPredefinedAccessPoints(StringKey selection)
   {
      return DefaultMessageHelper.hasPredefinedAccessPoints(selection);
   }

   public Collection getMessageTypes()
   {
      return DefaultMessageHelper.getMessageIds();
   }

   /**
    * If security context is not set by access points then the current context is set.
    * 
    * @param message
    * @throws JMSException
    */
   private void mergeDefaultSecurityContextToHeader(Message message) throws JMSException
   {
      if (null == message.getStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER))
      {
         message.setStringProperty(DefaultMessageHelper.PARTITION_ID_HEADER,
               SecurityProperties.getPartition().getId());
      }
   }

   private void fillHeader(Message message, Map accessPointValues,
         ActivityInstance activityInstance, boolean includeOidHeaders) throws JMSException
   {
      if (includeOidHeaders)
      {
         message.setLongProperty(DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER,
               activityInstance.getProcessInstanceOID());
         message.setLongProperty(DefaultMessageHelper.ACTIVITY_INSTANCE_OID_HEADER,
               activityInstance.getOID());

         if (trace.isDebugEnabled())
         {
            trace.debug("Header set with OID information");
         }
      }

      fillHeaderByAccesPoint(message, accessPointValues,
            DefaultMessageHelper.PARTITION_ACCESS_POINT_ID,
            DefaultMessageHelper.PARTITION_ID_HEADER);
   }

   /**
    * @param message
    * @param accessPointValues
    * @param accessPointId
    * @param propertyTag
    * @throws JMSException
    */
   private void fillHeaderByAccesPoint(Message message, Map accessPointValues,
         String accessPointId, String propertyTag) throws JMSException
   {
      if (accessPointValues.containsKey(accessPointId))
      {
         Object rawId = accessPointValues.get(accessPointId);
         if (rawId instanceof String)
         {
            String id = (String) rawId;
            message.setStringProperty(propertyTag, id);
         }
         else
         {
            trace.warn(MessageFormat.format(
                  "Access point {0} is not of type {1} but {2}.",
                  new Object[] { accessPointId, String.class.getName(),
                        rawId.getClass().getName() }));
         }
      }
   }

   private void fillMessage(Message message, Application application,
         Map accessPointValues) throws JMSException
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Filling up message");
      }

      List appAccessPoints = application.getAllAccessPoints();
      for (int i = 0; i < appAccessPoints.size(); ++i)
      {
         AccessPoint accessPoint = (AccessPoint) appAccessPoints.get(i);
         if (Direction.OUT == accessPoint.getDirection())
         {
            continue;
         }
         //@todo should we do deal with all cases of long, int ...indiviudally
         if (JMSLocation.BODY.equals(accessPoint.getAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY)))
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("setting body value " + accessPoint.getId() + " ==> "
                     + accessPointValues.get(accessPoint.getId()));
            }

            if (message instanceof MapMessage)
            {
               ((MapMessage) message).setObject(accessPoint.getId(),
                     accessPointValues.get(accessPoint.getId()));
            }
            else if (message instanceof StreamMessage)
            {
               ((StreamMessage) message).writeObject(
                     accessPointValues.get(accessPoint.getId()));
            }
            else if (message instanceof TextMessage)
            {
               ((TextMessage) message).setText(
                     (String) accessPointValues.get(accessPoint.getId()));
            }
            else if (message instanceof ObjectMessage)
            {
               ((ObjectMessage) message).setObject(
                     (Serializable) accessPointValues.get(accessPoint.getId()));
            }
            else
            {
               throw new InternalException(
                     "Message type not supported:" + message.getClass());
            }
         }
         else
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("setting header property " + accessPoint.getId() + " ==> "
                     + accessPointValues.get(accessPoint.getId()));
            }
            message.setObjectProperty(accessPoint.getId(),
                  accessPointValues.get(accessPoint.getId()));
         }
      }
   }
}
