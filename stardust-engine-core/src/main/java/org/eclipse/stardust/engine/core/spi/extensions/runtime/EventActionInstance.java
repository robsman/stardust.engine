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

import java.util.Iterator;
import java.util.Map;

/**
 * Implementations of <code>EventActionInstance</code> provide the actual behaviour of
 * any event action type.
 * 
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface EventActionInstance
{
   /**
    * Callback allowing for initialization of newly created event action instances.
    *
    * @param actionAttributes The type specific attributes of this event action.
    * @param accessPoints The access points available to the event action.
    */
   void bootstrap(Map actionAttributes, Iterator accessPoints);

   /**
    * Actually performs the event action.
    * 
    * @param event The event causing action execution.
    * @return The transformed event, possibly indicating additional side effects. Same as
    *       <code>event</code> in the most simple case.
    * @throws UnrecoverableExecutionException if event action execution terminated
    *       abruptly
    */
   Event execute(Event event) throws UnrecoverableExecutionException;
}
