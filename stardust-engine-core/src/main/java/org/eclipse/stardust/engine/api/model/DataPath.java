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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.Direction;

/**
 * The <code>DataPath</code> class provides read and write access to the workflow data.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface DataPath extends ModelElement
{
   /**
    * Gets the direction of the data path.
    *
    * @return the direction of the data path.
    */
   Direction getDirection();

   /**
    * Gets the type of the bridge object.
    *
    * @return the corresponding java class of the data path value.
    */
   Class getMappedType();

   /**
    * Gets whether this data path is used as a descriptor or not.
    * <p>The values of data paths marked as descriptors will be automatically attached to
    * the activity instances.</p>
    *
    * @return true if this data path is a descriptor.
    *
    * @see org.eclipse.stardust.engine.api.runtime.ActivityInstance#getDescriptorValue
    */
   boolean isDescriptor();
   
   boolean isKeyDescriptor();

   String getAccessPath();

   String getData();
}
