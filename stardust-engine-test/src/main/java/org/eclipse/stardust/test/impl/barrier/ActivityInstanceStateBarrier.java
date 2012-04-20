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

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * <p>
 * Allows to wait for an activity instance state change.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ActivityInstanceStateBarrier extends BarrierTemplate
{
   private final ServiceFactory sf;
   
   private final long aiOid;
   private final ActivityInstanceState state;
   
   /**
    * <p>
    * Initializes an object with the given parameters.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param aiOid the oid of the activity instance to wait for
    * @param state the activity instance state to wait for; must not be <code>null</code>
    */
   public ActivityInstanceStateBarrier(final ServiceFactory sf, final long aiOid, final ActivityInstanceState state)
   {
      if (sf == null)
      {
         throw new NullPointerException("Service factory must not be null.");
      }
      
      if (state == null)
      {
         throw new NullPointerException("State must not be null.");
      }
      
      this.sf = sf;
      this.aiOid = aiOid;
      this.state = state;
   }
   
   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.BarrierTemplate#checkCondition()
    */
   @Override
   protected ConditionStatus checkCondition()
   {
      final ActivityInstance ai = sf.getWorkflowService().getActivityInstance(aiOid);
      return state.equals(ai.getState())
         ? ConditionStatus.MET 
         : ConditionStatus.NOT_MET;
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.BarrierTemplate#checkCondition()
    */
   @Override
   protected String getConditionDescription()
   {
      return "Waiting for activity instance state change to '" + state + "'.";
   }
}
