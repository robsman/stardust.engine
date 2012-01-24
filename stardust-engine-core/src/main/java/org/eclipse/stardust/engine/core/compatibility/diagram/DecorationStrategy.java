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

public interface DecorationStrategy
{
   public static final int STYLE_PLAIN = 0;
   public static final int STYLE_TRAVERSED = 1;
   public static final int STYLE_ACTIVE = 2;

   /**
    *
    * @param item The symbol to query decoration style for.
    *
    * @return Either {@link #STYLE_PLAIN}, {@link #STYLE_TRAVERSED} or
    * {@link #STYLE_ACTIVE}.
    */
   int getSymbolStyle(Symbol item);
}
