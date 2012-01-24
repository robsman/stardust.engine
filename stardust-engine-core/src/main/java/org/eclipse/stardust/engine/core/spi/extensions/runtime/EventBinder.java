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

import java.util.Map;

import org.eclipse.stardust.engine.api.model.IEventHandler;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface EventBinder
{
   /**
    *
    * @param objectType
    * @param oid
    * @param handler
    * @param handlerAttributes The (possibly modified) map of handler attributes.
    */
   void bind(int objectType, long oid, IEventHandler handler, Map handlerAttributes);

   void unbind(int type, long objectOID, IEventHandler handler);
   
   void deactivate(int type, long objectOID, IEventHandler handler);
}
