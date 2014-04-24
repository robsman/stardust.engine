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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.BpmValidationError;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.ApplicationValidator;


public class JMSValidator implements ApplicationValidator
{
   public List validate(Map attributes, Map typeAttributes, Iterator accessPoints)
   {
      ArrayList inconsistencies = new ArrayList();
      JMSDirection key = (JMSDirection) attributes.get(PredefinedConstants.TYPE_ATT);
      if (key == null)
      {
         BpmValidationError error = BpmValidationError.APP_PROPERTY_NOT_SET.raise(PredefinedConstants.TYPE_ATT);
         inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
         return inconsistencies;
      }
      if (key.equals(JMSDirection.OUT) || key.equals(JMSDirection.INOUT))
      {
         checkProperty(inconsistencies, attributes, PredefinedConstants.QUEUE_CONNECTION_FACTORY_NAME_PROPERTY);
         checkProperty(inconsistencies, attributes, PredefinedConstants.QUEUE_NAME_PROPERTY);
         checkProperty(inconsistencies, attributes, PredefinedConstants.MESSAGE_PROVIDER_PROPERTY);
         checkProperty(inconsistencies, attributes, PredefinedConstants.REQUEST_MESSAGE_TYPE_PROPERTY);
         // @todo (ub)
         /*if (getAllInAccessPoints().hasNext() == false)
         {
            inconsistencies.add(new Inconsistency("Application '" + application.getId() +
                  "' doesnt have any parameters set for JMS Request type."
                  , application, Inconsistency.WARNING));
         } */
      }
      if (key.equals(JMSDirection.IN) || key.equals(JMSDirection.INOUT))
      {
         checkProperty(inconsistencies, attributes, PredefinedConstants.MESSAGE_ACCEPTOR_PROPERTY);
         checkProperty(inconsistencies, attributes, PredefinedConstants.RESPONSE_MESSAGE_TYPE_PROPERTY);
         // @todo (ub)
         /*
         if (application.getAllOutAccessPoints().hasNext() == false)
         {
            inconsistencies.add(new Inconsistency("Application '" + application.getId() +
                  "' doesnt have any parameters set for JMS Response type."
                  , application, Inconsistency.WARNING));
         }
         */
      }
      ArrayList ids = new ArrayList();
      for (; accessPoints.hasNext();)
      {
         AccessPoint ap = (AccessPoint) accessPoints.next();
         if (StringUtils.isEmpty(ap.getId()))
         {
            BpmValidationError error = BpmValidationError.TRIGG_PARAMETER_HAS_NO_ID.raise();
            inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
         }
         else if (!StringUtils.isValidIdentifier(ap.getId()))
         {
            BpmValidationError error = BpmValidationError.TRIGG_PARAMETER_HAS_INVALID_ID_DEFINED.raise();
            inconsistencies.add(new Inconsistency(error, Inconsistency.WARNING));
         }
         else
         {
            if (ap.getAttribute(PredefinedConstants.JMS_LOCATION_PROPERTY) == null)
            {
               BpmValidationError error = BpmValidationError.TRIGG_NO_LOCATION_FOR_PARAMETER_SPECIFIED.raise(ap.getName());
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }
            String className = (String) ap.getAttribute(
                  PredefinedConstants.CLASS_NAME_ATT);
            try
            {
               Reflect.getClassFromAbbreviatedName(className);
            }
            catch (InternalException e)
            {
               BpmValidationError error = BpmValidationError.TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_CANNOT_BE_FOUND.raise(
                     ap.getName(), className);
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }
            catch (NoClassDefFoundError e)
            {
               BpmValidationError error = BpmValidationError.TRIGG_NO_VALID_TYPE_FOR_PARAMETER_CLASS_COULD_NOT_BE_LOADED.raise(
                     ap.getName(), className);
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }

            String idKey = ap.getDirection().toString() + ":" + ap.getId();
            if (ids.contains(idKey))
            {
               BpmValidationError error = BpmValidationError.APP_DUPLICATE_ID_USED.raise(ap.getName());
               inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
            }
            ids.add(idKey);
         }
      }
      return inconsistencies;
   }

   private void checkProperty(ArrayList inconsistencies, Map attributes, String name)
   {
      Object property = attributes.get(name);
      if (property == null || property.toString().trim().length() == 0)
      {
         BpmValidationError error = BpmValidationError.APP_PROPERTY_NOT_SET.raise(name);
         inconsistencies.add(new Inconsistency(error, Inconsistency.ERROR));
      }
   }
}