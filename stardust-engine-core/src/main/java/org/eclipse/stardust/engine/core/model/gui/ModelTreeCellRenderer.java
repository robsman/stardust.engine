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


import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JTree;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.diagram.Diagram;
import org.eclipse.stardust.engine.core.model.gui.SymbolIconProvider;
import org.eclipse.stardust.engine.core.model.utils.IdentifiableElement;
import org.eclipse.stardust.engine.core.model.utils.ModelElement;

import java.awt.Component;
import java.awt.Color;

public class ModelTreeCellRenderer extends DefaultTreeCellRenderer
{
   private static final long serialVersionUID = -3172389627672264519L;

   public Component getTreeCellRendererComponent(JTree tree, Object value,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      Object userObject = value instanceof DefaultMutableTreeNode
            ? ((DefaultMutableTreeNode) value).getUserObject() : value;

      // icon
      if (value == tree.getModel().getRoot() && userObject == null)
      {
         setIcon(null);
      }
      else if (userObject instanceof ModelElement)
      {
         setIcon(SymbolIconProvider.instance().getIcon(userObject));
      }

      if (!selected && userObject instanceof IAction)
      {
         if (userObject instanceof IBindAction)
         {
            this.setForeground(Color.red);
         }
         else if (userObject instanceof IUnbindAction)
         {
            this.setForeground(Color.blue);
         }
      }

      // text
      setText(getText(userObject));
      return this;
   }

   // todo: (fh) when only CarnotTreeNodes will be used, then make it toString method of that class
   public static final String getText(Object object)
   {
      if (object instanceof IdentifiableElement)
      {
         return ((IdentifiableElement) object).getName();
      }
      else if (object instanceof Diagram)
      {
         return ((Diagram) object).getName();
      }
      else if (object instanceof IView)
      {
         return ((IView) object).getName();
      }
      else if (object instanceof ITrigger)
      {
         return ((ITrigger) object).getName();
      }
      else if (object instanceof Typeable)
      {
         return getText(((Typeable) object).getType());
      }
      return object == null ? "" : object.toString();
   }
}
