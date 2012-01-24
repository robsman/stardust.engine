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
 * A client side view of a data mapping.
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface DataMapping extends ModelElement
{
   /**
    * Gets the dereference path between the application access point and the bridge
    * object at the workflow session boundary.
    *
    * @return the access path that will be applied to the application access point.
    */
   String getApplicationPath();

   /**
    * Gets the application access point for this data mapping.
    *
    * @return the application access point.
    */
   AccessPoint getApplicationAccessPoint();

   /**
    * Gets the type of the bridge object.
    *
    * @return the java class of the bridge object.
    */
   Class getMappedType();

   /**
    * Gets the mapping direction.
    *
    * @return the mapping direction.
    */
   Direction getDirection();
   
   /**
    * Gets the id of corresponding data
    * 
    * @return the id of corresponding data
    */
   String getDataId();
   
   /**
    * Gets the access path to read (or write, depending on direction) into the data
    * 
    * @return the id value of the data path, can be null if the whole data is considered
    */
   String getDataPath();
}
