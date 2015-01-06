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
package org.eclipse.stardust.engine.core.javascript;

import java.lang.reflect.Method;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class EnumCallable implements Callable {
	private EnumAccessor enumAccessor;
	private Method method;

	public EnumCallable(EnumAccessor accessor) {
		super();
		this.enumAccessor = accessor;
	}
	
	public EnumCallable(EnumAccessor accessor, Method method)
   {
	      super();
	      this.enumAccessor = accessor;
	      this.method = method;
   }

   public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args)
   {
      Object result = null;
      if (method != null)
      {
         try
         {
            return method.invoke(enumAccessor.realEnum, args);
         }
         catch (Throwable t)
         {
            t.printStackTrace();
         }
      }
      else
      {
         args = unwrapArgs(args);
         Object arg = args[0];
         if (arg != null)
         {
            return enumAccessor.equivalentValues(arg);
         }
      }
      return result;
   }
	
	private Object[] unwrapArgs(Object[] args) {
		Object[] result = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof NativeJavaObject) {
				result[i] = ((NativeJavaObject)args[i]).unwrap();
			} else {
				result[i] = args[i];
			}
		}
		return result;
	}


}
