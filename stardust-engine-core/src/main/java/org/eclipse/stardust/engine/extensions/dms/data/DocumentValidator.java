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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.core.compatibility.extensions.dms.Document;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.model.BridgeObject;
import org.eclipse.stardust.engine.core.spi.extensions.model.DataValidator;


public class DocumentValidator implements DataValidator
{
   public BridgeObject getBridgeObject(AccessPoint point, String path, Direction direction)
   {
      Direction direction1 = null;

      Class currentType = Document.class;

      if (!StringUtils.isEmpty(path))
      {
         direction1 = Direction.OUT;
         for (Iterator i = JavaDataTypeUtils.parse(path).iterator(); i.hasNext();)
         {
            String element = (String) i.next();
            try
            {
               if (!i.hasNext() && !element.endsWith("()"))
               {
                  //we have found a setter here
                  currentType = Reflect.decodeMethod(currentType, element)
                        .getParameterTypes()[0];
                  direction1 = Direction.IN;
                  // todo: (france, fh) shouldn't be the setter the endpoint ???
               }
               else
               {
                  currentType = Reflect.decodeMethod(currentType, element).getReturnType();
               }
            }
            catch (InternalException x)
            {
               // @todo (france, ub): should we throw a public exception instead?!
               throw new InternalException(
                     "Method '" + element + "' not available or not accessible.", x);
            }
         }
      }
      
      return new BridgeObject(currentType, direction1);
   }

   public List validate(Map attributes)
   {
      List inconsistencies = CollectionUtils.newList();

      // TODO
      
      return inconsistencies;
   }

}
