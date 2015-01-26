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

/**
 * A BigDataHandler implementation that does not write to audit trail,
 * is used for read-only audit trails.
 */
public class TransientBigDataHandler implements BigDataHandler
{

   private Object value;
   
   public Object read()
   {
      return value;
   }

   public void refresh()
   {
   }

   public void write(Object value, boolean forceRefresh)
   {
      this.value = value;
   }

}
