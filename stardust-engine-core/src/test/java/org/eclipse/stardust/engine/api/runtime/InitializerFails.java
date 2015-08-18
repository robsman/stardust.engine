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
package org.eclipse.stardust.engine.api.runtime;

/**
 *
 */

@SuppressWarnings("null")
public class InitializerFails
{

   public static final String bla;

   static
   {
      String blubb = null;

      // this will cause an ExceptionInInitializerError:
      bla = blubb.toUpperCase();
   }

   public static final String getString()
   {
      return bla;
   }
}
