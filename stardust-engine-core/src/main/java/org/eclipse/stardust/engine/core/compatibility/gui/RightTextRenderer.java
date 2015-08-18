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

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/** */

public class RightTextRenderer extends JLabel

      implements TableCellRenderer

{

   /** */

   public RightTextRenderer()

   {

      super();

      setHorizontalAlignment(JLabel.RIGHT);

      setOpaque(true);

   }

   /** */

   public Component getTableCellRendererComponent(JTable table,

         Object value,

         boolean isSelected,

         boolean hasFocus,

         int row,

         int comlumn)

   {

      if (value != null)

      {

         setText(value.toString());

      }

      else

      {

         setText("");

      }

      if (isSelected)

      {

         setBackground(Color.blue);

         setForeground(SystemColor.textHighlightText);

      }

      else

      {

         setBackground(Color.white);

         setForeground(SystemColor.textText);

      }

      return this;

   }

}