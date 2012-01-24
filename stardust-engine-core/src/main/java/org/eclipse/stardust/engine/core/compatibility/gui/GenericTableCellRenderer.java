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

import javax.swing.table.TableCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.eclipse.stardust.common.Key;


/**
 *
 */
public class GenericTableCellRenderer implements TableCellRenderer
{
   private JComponent component;
   private JComponent wrappedComponent;

   /**
    *
    */
   public GenericTableCellRenderer(Class columnType)
   {
      component = GuiMapper.getComponentForClass(columnType, true);

      if (component instanceof Entry)
      {
         if (Key.class.isAssignableFrom(columnType))
         {
            component = new JLabel();
            component.setOpaque(true);
            wrappedComponent = component;
         }
         else
         {
            wrappedComponent = ((Entry) component).getWrappedComponent();
         }
      }

      wrappedComponent.setBorder(null);
   }

   /**
    *
    */
   public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
   {
      if (component instanceof Entry)
      {
         ((Entry) component).setObjectValue(value);
      }
      else if (value instanceof Key)
      {
         ((JLabel) component).setText(value.toString());
      }

      if (isSelected)
      {
         wrappedComponent.setForeground(table.getSelectionForeground());
         wrappedComponent.setBackground(table.getSelectionBackground());
      }
      else
      {
         wrappedComponent.setForeground(table.getForeground());
         wrappedComponent.setBackground(table.getBackground());
      }

      return wrappedComponent;
   }
}


