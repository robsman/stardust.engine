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
package org.eclipse.stardust.engine.core.runtime.logging;

/**
 * This SQL recorder implementation does nothing.
 * 
 * @author born
 * @version $Revision: $
 *
 */
public final class NullSqlTimeRecorder implements ISqlTimeRecorder
{
   /* (non-Javadoc)
    * @see org.eclipse.stardust.engine.core.runtime.logging.ISqlTimeRecorder#record(java.lang.String, long)
    */
   public void record(String sql, long duration)
   {
      // left empty by intention.
   }

   public void record(long duration)
   {
      // left empty by intention.
   }
   
   public String getUniqueIdentifier()
   {
      // TODO Auto-generated method stub
      return "";
   }
}