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

import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;


/**
 * Describes the mapping between process data and activity application
 * parameters.
 */
public interface IDataMapping extends IdentifiableElement
{
   String getId();

   String getName();

   /**
    * Populates the vector <code>inconsistencies</code> with all inconsistencies
    * of the data mapping.
    * 
    * @param inconsistencies The list of inconsistencies to be filled.
    */
   public void checkConsistency(List inconsistencies);

   public IData getData();

   public IActivity getActivity();

   public Direction getDirection();

   public void setDirection(Direction parameterType);

   public String getDataPath();

   public void setDataPath(String dataOutPath);

   public String getActivityPath();

   public void setActivityPath(String applicationPath);

   /**
    * Returns the name of the unique access point belonging to the data mapping
    * at the application end point.
    * 
    * @return 
    */
   String getActivityAccessPointId();

   void setActivityAccessPointId(String name);

   AccessPoint getActivityAccessPoint();

   String getContext();

   void setContext(String context);

   void setId(String id);

   void setName(String name);

   void setData(IData data);
}
