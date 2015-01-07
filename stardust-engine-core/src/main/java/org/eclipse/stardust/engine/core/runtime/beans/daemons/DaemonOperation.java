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
public class DaemonOperation implements Action
{
   public static enum Type
   {
      START, STOP, CHECK, RUN
   }
   
   private Type type;
   private DaemonCarrier carrier;
   private Object result;
   
   public DaemonOperation(Type type, DaemonCarrier carrier)
   {
      this.type = type;
      this.carrier = carrier;
   }

   public Type getType()
   {
      return type;
   }

   public DaemonCarrier getCarrier()
   {
      return carrier;
   }

   public void setResult(Object result)
   {
      this.result = result;
   }

   public Object execute()
   {
      return result;
   }
}
