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
package org.eclipse.stardust.engine.core.pojo.utils;

import org.eclipse.stardust.common.StringKey;

/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class JavaAccessPointType extends StringKey
{
   public static final JavaAccessPointType PARAMETER =
         new JavaAccessPointType("PARAMETER", "Parameter");
   public static final JavaAccessPointType RETURN_VALUE =
         new JavaAccessPointType("RETURN_VALUE", "Return value");
   public static final JavaAccessPointType METHOD =
         new JavaAccessPointType("METHOD", "Method");

   public JavaAccessPointType(String id, String defaultName)
   {
      super(id, defaultName);
   }
}
