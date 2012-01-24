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
package org.eclipse.stardust.engine.core.pojo.data;

import java.util.Map;
import java.util.Vector;
import java.util.List;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;


public class PrimitiveValidator implements DataValidator, Stateless
{

   public boolean isStateless()
   {
      return true;
   }

   public BridgeObject getBridgeObject(AccessPoint point, String path, Direction direction)
   {
      return JavaDataTypeUtils.getBridgeObject(point, path);
   }

   public List validate(Map attributes)
   {
      Vector inconsistencies = new Vector();
      Type type = (Type) attributes.get(PredefinedConstants.TYPE_ATT);

      if (null == type)
      {
         inconsistencies.add(new Inconsistency("Unspecified type for primitive data",
               Inconsistency.ERROR));
      }
      return inconsistencies;
   }

}
