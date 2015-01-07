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
package org.eclipse.stardust.engine.api.dto;

import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.AccessPoint;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;


/**
 * An <code>AccessPoint</code> is a modelling element where an Application provides
 * access to it's data.
 * <p/>
 * It is used as the endpoint for a data mapping. Basically it exposes
 * a java type to be the end point of a data mapping. Every application keeps a map of all
 * access points identified by the <code>id</code> attribute.
 * 
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public class AccessPointDetails
      implements AccessPoint
{

   private static final long serialVersionUID = -1838033734838701552L;
   private Direction direction;
   private String accessPathEvaluatorClass;
   private String name;
   private String id;
   private Map attributes;

   /**
    * @param accessPoint The access point to initialize from.
    */
   AccessPointDetails(org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint accessPoint)
   {
      this.id = accessPoint.getId();
      this.name = accessPoint.getName();
      this.direction = accessPoint.getDirection();
      accessPathEvaluatorClass = accessPoint.getType().getStringAttribute(
            PredefinedConstants.EVALUATOR_CLASS_ATT);
      attributes = accessPoint.getAllAttributes();
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public Map getAllAttributes()
   {
      return attributes;
   }

   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   public Direction getDirection()
   {
      return direction;
   }

   public String toString()
   {
      return getId();
   }

   public boolean equals(Object object)
   {
      if (object instanceof AccessPoint)
      {
         AccessPoint target = (AccessPoint) object;

         if ((getName().equals(target.getName()))
               && (getId().equals(target.getId()))
               && (getDirection() == target.getDirection()))
         {
            return true;
         }
      }
      return false;
   }

   public String getAccessPathEvaluatorClass()
   {
      return accessPathEvaluatorClass;
   }
}
