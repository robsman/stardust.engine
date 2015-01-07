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
package org.eclipse.stardust.engine.core.struct;

import java.util.Collections;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.w3c.dom.Node;


/**
 * @author rsauer
 * @version $Revision$
 */
public class DeltaDescriptor
{
   private boolean unsafeDelta = false;
   
   //private List<Long> addedNodes;
   
   private Map<Long, Node> modifiedNodes;
   
   //private Set<Long> removedNodes;

   public boolean isUnsafeDelta()
   {
      return unsafeDelta;
   }

   public void setUnsafeDelta()
   {
      this.unsafeDelta = true;
   }

   public Map getUpdatedNodes()
   {
      return (null != modifiedNodes) ? modifiedNodes : Collections.EMPTY_MAP;
   }
   
   public void registerAddedNode()
   {
   }
   
   public void registerUpdatedNode(long sdvOid, Node newNode)
   {
      if (null == modifiedNodes)
      {
         this.modifiedNodes = CollectionUtils.newMap();
      }
      modifiedNodes.put(sdvOid, newNode);
   }

   public void registerRemovedNode()
   {
   }
}
