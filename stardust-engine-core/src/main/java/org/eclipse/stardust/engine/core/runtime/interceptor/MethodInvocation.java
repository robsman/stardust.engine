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
package org.eclipse.stardust.engine.core.runtime.interceptor;

import java.lang.reflect.Method;

import org.eclipse.stardust.common.config.Parameters;


public interface MethodInvocation extends Invocation
{
   Object execute() throws Throwable;
   
   Method getMethod();

   Object getTarget();

   /**
    * Provides the result of this method invocation, if available.
    *
    * @return The method invocation's return value.
    */
   Object getResult();
   
   /**
    * Provides the exception causing abnormal termination of this method invocation, if available.
    *
    * @return The exception causing abnormal termination.
    */
   Throwable getException();

   Parameters getParameters();
}

