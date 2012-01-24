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

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 *
 */
class GenericTreeModel extends DefaultTreeModel
{
   /**
    *
    */
   public GenericTreeModel(TreeNode root)
   {
      super(root, true);
   }

   /**
    * Invoke this method if you've modified the TreeNodes upon which
    * this model depends.
    */
   public void reload()
   {
      ((GenericTreeNode) getRoot()).loadObjects();

      super.reload();
   }

   /**
    * Invoke this method if you've modified the TreeNodes upon which
    * this model depends.
    */
   public void reload(TreeNode node)
   {
      ((GenericTreeNode) node).loadObjects();

      super.reload(node);
   }
}
