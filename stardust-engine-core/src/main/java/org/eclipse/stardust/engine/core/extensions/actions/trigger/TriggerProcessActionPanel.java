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
package org.eclipse.stardust.engine.core.extensions.actions.trigger;

import java.awt.*;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.Mandatory;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.EventActionPropertiesPanel;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.model.gui.SymbolIconProvider;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;


public class TriggerProcessActionPanel extends EventActionPropertiesPanel
{
   private JList processList;
   private IAction action;
   private ListSelectionListener listener;

   public TriggerProcessActionPanel()
   {
      processList = new JList();

      CellConstraints cc = new CellConstraints();
      setLayout(new FormLayout("default, pref, pref:grow", "default, fill:pref:grow"));

      add(new JLabel("Triggered Process:"), cc.xy(1, 1));
      add(new Mandatory("No process selected.", processList, true), cc.xy(2, 1));
      JScrollPane scroller = new JScrollPane(processList);
      scroller.setPreferredSize(new Dimension(250, 350));
      add(scroller, cc.xywh(1, 2, 3, 1));
      processList.setCellRenderer(new IdentifiableComboBoxRenderer(new SymbolIconProvider()));
      processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      processList.addListSelectionListener(listener = new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            apply();
         }
      });
   }

   public void setData(EventHandlerOwner owner, IAction action)
   {
      this.action = action;
      processList.removeListSelectionListener(listener);

      String processId = action.getStringAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
      IProcessDefinition pd = null;
      Vector processDefinitions = new Vector();
      IModel model = (IModel) owner.getModel();
      for (Iterator i = model.getAllProcessDefinitions(); i.hasNext();)
      {
         IProcessDefinition def = (IProcessDefinition) i.next();
         if (def.getId().equals(processId))
         {
            pd = def;
         }
         processDefinitions.add(def);
      }
      processList.setListData(processDefinitions);
      processList.setSelectedValue(pd, true);

      processList.addListSelectionListener(listener);
   }

   public void apply()
   {
      IProcessDefinition def = (IProcessDefinition) processList.getSelectedValue();
      if (def != null)
      {
         action.setAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT, def.getId());
         action.setDescription(def.getName());
      }
      else
      {
         action.removeAttribute(PredefinedConstants.TRIGGER_ACTION_PROCESS_ATT);
         action.setDescription(null);
      }
   }

}
