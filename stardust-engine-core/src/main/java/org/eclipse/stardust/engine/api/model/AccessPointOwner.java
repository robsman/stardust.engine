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

import java.util.Iterator;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public interface AccessPointOwner
{
   
   /**
    * Searches for access point with specific id. Direction is ignored.
    * @param id
    * @return
    */
   AccessPoint findAccessPoint(String id);
   
   /**
    * Searches for access point with specific id and direction. Access point 
    * direction matches if (1) the parameter direction null 
    * (2) direction is exactly the same or (3) direction of access point is IN_OUT.
    * @param id
    * @param direction
    * @return
    */
   AccessPoint findAccessPoint(String id, Direction direction);

   AccessPoint createAccessPoint(String id, String name, Direction direction,
         IDataType type, int elementOID);

   void removeFromAccessPoints(AccessPoint match);

   Iterator getAllAccessPoints();

   Iterator getAllInAccessPoints();

   Iterator getAllOutAccessPoints();

   String getProviderClass();

   void addIntrinsicAccessPoint(AccessPoint ap);

   Iterator getAllPersistentAccessPoints();
}
