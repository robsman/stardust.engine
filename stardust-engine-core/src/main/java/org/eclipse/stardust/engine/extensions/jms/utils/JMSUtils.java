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
package org.eclipse.stardust.engine.extensions.jms.utils;

import java.util.*;

import javax.jms.*;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JMSUtils
{
   public static final String TEXT = "TEXT";
   public static final String OBJECT = "OBJECT";

   public static String messageToString(Message message)
   {
      StringBuffer messageString = new StringBuffer("MESSAGE INFO: ");
      try
      {
         messageString.append("Msg Id : " + message.getJMSMessageID() + "; ");
      }
      catch (JMSException e)
      {
         //ignore
      }
      messageString.append("HEADER INFORMATION : ");
      try
      {
         Map headerMap = getHeader(message);
         Iterator headerProps = headerMap.keySet().iterator();
         while (headerProps.hasNext())
         {
            String propertyName = (String) headerProps.next();
            Object value = headerMap.get(propertyName);
            messageString.append(propertyName + "=" + value);
            if (headerProps.hasNext())
            {
               messageString.append(", ");
            }
            else
            {
               messageString.append(";");
            }
         }
      }
      catch (JMSException e)
      {
         //ignore
      }
      messageString.append("BODY INFORMATION : ");
      try
      {
         Map bodyMap = getBody(message);
         Iterator bodyProps = bodyMap.keySet().iterator();
         while (bodyProps.hasNext())
         {
            String propertyName = (String) bodyProps.next();
            Object value = bodyMap.get(propertyName);
            messageString.append(propertyName + "=" + value);
            if (bodyProps.hasNext())
            {
               messageString.append(", ");
            }
            else
            {
               messageString.append(";");
            }
         }
      }
      catch (JMSException e)
      {
         //ignore
      }
      return messageString.toString();
   }

   public static SortedMap getBody(Message message) throws JMSException
   {
      SortedMap map = new TreeMap();

      if (message instanceof MapMessage)
      {
         Enumeration names = ((MapMessage) message).getMapNames();
         while (names.hasMoreElements())
         {
            String mapName = (String) names.nextElement();
            Object value = ((MapMessage) message).getObject(mapName);
            map.put(mapName, value);
         }
      }
      else if (message instanceof TextMessage)
      {
         String text = ((TextMessage) message).getText();
         map.put(TEXT, text);
      }
      else if (message instanceof ObjectMessage)
      {
         Object object = ((ObjectMessage) message).getObject();
         map.put(OBJECT, object);
      }
      else if (message instanceof StreamMessage)
      {
         for (Object object = ((StreamMessage) message).readObject(); object != null;)
         {
            map.put(object.getClass(), object);
         }
      }

      return map;
   }

   public static Map getHeader(Message message) throws JMSException
   {
      HashMap map = new HashMap();
      Enumeration names = message.getPropertyNames();

      while (names.hasMoreElements())
      {
         String propertyName = (String) names.nextElement();
         Object property = message.getObjectProperty(propertyName);
         map.put(propertyName, property);
      }

      return map;
   }
}
