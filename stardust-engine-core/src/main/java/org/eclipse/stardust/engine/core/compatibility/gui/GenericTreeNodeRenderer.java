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

import java.awt.SystemColor;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 */
public class GenericTreeNodeRenderer extends DefaultTreeCellRenderer
{
   static ImageIcon folderIcon;
   static ImageIcon fileIcon;
   static ImageIcon fetchIcon;
   static ImageIcon nextIcon;
   static ImageIcon previousIcon;

   static
   {
      folderIcon = new ImageIcon(GenericTreeNodeRenderer.class.getResource("images/folder.gif"));
      fileIcon = new ImageIcon(GenericTreeNodeRenderer.class.getResource("images/file.gif"));
      fetchIcon = new ImageIcon(GenericTreeNodeRenderer.class.getResource("images/added_element.gif"));
      nextIcon = new ImageIcon(GenericTreeNodeRenderer.class.getResource("images/added_element.gif"));
      previousIcon = new ImageIcon(GenericTreeNodeRenderer.class.getResource("images/removed_element.gif"));
   }

   public GenericTreeNodeRenderer()
   {
      super();

      setOpaque(true);
   }

   public java.awt.Component getTreeCellRendererComponent(JTree tree,
         Object value,
         boolean selected,
         boolean expanded,
         boolean leaf,
         int row,
         boolean hasFocus)
   {
      final Object userObject;
      if (!(value instanceof GenericTreeNode))
      {
         setText("");
      }
      else
      {
         userObject = ((GenericTreeNode) value).getUserObject();
         if (userObject == tree)
         {
            setText("more ...");
            setIcon(fetchIcon);
         }
         else if (userObject instanceof TreeNavigator)
         {
            if (userObject instanceof TreeNavigatorUp)
            {
               setText("previous ...");
               setIcon(previousIcon);
            }
            else
            {
               setText("next ...");
               setIcon(nextIcon);
            }
         }
         else
         {
            setText(((GenericTreeNode) value).label);
            ImageIcon icon = ((GenericTreeNode) value).icon;

            if (icon != null)
            {
               setIcon(icon);
            }
            else
            {
               if (((GenericTreeNode) value).isLeaf())
               {
                  setIcon(fileIcon);
               }
               else
               {
                  setIcon(folderIcon);
               }
            }
         }
      }

      if (selected)
      {
         setBackground(((GenericTree) tree).getLabelSelectionBackgroundColor());
         setForeground(SystemColor.textHighlightText);
      }
      else
      {
         setBackground(tree.getBackground());
         setForeground(SystemColor.textText);
      }

      return this;
   }
}