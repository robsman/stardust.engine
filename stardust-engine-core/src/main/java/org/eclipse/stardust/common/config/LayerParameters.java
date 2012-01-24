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

import java.util.Stack;
import java.util.Map;

/**
 * @author fherinean
 * @version $Revision$
 */
class LayerParameters
{
   private Stack<PropertyLayer> layers;

   LayerParameters()
   {
      layers = new Stack<PropertyLayer>();
   }

   public void setThreadLocal(String name, Object value)
   {
      PropertyLayer properties = layers.peek();
      properties.setProperty(name, value);
   }

   public PropertyLayer pushLayer(Map<String, ?> props)
   {
      return pushLayer(null, props);
   }

   public PropertyLayer pushLayer(PropertyLayerFactory layerFactory, Map<String, ?> props)
   {
      final PropertyLayer predecessor = layers.isEmpty()
            ? null
            : (PropertyLayer) layers.peek();

      final PropertyLayer newLayer = (null != layerFactory)
            ? layerFactory.createPropertyLayer(predecessor)
            : new PropertyLayer(predecessor);

      if ((null != props) && !props.isEmpty())
      {
         newLayer.setProperties(props);
      }
      layers.push(newLayer);

      return newLayer;
   }

   public void popLayer()
   {
      PropertyLayer layer = layers.pop();

      AbstractPropertyCache predecessor = ((AbstractPropertyCache) layer)
            .getPredecessor();
      if (null != predecessor)
      {
         predecessor.resetSuccessor();
      }
   }

   public Object get(String name)
   {
      PropertyLayer layer = layers.isEmpty() ? null : (PropertyLayer) layers.peek();
      return layer == null ? null : layer.get(name);
   }
}
