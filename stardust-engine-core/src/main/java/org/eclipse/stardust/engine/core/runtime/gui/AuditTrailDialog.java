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
package org.eclipse.stardust.engine.core.runtime.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.DateUtils;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDialog;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;


/**
 *
 */
public class AuditTrailDialog extends AbstractDialog
{
   private static final String[] tableHeaders = {"OID", "Name", "Started", "Completed", "Duration (h)", "Completed By"};

   private static AuditTrailDialog instance;

   private JTable table;
   private JMenuItem detailsItem;
   private JPopupMenu popupMenu;

   /**
    *
    */
   protected AuditTrailDialog(Frame parent)
   {
      super(AbstractDialog.CLOSE_TYPE, parent);
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == detailsItem)
      {
         ActivityInstance instance = ((AuditTrailModel) table.getModel())
               .getActivityInstance(table.getSelectedRow());
         if (instance != null)
         {
            ActivityInstanceDetailsDialog.showDialog(instance);
         }
      }
      else
      {
         // needed for the superclass buttons
         super.actionPerformed(event);
      }
   }

   /**
    * Creates the popup menu for activity instance table.
    */
   private void createPopupMenu()
   {
      popupMenu = new JPopupMenu();
      detailsItem = new JMenuItem("Activity Details...");
      detailsItem.addActionListener(this);
      detailsItem.setMnemonic('e');
      popupMenu.add(detailsItem);
/*      performerDetailsItem = new JMenuItem("Performer Details...");
      performerDetailsItem.addActionListener(this);
      performerDetailsItem.setMnemonic('e');
      popupMenu.add(performerDetailsItem);*/
   }

   /**
    *
    */
   public JComponent createContent()
   {
      createPopupMenu();
      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());
      panel.setBorder(GUI.getEmptyPanelBorder());
      JScrollPane scroller = new JScrollPane(createTable());
      scroller.setPreferredSize(new Dimension(600, 400));
      panel.add(scroller);
      return panel;
   }

   private JTable createTable()
   {
      table = new JTable(new AuditTrailModel());
      table.setShowHorizontalLines(false);
      table.addMouseListener(new MouseAdapter()
      {
         public void mousePressed(MouseEvent e)
         {
            if (GUI.isPopupTrigger(e))
            {
               int row = table.rowAtPoint(e.getPoint());
               if (row >= 0)
               {
                  table.getSelectionModel().setSelectionInterval(row, row);
                  GUI.showPopup(popupMenu, table, e.getX(), e.getY());
               }
            }
         }
      });
      return table;
   }

   public void validateSettings() throws ValidationException
   {
   }

   public static void showDialog(Collection auditTrail, Frame parent)
   {
      if (instance == null)
      {
         instance = new AuditTrailDialog(parent);
      }
      ((AuditTrailModel) instance.table.getModel()).setData(auditTrail);
      showDialog("Audit Trail", instance);
   }

   private class AuditTrailModel extends AbstractTableModel
   {
      java.util.List data = CollectionUtils.newList();

      public void setData(Collection auditTrail)
      {
         data.clear();         if (auditTrail != null)
         {
            data.addAll(auditTrail);
         }
         fireTableDataChanged();
      }

      public int getRowCount()
      {
         return data.size();
      }

      public int getColumnCount()
      {
         return tableHeaders.length;
      }

      public String getColumnName(int columnIndex)
      {
         return tableHeaders[columnIndex];
      }

      public Class getColumnClass(int columnIndex)
      {
         return Object.class;
      }

      public Object getValueAt(int rowIndex, int columnIndex)
      {
         ActivityInstance activity = getActivityInstance(rowIndex);
         switch (columnIndex)
         {
            case 0:
               return Long.toString(activity.getOID());
            case 1:
               return activity.getActivity().getName();
            case 2:
               return DateUtils.formatDateTime(activity.getStartTime());
            case 3:
               return DateUtils.formatDateTime(activity.getLastModificationTime());
            case 4:
               if (!ActivityInstanceState.Completed.equals(activity.getState()))
               {
                  return "N/A";
               }
               double duration = ((double) activity.getLastModificationTime().getTime() -
                      activity.getStartTime().getTime()) / 1000 / 60 / 60;
               return DateUtils.formatDurationAsString(duration);
            case 5:
               return activity.getPerformedByName();
         }
         return null;
      }

      public ActivityInstance getActivityInstance(int row)
      {
         return row < 0 || row >= data.size() ? null : (ActivityInstance) data.get(row);
      }
   }
}
