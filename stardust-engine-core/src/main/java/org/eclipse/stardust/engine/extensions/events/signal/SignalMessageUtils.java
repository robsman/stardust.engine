/***********************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 ***********************************************************************************/
package org.eclipse.stardust.engine.extensions.events.signal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.jms.MapMessage;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.stardust.common.error.PublicException;

/**
 * <p>
 * Utility class hosting reoccurring <i>JMS Signal Message</i> tasks.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class SignalMessageUtils
{
   public static String getSignalNameFrom(final MapMessage msg)
   {
      try
      {
         return msg.getStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY);
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   public static void setSignalNameOn(final MapMessage msg, final String signalName)
   {
      try
      {
         msg.setStringProperty(SignalMessageAcceptor.BPMN_SIGNAL_PROPERTY_KEY, signalName);
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   public static String createSignalDataHash(final MapMessage msg, final String signalName, final List<String> dataIds)
   {
      try
      {
         final LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();

         for (final String dataId : dataIds)
         {
            data.put(dataId, msg.getObject(dataId));
         }

         return createSignalDataHash(signalName, data);
      }
      catch (final JMSException e)
      {
         // TODO - bpmn-2-events - review exception handling
         throw new PublicException(e.getMessage(), e);
      }
   }

   public static String createSignalDataHash(final String signalName, final LinkedHashMap<String, Object> data)
   {
      final StringBuilder sb = new StringBuilder();
      sb.append(signalName);

      for (final Entry<String, Object> e : data.entrySet())
      {
         sb.append(e.getKey());
         final Object obj = e.getValue();
         sb.append(obj != null ? obj.toString() : null);
      }

      return DigestUtils.md5Hex(sb.toString());
   }
}
