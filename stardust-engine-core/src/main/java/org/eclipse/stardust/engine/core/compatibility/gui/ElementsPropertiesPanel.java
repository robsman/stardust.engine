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


import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import org.eclipse.stardust.engine.core.model.gui.ModelElementTemplate;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.Enumeration;

public abstract class ElementsPropertiesPanel extends ModelElementsPropertiesPanel
      implements ItemStateListener
{
   private JList list;

   protected Component createListPanel()
   {
      list = new JList();
      list.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            Object item = list.getSelectedValue();
            setButtonEnabled(REMOVE, isEnabled() && item != null);
            setButtonEnabled(UP, isEnabled() && list.getSelectedIndex() > 0);
            setButtonEnabled(DOWN, isEnabled() &&
                  list.getSelectedIndex() < list.getModel().getSize() - 1
                  && list.getSelectedIndex() >= 0);
            itemSelected(item);
         }
      });
      list.setCellRenderer(new DefaultListCellRenderer()
      {
         public Component getListCellRendererComponent(JList list, Object value,
               int index, boolean isSelected, boolean cellHasFocus)
         {
            super.getListCellRendererComponent(list, value, index,
                  isSelected, cellHasFocus);
            setText(getItemText(value));
            setIcon(getItemIcon(value));
            return this;
         }
      });
      list.setModel(new DefaultListModel());
      JScrollPane scroller = new JScrollPane(list);
      scroller.setPreferredSize(getListPreferredSize());
      return scroller;
   }

   protected Dimension getListPreferredSize()
   {
      return new Dimension(250, 350);
   }

   protected JList getList()
   {
      return list;
   }

   protected Object getCurrentItem()
   {
      return list.getSelectedValue();
   }

   protected Iterator getItems()
   {
      final Enumeration en = ((DefaultListModel) list.getModel()).elements();
      return new Iterator()
      {
         public boolean hasNext()
         {
            return en.hasMoreElements();
         }

         public Object next()
         {
            return en.nextElement();
         }

         public void remove()
         {
            throw new UnsupportedOperationException(REMOVE);
         }
      };
   }

   protected void setItems(Iterator items)
   {
      DefaultListModel model = (DefaultListModel) list.getModel();
      model.clear();
      for (; items.hasNext();)
      {
         Object item = items.next();
         if (item instanceof ModelElementTemplate)
         {
            ((ModelElementTemplate) item).setItemStateListener(this);
         }
         model.addElement(item);
      }
      list.clearSelection();
      itemSelected(null);
   }


   public void selectItem(Object o)
   {
      list.setSelectedValue(o, true);
   }

   public void updateItem(Object item)
   {
      DefaultListModel model = (DefaultListModel) list.getModel();
      int index = model.indexOf(item);
      if (index >= 0)
      {
         model.set(index, item);
      }
   }

   protected void moveItemDown()
   {
      int index = list.getSelectedIndex();
      if (index >= 0 && index < list.getModel().getSize() - 1)
      {
         DefaultListModel model = (DefaultListModel) list.getModel();
         Object item = model.elementAt(index);
         model.removeElementAt(index);
         index++;
         model.insertElementAt(item, index);
         list.setSelectedIndex(index);
      }
   }

   protected void moveItemUp()
   {
      int index = list.getSelectedIndex();
      if (index > 0)
      {
         DefaultListModel model = (DefaultListModel) list.getModel();
         Object item = model.elementAt(index);
         model.removeElementAt(index);
         index--;
         model.insertElementAt(item, index);
         list.setSelectedIndex(index);
      }
   }

   protected void removeItem()
   {
      int index = list.getSelectedIndex();
      if (index >= 0)
      {
         DefaultListModel model = (DefaultListModel) list.getModel();
         model.removeElementAt(index);
         if (index >= model.size())
         {
            index--;
         }
         if (index >= 0)
         {
            list.setSelectedIndex(index);
         }
      }
   }

   protected void addItem()
   {
      ModelElementTemplate item = createItem();
      if (item != null)
      {
         item.setItemStateListener(this);
         DefaultListModel model = (DefaultListModel) list.getModel();
         int index = list.getSelectedIndex();
         if (index < 0)
         {
            index = model.size();
         }
         else
         {
            index++;
         }
         model.insertElementAt(item, index);
         list.setSelectedIndex(index);
      }
   }

   protected abstract String getItemText(Object item);

   protected abstract Icon getItemIcon(Object item);

   protected abstract void itemSelected(Object item);

   protected abstract ModelElementTemplate createItem();
}
