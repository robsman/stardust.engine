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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author rsauer
 * @version $Revision$
 */
public class RollbackActionManager implements Serializable
{
   private static final Logger trace = LogManager.getLogger(RollbackActionManager.class);
   
   public static final String PARAM_ID = RollbackActionManager.class.getName();
   
   private List actions;
   
   public void addRollbackAction(Action action)
   {
      if (null == actions)
      {
         this.actions = new ArrayList();
      }
      actions.add(action);
   }
   
   public void performRollbackActions()
   {
      if (null != actions)
      {
         for (Iterator i = actions.iterator(); i.hasNext();)
         {
            Action action = (Action) i.next();
            try
            {
               action.execute();
            }
            catch (Throwable t)
            {
               trace.warn("Error while performing rollback action.", t);
            }
         }
      }
   }

   public void reset()
   {
      this.actions = null;
   }
   
}
