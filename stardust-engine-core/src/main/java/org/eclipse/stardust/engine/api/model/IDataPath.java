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


/**
 * @author ubirkemeyer
 */
public interface IDataPath extends IdentifiableElement
{
   void setId(String identifier);

   void setName(String name);

   Direction getDirection();

   void setDirection(Direction value);

   IData getData();

   String getAccessPath();

   boolean isDescriptor();

   void setDescriptor(boolean descriptor);

   boolean isKeyDescriptor();

   void setKeyDescriptor(boolean keyDescriptor);

   void setData(IData data);

   void setAccessPath(String accessPath);

   void checkConsistency(List inconsistencies);
}
