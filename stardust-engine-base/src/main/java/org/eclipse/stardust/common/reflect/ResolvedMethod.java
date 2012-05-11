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
package org.eclipse.stardust.common.reflect;

import java.lang.reflect.Method;

import org.eclipse.stardust.common.constants.PlainJavaConstants;


/**
 * @author sauer
 * @version $Revision$
 */
public class ResolvedMethod
{

   public final Method self;
   
   public final String[] argNames;
   
   public final Class[] argTypes;
   
   public final Class resultType;

   public ResolvedMethod(Method method)
   {
      this.self = method;
      
      this.argTypes = method.getParameterTypes();

      this.argNames = new String[argTypes.length];
      for (int i = 0; i < argTypes.length; i++ )
      {
         String humanName = Reflect.getHumanReadableClassName(argTypes[i]);
         argNames[i] = humanName.toLowerCase().charAt(0)
               + PlainJavaConstants.METHOD_PARAMETER_PREFIX + (i + 1);
      }
      
      this.resultType = method.getReturnType();
   }
}
