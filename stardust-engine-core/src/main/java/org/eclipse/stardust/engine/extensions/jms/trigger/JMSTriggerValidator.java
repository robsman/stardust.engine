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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidator;
import org.eclipse.stardust.engine.core.spi.extensions.model.TriggerValidatorEx;


public class JMSTriggerValidator implements TriggerValidator, TriggerValidatorEx
{
   public Collection validate(Map attributes, Iterator accessPoints)
   {
      throw new UnsupportedOperationException();
   }
   
   public List validate(ITrigger trigger)
   {
      List inconsistencies = CollectionUtils.newList();
      if (trigger.getAttribute(PredefinedConstants.MESSAGE_TYPE_ATT) == null)
      {
         inconsistencies.add(new Inconsistency("Unspecified message type for JMS trigger",
               trigger, Inconsistency.ERROR));
      }
      Iterator<AccessPoint> accessPoints = trigger.getAllAccessPoints();
      while (accessPoints.hasNext())
      {
         AccessPoint ap = accessPoints.next();
         if (StringUtils.isEmpty(ap.getId()))
         {
            inconsistencies.add(new Inconsistency("Parameter has no id.",
                  trigger, Inconsistency.ERROR));
         }
         else if (!StringUtils.isValidIdentifier(ap.getId()))
         {
            inconsistencies.add(new Inconsistency("Parameter has invalid id defined.",
                  trigger, Inconsistency.WARNING));
         }
         if (ap.getAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY) == null)
         {
            inconsistencies.add(new Inconsistency("No Location specified for parameter: " + ap.getId(),
                  trigger, Inconsistency.WARNING));
         }
         String clazz = (String) ap.getAttribute(PredefinedConstants.CLASS_NAME_ATT);
         try
         {
            Reflect.getClassFromAbbreviatedName(clazz);
         }
         catch (InternalException e)
         {
            inconsistencies.add(new Inconsistency("Please provide a valid Type for Parameter '"
                  + ap.getName() + "' (Class '" + clazz + "' cannot be found).",
                  trigger, Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            inconsistencies.add(new Inconsistency("Please provide a valid Type for Parameter '"
                  + ap.getName() + "' (Class '" + clazz + "' could not be loaded).",
                  trigger, Inconsistency.WARNING));
         }
      }
      return inconsistencies;
   }
}