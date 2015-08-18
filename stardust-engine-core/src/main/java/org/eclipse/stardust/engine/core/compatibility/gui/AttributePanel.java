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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Attribute;
import org.eclipse.stardust.common.AttributeHolder;


/** */
public class AttributePanel extends JPanel
      implements ActionListener
{
   private static String[] tableHeaders = new String[]{"Name", "Value"};
   private static String[] tableProperties = new String[]{"Name", "StringifiedValue"};

   private AttributeHolder propertyHolder;
   private GenericTable table;
   private JMenuItem createStringPropertyItem;
   private JMenuItem createIntegerPropertyItem;
   private JMenuItem modifyPropertyItem;
   private JMenuItem removePropertyItem;

   private boolean readOnly = false;

   /** */
   public AttributePanel()
   {
      this(true);
   }

   /** */
   public AttributePanel(boolean readOnly)
   {
      layoutPanel();
      setReadOnly(readOnly);
   }

   /** */
   public void actionPerformed(ActionEvent e)
   {
      Assert.isNotNull(propertyHolder, "the Propertyholder is not null");
      if (e.getSource() == createIntegerPropertyItem)
      {
         IntegerPropertyDialog.showDialog(propertyHolder, JOptionPane.getFrameForComponent(this));
         //@todo jawad change it later.
         //table.setEnumeration(propertyHolder.getAllProperties());
      }
      else if (e.getSource() == createStringPropertyItem)
      {
         StringPropertyDialog.showDialog(propertyHolder, JOptionPane.getFrameForComponent(this));
         //@todo jawad change it later.
         //table.setEnumeration(propertyHolder.getAllProperties());
      }
      else if (e.getSource() == removePropertyItem)
      {
         // @todo ... implement delete functionality
      }
   }

   /**
    * @return boolean
    * @todo Insert the method's description here.
    */
   public boolean isReadOnly()
   {
      return readOnly;
   }

   /**
    *
    */
   public void layoutPanel()
   {
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      setBorder(GUI.getEmptyPanelBorder());

      JTabbedPane tabbedPane = new JTabbedPane();

      table = new GenericTable(Attribute.class, tableProperties, tableHeaders);
      table.setNumberColumnDisplayed(false);
      table.setStatusBarVisible(false);
      table.getTable().setShowVerticalLines(true);
      table.getTable().setShowHorizontalLines(false);
      table.getTable().setCellSelectionEnabled(false);
      table.getTable().setColumnSelectionAllowed(false);
      table.getTable().setRowSelectionAllowed(true);
      table.setBackground(Color.white);

      // Create popup menu

      JPopupMenu popupMenu = new JPopupMenu();

      createStringPropertyItem = new JMenuItem("Create String Property ...");

      createStringPropertyItem.addActionListener(this);
      createStringPropertyItem.setMnemonic('s');
      popupMenu.add(createStringPropertyItem);

      createIntegerPropertyItem = new JMenuItem("Create Integer Property ...");

      createIntegerPropertyItem.addActionListener(this);
      createIntegerPropertyItem.setMnemonic('i');
      popupMenu.add(createIntegerPropertyItem);
      popupMenu.addSeparator();

      modifyPropertyItem = new JMenuItem("Modify Property ...");

      modifyPropertyItem.addActionListener(this);
      modifyPropertyItem.setMnemonic('m');
      popupMenu.add(modifyPropertyItem);
      popupMenu.addSeparator();

      removePropertyItem = new JMenuItem("Remove Property");

      removePropertyItem.addActionListener(this);
      removePropertyItem.setMnemonic('r');
      popupMenu.add(removePropertyItem);

      table.setPopupMenu(popupMenu);

      add(table);
   }

   /** */
   public void setAttributeHolder(AttributeHolder propertyHolder)
   {
      this.propertyHolder = propertyHolder;
      //@todo jawad change it later.
      //table.setEnumeration(propertyHolder.getAllProperties());
   }

   /**
    * @param newReadOnly boolean
    * @todo Insert the method's description here.
    */
   public void setReadOnly(boolean newReadOnly)
   {
      readOnly = newReadOnly;

      if (createStringPropertyItem != null)
      {
         createStringPropertyItem.setEnabled(!readOnly);
      }
      if (createIntegerPropertyItem != null)
      {
         createIntegerPropertyItem.setEnabled(!readOnly);
      }
      if (modifyPropertyItem != null)
      {
         modifyPropertyItem.setEnabled(!readOnly);
      }
      if (removePropertyItem != null)
      {
         removePropertyItem.setEnabled(!readOnly);
      }

   }
}
