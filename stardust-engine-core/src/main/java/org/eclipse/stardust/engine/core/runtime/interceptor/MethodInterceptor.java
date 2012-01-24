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

import java.io.Serializable;

/**
 * Intercepts calls on an interface on it's way to the target. These
 * are nested "on top" of the target.
 *
 * <p>The user should implement the {@link #invoke(MethodInvocation)}
 * method to modify the original behavior. E.g. the following class
 * implements a tracing interceptor (traces all the calls on the
 * intercepted method(s)):
 *
 * <pre class=code>
 * class TracingInterceptor implements MethodInterceptor 
 * {
 *    Object invoke(MethodInvocation i) throws Throwable
 *    {
 *       System.out.println("method " + i.getMethod() + " is called on " +
 *             i.getThis() + " with args " + i.getArguments());
 *       Object ret = i.proceed();
 *       System.out.println("method " + i.getMethod() + " returns " + ret);
 *       return ret;
 *    }
 * }
 * </pre>
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface MethodInterceptor extends Interceptor, Serializable
{
    /**
     * Implement this method to perform extra treatments before and
     * after the invocation. Polite implementations would certainly
     * like to invoke {@link Joinpoint#proceed()}.
     *
     * @param invocation the method invocation joinpoint
     * @return the result of the call to {@link
     * Joinpoint#proceed()}, might be intercepted by the
     * interceptor.
     *
     * @throws Throwable if the interceptors or the
     * target-object throws an exception.  */
    Object invoke(MethodInvocation invocation) throws Throwable;
}
