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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;

public class StaticJavaMethodCallable implements Callable {
	Class clazz;
	String name;
	Method method;
	
	public StaticJavaMethodCallable(Class clazz, String name) {
		this.clazz = clazz;
		this.name = name;
		this.method = getMethod(name);
	}

	public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {		
		Object result = null;
		args = unwrapArgs(args);		
		try {					
			result = method.invoke(null, args);			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
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

	private Method getMethod(String name) {
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getName().equalsIgnoreCase(name)) {
				return method;
			}
		}
		return null;
	}

}
