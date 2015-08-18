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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AccessPoint extends IdentifiableElement
{
   private String direction;
   private boolean browsable;
   private String type;

   public AccessPoint(String id, String name, String clazz, String direction,
         boolean browsable, String userObject, String defaultValue, String type)
   {
      super(id, name, null);
      setAttribute(PredefinedConstants.CLASS_NAME_ATT, clazz);
      this.direction = direction;
      this.browsable = browsable;
      // it's always a jms accesspoint in pre 3.0, so we know about the user object
      if (userObject != null && userObject.length() != 0)
      {
         setAttribute(new Attribute(PredefinedConstants.JMS_LOCATION_PROPERTY,
               "ag.carnot.workflow.spi.providers.applications.jms.JMSLocation",
               userObject));
      }
      if (defaultValue != null && defaultValue.length() != 0)
      {
         setAttribute(PredefinedConstants.DEFAULT_VALUE_ATT, defaultValue);
      }
      this.type = type;
   }

   public boolean isBrowsable()
   {
      return browsable;
   }

   public String getDirection()
   {
      return direction;
   }

   public String getType()
   {
      return type;
   }
}
