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
package org.eclipse.stardust.common.config;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.util.Iterator;
import java.util.Map;

/**
 * @author fherinean
 * @version $Revision$
 */
public class PropertyLayer extends AbstractPropertyCache
{
   private final Map<String, Object> layerProperties;

   protected PropertyLayer(PropertyLayer predecessor)
   {
      super(predecessor);

      this.layerProperties = newHashMap();
   }

   public void setProperty(String name, Object value)
   {
      layerProperties.put(name, value);
      uncacheProperty(name);
   }

   public <V extends Object> void setProperties(Map<String, V> properties)
   {
      for (Iterator<Map.Entry<String, V>> i = properties.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry<String, V> prop = i.next();
         String name = prop.getKey();
         layerProperties.put(name, prop.getValue());
         uncacheProperty(name);
      }
   }

   protected Object resolveProperty(String name)
   {
      Object value = null;
      if (null != layerProperties)
      {
         if (layerProperties.containsKey(name))
         {
            value = layerProperties.get(name);
            if (null == value)
            {
               value = Parameters.NULL_VALUE;
            }
         }
      }
      return value;
   }
}
