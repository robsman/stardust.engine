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
import java.util.Map;
import java.util.Set;

/**
 * Describes the contract to implement the runtime behaviour of a synchronous
 * application type. It contains the callbacks the CARNOT engine needs to sucessfully
 * run a synchronous application.
 *
 * @author rsauer, ubirkemeyer
 * @version $Revision$
 */
public interface StatelessSynchronousApplicationInstance extends StatelessApplicationInstance
{
   /**
    * Callback used by the CARNOT engine when the corresponding activity instance
    * is run.
    *
    * @param outDataTypes A set of AccessPointBean names to be expected as return values.
    *        This is filled by the CARNOT engine and is an optimization hint to prevent
    *        the application instance to evaluate all possible OUT AccessPoints.
    * @return A map with the provided AccessPointBean names as keys and the values at
    *         this access points as values.
    * @throws InvocationTargetException Any exception thrown during execution of the
    *         application has to be delivered via this exception.
    */
   Map invoke(ApplicationInvocationContext context, Set outDataTypes)
         throws InvocationTargetException;
}
