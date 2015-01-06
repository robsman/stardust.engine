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
import java.util.HashMap;

import javax.swing.*;

public abstract class ModelElementsPropertiesPanel extends JPanel
{
   protected static final String ADD = "add";
   protected static final String REMOVE = "remove";
   protected static final String UP = "up";
   protected static final String DOWN = "down";

   private ActionListener listener;
   private HashMap buttons = new HashMap();
   private JToolBar toolbar;

   public ModelElementsPropertiesPanel()
   {
      setLayout(new BorderLayout(GUI.HorizontalWidgetDistance, 0));
      setBorder(GUI.getEmptyPanelBorder());
      toolbar = createToolbar();
      if (toolbar != null)
      {
         add(toolbar, BorderLayout.NORTH);
      }
      Component details = createDetailsPanel();
      if (details != null)
      {
         add(details);
      }
      Component list = createListPanel();
      if (list != null)
      {
         add(list, BorderLayout.WEST);
      }
   }

   protected JToolBar createToolbar()
   {
      JToolBar toolbar = new JToolBar();
      toolbar.setFloatable(false);
      toolbar.setBorderPainted(false);
      toolbar.add(createButton("images/cross_add.gif", null, ADD));
      toolbar.add(createButton("images/cross_delete.gif", null, REMOVE));
      toolbar.add(createButton("images/arrow_up.gif", null, UP));
      toolbar.add(createButton("images/arrow_down.gif", null, DOWN));
      setButtonEnabled(ADD, true);
      return toolbar;
   }

   protected JToolBar getToolbar()
   {
      return toolbar;
   }

   protected void setActionListener(String command, ActionListener listener)
   {
      ToolbarButton button = (ToolbarButton) buttons.get(command);
      if (button != null)
      {
         button.removeActionListener(this.listener);
         button.addActionListener(listener);
      }
   }

   protected JButton createButton(String icon, String text, String command)
   {
      if (listener == null)
      {
         listener = new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               if (e.getActionCommand().equals(ADD))
               {
                  addItem();
               }
               else if (e.getActionCommand().equals(REMOVE))
               {
                  removeItem();
               }
               else if (e.getActionCommand().equals(UP))
               {
                  moveItemUp();
               }
               else if (e.getActionCommand().equals(DOWN))
               {
                  moveItemDown();
               }
            }
         };
      }
      ToolbarButton button = new ToolbarButton();
      if (icon != null)
      {
         button.setIcon(GUI.getIcon(icon));
      }
      if (text != null)
      {
         button.setText(text);
      }
      else
      {
         button.setMargin(GUI.noInsets);
         button.setToolTipText(command);
      }
      button.setEnabled(false);
      button.setActionCommand(command);
      button.addActionListener(listener);
      buttons.put(command, button);
      return button;
   }

   protected boolean isButtonEnabled(String command)
   {
      ToolbarButton button = (ToolbarButton) buttons.get(command);
      return button == null ? false : button.isEnabled();
   }

   protected void setButtonEnabled(String command, boolean enabled)
   {
      ToolbarButton button = (ToolbarButton) buttons.get(command);
      if (button != null)
      {
         button.setEnabled(isEnabled() && enabled);
      }
   }

   protected abstract void moveItemDown();

   protected abstract void moveItemUp();

   protected abstract void removeItem();

   protected abstract void addItem();

   protected abstract Component createListPanel();

   protected abstract Component createDetailsPanel();

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      setButtonEnabled(ADD, enabled);
      // disable all the buttons ???
   }
}
