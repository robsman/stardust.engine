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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.runtime.beans.DetailsFactory;


// @todo (france, ub): revise the use case. It is not the typical one

/**
 * A client side view of a process trigger.
 * <p/>
 * Client side views of CARNOT model and runtime objects are exposed to a client as
 * readonly detail objects which contain a copy of the state of the corresponding server
 * object.
 * <p/>
 */
public class TriggerDetails extends AuditTrailModelElementDetails implements Trigger
{
   private static final long serialVersionUID = -5027343144153114464L;

   private ProcessDefinition processDefinition;

   private final String type;

   private final boolean isSynchronous;

   private final List accessPoints;

   private final List parameterMappings;

   TriggerDetails(ProcessDefinition processDefinition, ITrigger trigger)
   {
      super(trigger);
      this.processDefinition = processDefinition;

      this.type = trigger.getType().getId();

      this.isSynchronous = trigger.isSynchronous();

      this.accessPoints = DetailsFactory.createCollection(trigger.getAllAccessPoints(),
            org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint.class, AccessPointDetails.class);

      this.parameterMappings = DetailsFactory.createCollection(trigger.getAllParameterMappings(),
            IParameterMapping.class, ParameterMappingDetails.class);
   }

   public ProcessDefinition getProcessDefinition()
   {
      return processDefinition;
   }

   public String getType()
   {
      return type;
   }

   public boolean isSynchronous()
   {
      return isSynchronous;
   }

   public List getAllAccessPoints()
   {
      return Collections.unmodifiableList(accessPoints);
   }

   public AccessPoint getAccessPoint(String id)
   {
      AccessPoint accessPoint = null;
      for (Iterator apItr = accessPoints.iterator(); apItr.hasNext();)
      {
         AccessPoint ap = (AccessPoint) apItr.next();
         if (ap.getId().equals(id))
         {
            accessPoint = ap;
            break;
         }
      }
      return accessPoint;
   }

   public List getAllParameterMappings()
   {
      return Collections.unmodifiableList(parameterMappings);
   }
}
