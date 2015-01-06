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
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
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
         BpmValidationError error = BpmValidationError.TRIGG_UNSPECIFIED_MESSAGE_TYPE_FOR_JMS_TRIGGER.raise();
         inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
      }
      Iterator<AccessPoint> accessPoints = trigger.getAllAccessPoints();
      while (accessPoints.hasNext())
      {
         AccessPoint ap = accessPoints.next();
         if (StringUtils.isEmpty(ap.getId()))
         {
            BpmValidationError error = BpmValidationError.TRIGG_PARAMETER_HAS_NO_ID.raise();
            inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.ERROR));
         }
         else if (!StringUtils.isValidIdentifier(ap.getId()))
         {
            BpmValidationError error = BpmValidationError.TRIGG_PARAMETER_HAS_INVALID_ID_DEFINED.raise();
            inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.WARNING));
         }
         if (ap.getAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY) == null)
         {
            BpmValidationError error = BpmValidationError.TRIGG_NO_LOCATION_FOR_PARAMETER_SPECIFIED.raise(ap.getId());
            inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.WARNING));
         }
         String clazz = (String) ap.getAttribute(PredefinedConstants.CLASS_NAME_ATT);
         try
         {
            Reflect.getClassFromAbbreviatedName(clazz);
         }
         catch (InternalException e)
         {
            BpmValidationError error = BpmValidationError.TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND.raise(
                  ap.getName(), clazz);
            inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.WARNING));
         }
         catch (NoClassDefFoundError e)
         {
            BpmValidationError error = BpmValidationError.TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED.raise(
                  ap.getName(), clazz);
            inconsistencies.add(new Inconsistency(error, trigger, Inconsistency.WARNING));
         }
      }
      return inconsistencies;
   }
}