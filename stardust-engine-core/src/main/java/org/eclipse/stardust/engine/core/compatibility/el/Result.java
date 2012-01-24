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
package org.eclipse.stardust.engine.core.compatibility.el;

public class Result
{
   public static final Result TRUE = new Result("TRUE");
   public static final Result FALSE = new Result("FALSE");
   public static final Result OTHERWISE = new Result("OTHERWISE");

   private String name;

   private Result(String name)
   {
      this.name = name;
   }

   public String toString()
   {
      return name;
   }
}