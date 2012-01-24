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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.util.Enumeration;

/**
 * <p>An instance of this class iterates over all user objects retrieved as
 * "child objects" for a given node <code>node<code> and its associated user
 * object.</p>
 *
 * <p>If the children node vector of the given node does not contain nodes
 * for all child objects, these are created during iteration.</p>
 *
 * @author Marc Gille
 * @version $Revision$
 *
 * @todo convert this class and clients of this class to an iterator
 */
class GenericTreeNodeEnumeration implements Enumeration
{
   java.util.Iterator iterator;
   GenericTreeNode node;
   GenericTree tree;
   int index;
   int size;

   /**
    *
    */
   public GenericTreeNodeEnumeration(GenericTreeNode node)
   {
      this.node = node;
      index = 0;
   }

   /**
    *
    */
   public boolean hasMoreElements()
   {
      return (index < node.objects.size());
   }

   /**
    * Creates tree nodes on demand.
    *
    * @return the next element from the iterator
    */
   public Object nextElement()
   {
      GenericTreeNode child = 
            (GenericTreeNode)node.getChildrenVector().get(index);

      if (child == null)
      {
         child = new GenericTreeNode(node.tree, node, node.objects.get(index),
            node.isSorted());

         node.getChildrenVector().set(index, child);
      }
      else
      {
         // Nodes may be reused for different user objects, this requires
         // reinitializationof the user object

         if (node.objects.get(index) != child.getUserObject())
         {
            child.setUserObject(node.objects.get(index));
         }
      }

      ++index;

      return child;
   }
}
