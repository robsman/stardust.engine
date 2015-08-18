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
package org.eclipse.stardust.engine.core.compatibility.diagram;

public final class DefaultDecorationStrategy implements DecorationStrategy
{
   private static final DecorationStrategy INSTANCE = new DefaultDecorationStrategy();

   public static DecorationStrategy instance()
   {
      return INSTANCE;
   }

   private DefaultDecorationStrategy()
   {
   }

   public int getSymbolStyle(Symbol item)
   {
      return STYLE_PLAIN;
   }
}
