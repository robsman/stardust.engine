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
 * This interface provides methods for storing SQL statements with some additional information.
 * This information can later be used for logging purposes.
 * 
 * @author born
 * @version $Revision: $
 */
public interface ISqlTimeRecorder
{
   public static final String PRP_SQL_TIME_RECORDER = ISqlTimeRecorder.class.getName();
   
   /**
    * Records a SQL statement with its duration for execution.
    * 
    * @param sql the SQL statement
    * @param duration the duration for execution in milliseconds.
    */
   void record(String sql, long duration);
   
   /**
    * Records SQL related durations like fetching time etc.
    * @param duration
    */
   void record(long duration);
   
   /**
    * Returns a unique identifier for the instance on sql time recorder.
    * 
    * @return a unique identifier.
    */
   String getUniqueIdentifier();
}