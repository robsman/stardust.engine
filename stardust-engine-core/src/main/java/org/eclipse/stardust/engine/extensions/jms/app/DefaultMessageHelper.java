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

import java.util.*;

import javax.jms.*;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DefaultMessageHelper
{
   public static final Logger trace = LogManager.getLogger(DefaultMessageHelper.class);

   //copy them to jmsProperties???
   public static final String PROCESS_INSTANCE_OID_HEADER = "processInstanceOID";
   public static final String ACTIVITY_INSTANCE_OID_HEADER = "activityInstanceOID";
   public static final String PARTITION_ID_HEADER = "carnotPartitionID";
   public static final String PROCESS_ID_HEADER = "processID";
   public static final String ACTIVITY_ID_HEADER = "activityID";
   public static final String DATA_ID_HEADER = "dataID";
   public static final String STR_DATA_VALUE_HEADER = "stringifiedDataValue";
   public static final String SER_DATA_VALUE_HEADER = "serializedDataValue";
   public static final String ACCESS_POINT_ID = "content";

   public static final String PARTITION_ACCESS_POINT_ID = "partition";

   public static boolean hasPredefinedAccessPoints(StringKey messageType)
   {
      return messageType.equals(MessageType.TEXT) || messageType.equals(MessageType.OBJECT);
   }

   public static Collection getIntrinsicAccessPoints(StringKey messageType, Direction direction)
   {
      List intrinsicAccessPoints = null;

      if (messageType.equals(MessageType.TEXT))
      {
         intrinsicAccessPoints = new ArrayList(1);
         AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(ACCESS_POINT_ID,
               ACCESS_POINT_ID, String.class.getName(), direction, false, null);
         ap.setAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY, JMSLocation.BODY);
         intrinsicAccessPoints.add(ap);
      }
      else if (messageType.equals(MessageType.OBJECT) || messageType.equals(MessageType.STREAM))
      {
         intrinsicAccessPoints = new ArrayList(1);
         AccessPoint ap = JavaDataTypeUtils.createIntrinsicAccessPoint(ACCESS_POINT_ID,
               ACCESS_POINT_ID, Object.class.getName(), direction, false, null);
         ap.setAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY, JMSLocation.BODY);
         intrinsicAccessPoints.add(ap);
      }
      return intrinsicAccessPoints;
   }

   public static Collection getMessageIds()
   {
      return StringKey.getKeys(MessageType.class);
   }

   public static Map getData(Message message, Iterator accessPoints)
   {
      Map map = new HashMap();

      while (accessPoints.hasNext())
      {
         String id = null;
         Object location = null;
         Object ap = accessPoints.next();
         if (ap instanceof AccessPoint)
         {
            id = ((AccessPoint) ap).getId();
            location = ((AccessPoint) ap).getAttribute(
                  PredefinedConstants.JMS_LOCATION_PROPERTY);
         }
         else if (ap instanceof org.eclipse.stardust.engine.api.model.AccessPoint)
         {
            id = ((org.eclipse.stardust.engine.api.model.AccessPoint) ap).getId();
            location = ((org.eclipse.stardust.engine.api.model.AccessPoint) ap).getAttribute(
                  PredefinedConstants.JMS_LOCATION_PROPERTY);
         }
         try
         {
            if (JMSLocation.BODY.equals(location))
            {
               if (message instanceof MapMessage)
               {
                  map.put(id,
                        ((MapMessage) message).getObject(id));
               }
               else if (message instanceof StreamMessage)
               {
                  map.put(id, ((StreamMessage) message).readObject());
               }
               else if (message instanceof TextMessage)
               {
                  map.put(id, ((TextMessage) message).getText());
               }
               else if (message instanceof ObjectMessage)
               {
                  map.put(id, ((ObjectMessage) message).getObject());
               }
            }
            else if (JMSLocation.HEADER.equals(location))
            {
               map.put(id, message.getObjectProperty(id));
            }
         }
         catch (JMSException e)
         {
            trace.warn("", e);
            //               throw new InternalException(e);
         }
      }

      return map;
   }
}
