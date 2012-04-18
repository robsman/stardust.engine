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
package org.eclipse.stardust.test.api;

import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;

/**
 * <p>
 * Allows to wait for a process instance state change.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class ProcessInstanceStateBarrier extends BarrierTemplate
{
   private final ServiceFactory sf;
   
   private final long oid;
   private final ProcessInstanceState state;
   
   /**
    * <p>
    * Initializes an object with the given parameters.
    * </p>
    * 
    * @param sf the service factory to use; must not be <code>null</code>
    * @param oid the oid of the process instance to wait for
    * @param state the state to wait for; must not be <code>null</code>
    */
   public ProcessInstanceStateBarrier(final ServiceFactory sf, final long oid, final ProcessInstanceState state)
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
      this.oid = oid;
      this.state = state;
   }
   
   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.BarrierTemplate#checkCondition()
    */
   @Override
   protected ConditionStatus checkCondition()
   {
      final ProcessInstance pi = sf.getWorkflowService().getProcessInstance(oid);
      return state.equals(pi.getState())
         ? ConditionStatus.MET 
         : ConditionStatus.NOT_MET;
   }

   /*
    * (non-Javadoc)
    * @see org.eclipse.stardust.test.api.BarrierTemplate#getConditionDescription()
    */
   @Override
   protected String getConditionDescription()
   {
      return "Waiting for process instance state change to '" + state + "'.";
   }
}
