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
package org.eclipse.stardust.engine.core.monitoring;

import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.runtime.audittrail.management.ProcessInstanceUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListener;
import org.eclipse.stardust.engine.core.spi.persistence.IPersistentListenerAction;



/**
 * 
 * @author thomas.wolfram
 * 
 */
public class ProcessInstancePersistentListener implements IPersistentListener
{
   private static final IPersistentListenerAction[] listenerActions = {new UpdateCriticalityAction()};

   public void updated(Persistent persistent)
   {                  
      ProcessInstanceBean piBean = (ProcessInstanceBean) persistent;

      if (ProcessInstanceUtils.isTransientExecutionScenario(piBean))
      {
         /* for transient process instance execution the criticality feature */
         /* does not make any sense, but decreases performance               */
         return;
      }
      
      for (int i=0; i < listenerActions.length; i++)
      {
         listenerActions[i].execute(piBean);
      }
   }

   public void created(Persistent persistent)
   {
      ProcessInstanceBean piBean = (ProcessInstanceBean) persistent;

      if (ProcessInstanceUtils.isTransientExecutionScenario(piBean))
      {
         /* for transient process instance execution the criticality feature */
         /* does not make any sense, but decreases performance               */
         return;
      }
      
      for (int i=0; i < listenerActions.length; i++)
      {
         listenerActions[i].execute(piBean);
      }
   }
}
