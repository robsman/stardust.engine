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

import org.eclipse.stardust.common.CompareHelper;


/**
 * @author rsauer
 * @version $Revision$
 */
public class ModelApiUtils
{
   public static ModelElement firstWithId(Iterator source, String id)
   {
      ModelElement result = null;

      while (source.hasNext())
      {
         ModelElement item = (ModelElement) source.next();
         if (CompareHelper.areEqual(id, item.getId()))
         {
            result = item;
            break;
         }
      }

      return result;
   }

   private ModelApiUtils()
   {
      // utility class
   }
}
