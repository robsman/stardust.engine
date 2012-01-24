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
package org.eclipse.stardust.engine.core.model.gui;


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.core.model.utils.ModelElement;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author fherinean
 * @version $Revision$
 */
public abstract class CarnotTreeNode extends DefaultMutableTreeNode
{
   protected String compareValue;
   protected DefaultTreeModel treeModel;

   public CarnotTreeNode(Object userObject, boolean allowsChildren)
   {
      super(userObject, allowsChildren);
      compareValue = getCompareValue(userObject);
   }

   protected String getCompareValue(Object o1)
   {
      if (o1 == null)
      {
         return "";
      }
      String class1 = o1 instanceof String ? "" : o1.getClass().getName();
      return class1 + ":" + getText(o1);
   }

   public abstract String getText(Object o1);

   public void setTreeModel(DefaultTreeModel treeModel)
   {
      this.treeModel = treeModel;
   }

   public CarnotTreeNode getNode(int i)
   {
      return (CarnotTreeNode) getChildAt(i);
   }

   public int findCreateIndex(Object userObject)
   {
      // hint: (fh) children seems to be lazily created in DefaultMutableTreeNode
      String ox = getCompareValue(userObject);
      if (children == null)
      {
         return 0;
      }

      // hint: (fh) code adapted from Collections.binarySearch
      int low = 0;
      int high = children.size() - 1;

      while (low <= high)
      {
         int mid = (low + high) / 2;
         String midVal = ((CarnotTreeNode) children.get(mid)).compareValue;
         int cmp = midVal.compareToIgnoreCase(ox);

         if (cmp < 0)
         {
            low = mid + 1;
         }
         else if (cmp > 0)
         {
            high = mid - 1;
         }
         else
         {
            return mid;
         }
      }
      return low;
   }

   public CarnotTreeNode add(Object userObject)
   {
      CarnotTreeNode newNode = create(userObject, false);
      newNode.treeModel = treeModel;
      treeModel.insertNodeInto(newNode, this, findCreateIndex(userObject));
      return newNode;
   }

   public CarnotTreeNode add(int index, Object userObject)
   {
      CarnotTreeNode newNode = create(userObject, false);
      newNode.treeModel = treeModel;
      treeModel.insertNodeInto(newNode, this, index);
      return newNode;
   }

   public abstract CarnotTreeNode create(Object userObject, boolean allowsChildren);

   public void delete()
   {
      treeModel.removeNodeFromParent(this);
   }

   public void update()
   {
      if (!compareValue.equals(getCompareValue(userObject)))
      {
         if (isRoot())
         {
            treeModel.nodeChanged(this);
         }
         else
         {
            CarnotTreeNode parent = (CarnotTreeNode) getParent();
            treeModel.removeNodeFromParent(this);
            treeModel.insertNodeInto(this, parent, parent.findCreateIndex(userObject));
         }
         compareValue = getCompareValue(userObject);
      }
   }

   public CarnotTreeNode getNode(Object element)
   {
      for (int i = 0; i < getChildCount(); i++)
      {
         CarnotTreeNode node = (CarnotTreeNode) children.get(i);
         if (node.getUserObject() == element)
         {
            return node;
         }
      }
      return null;
   }

   public Iterator findAll(final Object object)
   {
      ArrayList list = new ArrayList();
      for (Enumeration e = depthFirstEnumeration(); e.hasMoreElements();)
      {
         CarnotTreeNode node = (CarnotTreeNode) e.nextElement();
         if (node.getUserObject() == object)
         {
            list.add(node);
         }
      }
      return list.iterator();
   }

   public void delete(ModelElement element)
   {
      CarnotTreeNode node = getNode(element);
      if (node != null)
      {
         node.delete();
      }
   }

   public CarnotTreeNode findFirst(Object object)
   {
      for (Enumeration e = depthFirstEnumeration(); e.hasMoreElements();)
      {
         CarnotTreeNode node = (CarnotTreeNode) e.nextElement();
         if (node.getUserObject() == object)
         {
            return node;
         }
      }
      return null;
   }
}
