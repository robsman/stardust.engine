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
package org.eclipse.stardust.engine.core.runtime.beans;

import java.util.Iterator;

import org.eclipse.stardust.engine.api.dto.DetailsCache;
import org.eclipse.stardust.engine.api.model.IAction;
import org.eclipse.stardust.engine.api.model.IEventHandler;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventActionInstance;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.EventHandlerInstance;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface  DependentObjectsCache<T> extends DetailsCache<T>
{
   Iterator<T> getEmitters();

   EventHandlerInstance getHandlerInstance(IEventHandler handler);

   EventActionInstance getActionInstance(IAction action);
}
