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

import java.util.Vector;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TinyTreeModel implements TreeModel
{
   private TinyTreeNode root;
   private Vector listeners = new Vector();

   public TinyTreeModel(TinyTreeNode root)
   {
      this.root = root;
   }

   public Object getRoot()
   {
      return root;
   }

   public Object getChild(Object parent, int index)
   {
      return ((TinyTreeNode) parent).getChild(index);
   }

   public int getChildCount(Object parent)
   {
      return ((TinyTreeNode) parent).getChildCount();
   }

   public boolean isLeaf(Object node)
   {
      return ((TinyTreeNode) node).getChildCount() == 0;
   }

   public void valueForPathChanged(TreePath path, Object newValue)
   {
   }

   public int getIndexOfChild(Object parent, Object child)
   {
      return ((TinyTreeNode) parent).getIndexOfChild(child);
   }

   public void addTreeModelListener(TreeModelListener l)
   {
      listeners.add(l);
   }

   public void removeTreeModelListener(TreeModelListener l)
   {
      listeners.remove(l);
   }

}
