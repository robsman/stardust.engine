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
package org.eclipse.stardust.engine.core.runtime.beans.daemons;

import org.eclipse.stardust.common.Action;

/**
 * 
 * @author Florin.Herinean
 * @version $Revision: $
 */
public class DaemonOperationExecutor implements Action
{
   private DaemonOperation operation;
   private DaemonHandler context;

   public DaemonOperationExecutor(DaemonOperation operation, DaemonHandler context)
   {
      this.operation = operation;
      this.context = context;
   }

   public Object execute()
   {
      switch (operation.getType())
      {
      case START:
         context.startTimer(operation.getCarrier());
         break;
      case STOP:
         context.stopTimer(operation.getCarrier());
         break;
      case CHECK:
         operation.setResult(context.checkTimer(operation.getCarrier()));
         break;
      case RUN:
         context.runDaemon(operation.getCarrier());
         break;
      }
      return operation.execute();
   }
}
