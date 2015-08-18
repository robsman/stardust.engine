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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author rsauer
 * @version $Revision$
 */
public abstract class ActivityThreadContextAdapter implements ActivityThreadContext
{
   private List listeners;

   public void addWorkflowEventListener(IWorkflowEventListener listener)
   {
      if (null == listeners)
      {
         this.listeners = new ArrayList();
      }
      if ( !listeners.contains(listener))
      {
         listeners.add(listener);
      }
   }

   public void removeWorkflowEventListener(IWorkflowEventListener listener)
   {
      if ((null != listeners) && listeners.contains(listener))
      {
         listeners.remove(listener);
      }
   }

   protected void fireActivityStarted(IActivityInstance ai)
   {
      if ((null != listeners) && !listeners.isEmpty())
      {
         IWorkflowEventListener[] tmp = (IWorkflowEventListener[]) listeners.toArray(new IWorkflowEventListener[listeners.size()]);
         for (int i = 0; i < tmp.length; i++ )
         {
            tmp[i].startedActivityInstance(ai);
         }
      }
   }

   protected void fireActivityCompleted(IActivityInstance ai)
   {
      if ((null != listeners) && !listeners.isEmpty())
      {
         IWorkflowEventListener[] tmp = (IWorkflowEventListener[]) listeners.toArray(new IWorkflowEventListener[listeners.size()]);
         for (int i = 0; i < tmp.length; i++ )
         {
            tmp[i].completedActivityInstance(ai);
         }
      }
   }
}
