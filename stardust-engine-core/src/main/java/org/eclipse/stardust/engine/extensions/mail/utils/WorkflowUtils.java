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
package org.eclipse.stardust.engine.extensions.mail.utils;

/**
 * @author rsauer
 * @version $Revision$
 */
public class WorkflowUtils
{
   public boolean getTrue()
   {
      return true;
   }

   public boolean getFalse()
   {
      return false;
   }

   public String copy(String value)
   {
      return value;
   }
   
   public long copy(long value)
   {
      return value;
   }
   
   public int copy(int value)
   {
      return value;
   }

   public double copy(double value)
   {
      return value;
   }
   
   public int increment(int value)
   {
      return value + 1;
   }

   public int decrement(int value)
   {
      return value - 1;
   }

   public int add(int lhs, int rhs)
   {
      return lhs + rhs;
   }

   public int subtract(int lhs, int rhs)
   {
      return lhs - rhs;
   }
   
   public void fail(boolean doFail)
   {
      if (doFail)
      {
         throw new RuntimeException("Failed by request.");
      }
   }
}
