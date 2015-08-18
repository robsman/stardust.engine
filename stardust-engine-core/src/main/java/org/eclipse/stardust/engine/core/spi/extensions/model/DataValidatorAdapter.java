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

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;


public class DataValidatorAdapter implements ExtendedDataValidator
{
   private DataValidator oldValidator;

   public DataValidatorAdapter(DataValidator oldValidator)
   {
      super();
      this.oldValidator = oldValidator;
   }

   public BridgeObject getBridgeObject(AccessPoint point, String path,
         Direction direction, AccessPathEvaluationContext context)
   {
      return oldValidator.getBridgeObject(point, path, direction);
   }

   public List validate(Map attributes)
   {
      return oldValidator.validate(attributes);
   }

}
