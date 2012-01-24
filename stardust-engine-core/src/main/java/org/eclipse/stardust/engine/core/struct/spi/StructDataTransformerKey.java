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

package org.eclipse.stardust.engine.core.struct.spi;

import org.eclipse.stardust.common.StringKey;

/**
 * 
 * @author born
 */
public class StructDataTransformerKey extends StringKey
{
   private static final long serialVersionUID = 1L;

   public static final String NONE = "NONE";
   public static final String DOM = "DOM";
   public static final String BEAN = "BEAN";

   public static final StructDataTransformerKey None = new StructDataTransformerKey(NONE);
   public static final StructDataTransformerKey Dom = new StructDataTransformerKey(DOM);
   public static final StructDataTransformerKey Bean = new StructDataTransformerKey(BEAN);

   public static StructDataTransformerKey getKey(String id)
   {
      return (StructDataTransformerKey) getKey(StructDataTransformerKey.class, id);
   }

   protected Object readResolve()
   {
      return super.readResolve();
   }

   private StructDataTransformerKey(String id)
   {
      super(id, id);
   }
}
