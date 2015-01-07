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
package org.eclipse.stardust.engine.core.extensions.interactive.contexts.manual;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.eclipse.stardust.engine.api.model.IApplication;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.ApplicationPropertiesPanel;


/**
 *
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ManualApplicationPropertiesPanel extends ApplicationPropertiesPanel
{
   Component parent;

   public ManualApplicationPropertiesPanel(Component parent)
   {
      this.parent = parent;
      initComponents();
   }

   protected void initComponents()
   {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(GUI.getEmptyPanelBorder());

      final TableModel myModel = new TableModel();
      JTable table = new JTable(myModel);
      table.setPreferredScrollableViewportSize(new Dimension(500, 70));

      //Create the scroll pane and add the table to it.
      JScrollPane scrollPane = new JScrollPane(table);

      //Add the scroll pane to this window.
      add(scrollPane, BorderLayout.CENTER);

      JButton addButton = new JButton("add");
      addButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            myModel.addRow();
         }
      });
      add(addButton);

   }

   public void setData(Map properties, java.util.Iterator accessPoints)
   {
   }

   public Map getAttributes()
   {
      return null;
   }

   public void validatePanel()
   {
   }

   public void createAccessPoints(IApplication application)
   {
    // todo
   }

   class TableModel extends AbstractTableModel
   {
      final String[] columnNames = {"ID",
                                    "Label",
                                    "Test"};

      final Class[] columnClasses = {String.class, String.class, Boolean.class};
      final LinkedList data = new LinkedList();

      public int getColumnCount()
      {
         return columnNames.length;
      }

      public int getRowCount()
      {
         return data.size();
      }

      public String getColumnName(int col)
      {
         return columnNames[col];
      }

      public Object getValueAt(int row, int col)
      {
         return ((TableRow) data.get(row)).get(col);
      }

      public Class getColumnClass(int c)
      {

         return columnClasses[c];
      }

      public boolean isCellEditable(int row, int col)
      {
            return true;
      }

      public void setValueAt(Object value, int row, int col)
      {
         ((TableRow) data.get(row)).set(col, value);
         fireTableCellUpdated(row, col);
      }

      public void addRow()
      {
         data.add(new TableRow());
         fireTableRowsInserted(data.size() -1, data.size() -1);
      }
   }

   class TableRow
   {
      Object[] data = new Object[] {"", "", new Boolean(false)};

      public Object get(int col)
      {
         return data[col];
      }

      public void set(int col, Object value)
      {
         data[col] = value;
      }

   }
}
