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

import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.border.LineBorder;

/**
 *
 */
public class GenericTreeNodeEditor extends DefaultCellEditor
{
   private Object currentUserObject;
   private GenericTreeNode currentNode;

   /**
    *
    */
   public GenericTreeNodeEditor()
   {
      super(new JTextField());

      ((JTextField) getComponent()).setBorder(new LineBorder(SystemColor.textText, 1));
   }

   /**
    * @return The content of the entry field.
    */
   public String getText()
   {
      return ((JTextField) getComponent()).getText();
   }

   /**
    * @return The currently edited node.
    */
   public GenericTreeNode getNode()
   {
      return currentNode;
   }

   /**
    * @return The user object attached to the currently edited node.
    */
   public Object getCellEditorValue()
   {
      return currentUserObject;
   }

   /**
    *
    */
   public Component getTreeCellEditorComponent(JTree tree, Object value,
         boolean isSelected, boolean expanded,
         boolean leaf, int row)
   {
      if (value instanceof GenericTreeNode)
      {
         currentUserObject = ((GenericTreeNode) value).getUserObject();
         currentNode = (GenericTreeNode) value;

         if (currentUserObject == tree)
         {
            currentUserObject = null;
            currentNode = null;
         }
      }
      else
      {
         currentUserObject = null;
         currentNode = null;
      }

      Component component = super.getTreeCellEditorComponent(tree, value, isSelected, expanded,
            leaf, row);

      if (currentNode != null)
      {
         ((JTextField) getComponent()).setText(currentNode.getLabel());
         ((JTextField) getComponent()).requestFocus();
      }

      return component;
   }
}
