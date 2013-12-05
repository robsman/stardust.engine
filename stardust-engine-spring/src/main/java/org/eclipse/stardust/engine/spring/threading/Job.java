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
package org.eclipse.stardust.engine.spring.threading;

public class Job
{
   public final Runnable runnable;

   public boolean done;

   public Throwable error;

   public Job(Runnable runnable)
   {
      this.runnable = runnable;
   }
   
   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();
      
      sb.append("Job {");
      sb.append("runnable = ").append(runnable).append(", ");
      sb.append("done = ").append(done).append(", ");
      sb.append("error = ").append(error);
      sb.append("}");
      
      return sb.toString();
   }
}