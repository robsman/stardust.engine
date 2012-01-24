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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.common.IntKey;

public class ContextKind extends IntKey
{
   private static final long serialVersionUID = 1L;

   /**
    * Context is process instance.
    */
   public static final int PROCESS_INSTANCE = 1;

   /**
    * Context is activity instance.
    */
   public static final int ACTIVITY_INSTANCE = 2;
   
   /**
    * Global related context.
    */
   public static final int GLOBAL = 3;

   public static final ContextKind ProcessInstance = new ContextKind(PROCESS_INSTANCE, "PI");

   public static final ContextKind ActivityInstance = new ContextKind(ACTIVITY_INSTANCE, "AI");
   
   public static final ContextKind Global = new ContextKind(GLOBAL, "GLOBAL");
   
   public static ContextKind get(int value)
   {
      return (ContextKind) getKey(ContextKind.class, value);
   }
   
   protected Object readResolve()
   {
      return super.readResolve();
   }
   
   private ContextKind(int id, String defaultName)
   {
      super(id, defaultName);
   }
}
