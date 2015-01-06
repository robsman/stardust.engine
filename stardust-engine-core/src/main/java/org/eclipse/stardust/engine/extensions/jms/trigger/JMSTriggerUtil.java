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
import java.util.Map;

import org.eclipse.stardust.common.StringKey;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;


public class JMSTriggerUtil
{
   public String describe(Map attributes)
   {
      String description = "unknown";
      if (attributes.containsKey(PredefinedConstants.MESSAGE_TYPE_ATT))
      {
         StringKey messageType = (StringKey) attributes.get(PredefinedConstants.MESSAGE_TYPE_ATT);
         if (null != messageType)
         {
            description = messageType.getName();
         }
      }
      return description;
   }

   public boolean hasPredefinedParameters(Map attributes)
   {
      boolean hasPredefined = false;

      TriggerMessageAcceptor acceptor = getAcceptor(attributes);
      if ((null != acceptor) && attributes.containsKey(PredefinedConstants.MESSAGE_TYPE_ATT))
      {
         StringKey messageType = (StringKey) attributes.get(PredefinedConstants.MESSAGE_TYPE_ATT);
         hasPredefined = (null == messageType)
               || acceptor.hasPredefinedParameters(messageType);
      }
      return hasPredefined;
   }

   public Collection getPredefinedParameters(Map attributes)
   {
      Collection parameters = null;

      TriggerMessageAcceptor acceptor = getAcceptor(attributes);
      if ((null != acceptor) && attributes.containsKey(PredefinedConstants.MESSAGE_TYPE_ATT))
      {
         StringKey messageType = (StringKey) attributes.get(PredefinedConstants.MESSAGE_TYPE_ATT);
         if (null != messageType)
         {
            parameters = acceptor.getPredefinedParameters(messageType);
         }
         else
         {
            parameters = Collections.EMPTY_LIST;
         }
      }
      return parameters;
   }

   public static final TriggerMessageAcceptor getAcceptor(Map attributes)
   {
      if (attributes.containsKey(PredefinedConstants.ACCEPTOR_CLASS_ATT))
      {
         return (TriggerMessageAcceptor) Reflect.createInstance(
               (String) attributes.get(PredefinedConstants.ACCEPTOR_CLASS_ATT));
      }
      return null;
   }
}
