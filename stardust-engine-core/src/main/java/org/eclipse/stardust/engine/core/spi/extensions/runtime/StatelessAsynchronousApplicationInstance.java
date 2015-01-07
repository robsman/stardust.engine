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
package org.eclipse.stardust.engine.core.spi.extensions.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

/**
 * Describes the contract to implement the runtime behaviour of a synchronous
 * application type. It contains the callbacks the CARNOT engine needs to sucessfully
 * run a synchronous application.
 *
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface StatelessAsynchronousApplicationInstance extends StatelessApplicationInstance
{
   /**
    * Callback to make an asynchronous call.
    * 
    * @throws InvocationTargetException
    *            Any exception thrown while attempting to send has to be delivered via
    *            this exception.
    */
   void send(ApplicationInvocationContext c) throws InvocationTargetException;

   /**
    * Callback when the corresponding activity instance is awakened from the
    * <code>HIBERNATED</code> state.
    *
    * @param data The data received.
    *
    * @param outDataTypes A set of AccessPointBean names to be expected as return values.
    *        This is filled by the CARNOT engine and is an optimization hint to prevent
    *        the application instance to evaluate all possible OUT AccessPoints.
    * @return A map with the provided AccessPointBean names as keys and the values at
    *        this access points as values.
    */
   Map receive(ApplicationInvocationContext c, Map data, Iterator outDataTypes);

   /**
    * Indicates that the instance will implement SEND behaviour.
    *
    * @return <code>true</code> if the the application provides asynchronous send
    *       functionality, <code>false</code> if not.
    */
   boolean isSending(ApplicationInvocationContext c);

   /**
    * Indicates that the  instance will implement RECEIVE behaviour.
    *
    * @return <code>true</code> if the the application provides asynchronous receive
    *       functionality, <code>false</code> if not.
    */
   boolean isReceiving(ApplicationInvocationContext c);
}
