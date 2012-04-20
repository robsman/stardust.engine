/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.impl.barrier;

import java.util.Collections;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * <p>
 * Allows to wait for the creation of an activity instance, i.e. until
 * one activitiy instance for a particular process instance is in the 
 * state {@link ActivityInstanceState.Application}.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision: $
 */
public class ActivityInstanceCreatedBarrier extends BarrierTemplate
{
   private final ServiceFactory sf;
   private final long piOid;
   
   /**
    * <p>
    * Initializes an object with the given parameters.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param oid the oid of the process instance that comprises the activity instance to wait for
    */
   public ActivityInstanceCreatedBarrier(final ServiceFactory sf, final long piOid)
   {
      if (sf == null)
      {
         throw new NullPointerException("Service factory must not be null.");
      }
      
      this.sf = sf;
      this.piOid = piOid;
   }
   
   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.barrier.BarrierTemplate#checkCondition()
    */
   @Override
   public ConditionStatus checkCondition()
   {
      try
      {
         final ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAlive();
         aiQuery.where(ProcessInstanceFilter.in(Collections.singleton(piOid)));
         sf.getQueryService().findFirstActivityInstance(aiQuery);
         return ConditionStatus.MET;
      }
      catch (final ObjectNotFoundException e)
      {
         return ConditionStatus.NOT_MET;
      }
   }
   
   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.barrier.BarrierTemplate#getConditionDescription()
    */
   @Override
   public String getConditionDescription()
   {
      return "Waiting for activity instance creation of process instance with oid '" + piOid + "'.";
   }
}
