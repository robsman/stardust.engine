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
package org.eclipse.stardust.engine.core.model.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class UniqueIdCollector implements Collector
{
   private static final Logger trace = LogManager.getLogger(UniqueIdCollector.class);

   private Map targets = new HashMap();

   public UniqueIdCollector()
   {
   }

   public void collect(ModelElement source, ModelElement target)
   {
      targets.put(source, target);
   }

   public ModelElement findInTarget(ModelElement source)
   {
      ModelElement result =  (ModelElement) targets.get(source);
      if (result == null)
      {
         trace.info("No match for : " + source);
      }
      return result;
   }

   public Object getLocalId(ModelElement element)
   {
      return element.getUniqueId();
   }
}
