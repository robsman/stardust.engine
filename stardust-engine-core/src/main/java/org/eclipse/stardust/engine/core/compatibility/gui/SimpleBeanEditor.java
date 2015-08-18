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
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;

// @todo (france, ub): what is this?

public class SimpleBeanEditor extends JPanel
{
   private SimpleBeanTableModel model;
   private JTable table;
   private JScrollPane pane;

   public SimpleBeanEditor()
   {
      this(false);
   }

   public SimpleBeanEditor(boolean showHeader)
   {
      super(new BorderLayout());
      table = new JTable();
      if (!showHeader)
      {
         table.setTableHeader(null);
      }
      pane = new JScrollPane(table);
      add(pane, BorderLayout.CENTER);
      reset();
   }

   public void setData(Object bean, String[] editableProperties)
   {
      if (bean != null)
      {
         model = new SimpleBeanTableModel(this, bean, editableProperties);
      }
      reset();
   }

   private void reset()
   {
      if (model != null)
      {
         table.setModel(model);
      }
      pane.setMinimumSize(new Dimension(50, 50));
      Dimension dim = table.getPreferredSize();
      Insets insets = pane.getInsets();
      pane.setPreferredSize(new Dimension(
         Math.max(300, (int) dim.getWidth()) + insets.left + insets.right,
         Math.min(table.getRowHeight() * 5,
            (int) dim.getHeight()) + insets.top + insets.bottom));
      validate();
   }

   public Iterator getPropertiesIterator()
   {
      return model.getPropertiesIterator();
   }

   public Object getObject()
   {
      return model.getObject();
   }

   public void setDefaultValues(Map properties)
   {
      Iterator itr = properties.keySet().iterator();
      while (itr.hasNext())
      {
         String name = itr.next().toString();
         model.setValue(name, properties.get(name));
      }
   }

   public void copyPropertiesAsStrings(Map properties, String base)
   {
      Iterator itr = getPropertiesIterator();
      while (itr.hasNext())
      {
         String key = (String) itr.next();
         Object prop = model.getValue(key);
         String value = prop == null ? null : prop.toString();
         if (value != null && value.length() > 0)
         {
            properties.put(base + "." + key, value);
         }
      }
   }
}
