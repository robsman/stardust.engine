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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.StringKey;

/** */
public class LoopType extends StringKey
{
   public static final LoopType Unknown = new LoopType("Unknown");
   public static final LoopType None = new LoopType("No Loop");
   public static final LoopType While = new LoopType("While");
   public static final LoopType Repeat = new LoopType("Repeat");

   public static LoopType getKey(String id)
   {
      return (LoopType) getKey(LoopType.class, id);
   }

   /** */
   private LoopType(String id)
   {
      super(id, id);
   }
}
