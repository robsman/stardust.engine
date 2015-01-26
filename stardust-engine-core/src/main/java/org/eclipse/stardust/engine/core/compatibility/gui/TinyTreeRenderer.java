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

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TinyTreeRenderer extends DefaultTreeCellRenderer
{
   public Component getTreeCellRendererComponent(JTree tree, Object value,
         boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
   {
      super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      TinyTreeNode node = ((TinyTreeNode) value);
      setText(node.getText());
      setIcon(node.getIcon());
      Font font = getFont();
      if (font != null)
      {
         setFont(font.deriveFont(node.getFontStyle()));
      }
      return this;
   }

}
