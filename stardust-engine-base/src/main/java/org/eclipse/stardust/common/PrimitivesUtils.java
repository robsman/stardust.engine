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
package org.eclipse.stardust.common;

/**
 * @author sauer
 * @version $Revision: $
 */
public class PrimitivesUtils
{

   public static boolean nvl(Boolean value, boolean nullValue)
   {
      return (null != value) ? value.booleanValue() : nullValue;
   }
   
   public static int nvl(Integer value, int nullValue)
   {
      return (null != value) ? value.intValue() : nullValue;
   }
   
   public static long nvl(Long value, long nullValue)
   {
      return (null != value) ? value.longValue() : nullValue;
   }
   
}
