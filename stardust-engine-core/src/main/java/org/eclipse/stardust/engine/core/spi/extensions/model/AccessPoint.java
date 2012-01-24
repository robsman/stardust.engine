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
package org.eclipse.stardust.engine.core.spi.extensions.model;

import org.eclipse.stardust.common.AttributeHolder;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.api.model.Typeable;


/**
 * Provides an abstraction of a handle to write data into or retrieve data from an entity.
 * <p />
 * Examples of such a handle are properties of a JavaBean or attributes of a JMS message.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AccessPoint extends AttributeHolder, Typeable
{
   /**
    * Gets the data flow direction of this access point, may be either
    * {@link Direction#In}, {@link Direction#Out} or {@link Direction#InOut}.
    *
    * @return The data flow direction.
    */
   Direction getDirection();

   /**
    * Gets the id of this access point.
    *
    * @return The access point id.
    *
    * @see #getName()
    */
   String getId();

   /**
    * Gets the name of this access point.
    *
    * @return The access point name.
    *
    * @see #getId()
    */
   String getName();
}
