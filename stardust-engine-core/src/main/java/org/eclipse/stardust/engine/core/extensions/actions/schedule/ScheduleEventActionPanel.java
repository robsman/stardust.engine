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
package org.eclipse.stardust.engine.core.extensions.actions.schedule;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;

import org.eclipse.stardust.engine.api.model.EventHandlerOwner;
import org.eclipse.stardust.engine.api.model.IAction;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.EventActionPropertiesPanel;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ScheduleEventActionPanel extends EventActionPropertiesPanel
{
   private static final String SUSPENDED_ACTION = "suspended";
   private static final String HIBERNATED_ACTION = "hibernated";
//   private static final String UNCHANGED_ACTION = "unchanged";

   private JRadioButton suspendedButton;
   private JRadioButton hibernatedButton;
//   private JRadioButton unchangedButton;

   private IAction action;

   public ScheduleEventActionPanel()
   {
      setLayout(new BorderLayout());
      add(getStatePanel(), BorderLayout.NORTH);
   }

   private JPanel getStatePanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setBorder(BorderFactory.createEtchedBorder());
      panel.add(suspendedButton = new JRadioButton("Suspended"));
      suspendedButton.setActionCommand(SUSPENDED_ACTION);
//      suspendedButton.addActionListener(this);
      panel.add(hibernatedButton = new JRadioButton("Hibernated"));
      hibernatedButton.setActionCommand(HIBERNATED_ACTION);
//      hibernatedButton.addActionListener(this);
//      panel.add(unchangedButton = new JRadioButton("Unchanged"));
//      unchangedButton.setActionCommand(UNCHANGED_ACTION);
//      unchangedButton.addActionListener(this);
      ButtonGroup group = new ButtonGroup();
      group.add(suspendedButton);
      group.add(hibernatedButton);
//      group.add(unchangedButton);

      JPanel outer = new JPanel(new BorderLayout());
      outer.setBorder(BorderFactory.createEmptyBorder(
            GUI.VerticalWidgetDistance, 0, GUI.VerticalWidgetDistance, 0));
      outer.add(new JLabel("Intended State Change:"), BorderLayout.NORTH);
      outer.add(panel);
      return outer;
   }

   public void setData(EventHandlerOwner owner, IAction action)
   {
      this.action = action;

      // set intended state data
      ActivityInstanceState state = (ActivityInstanceState)
            action.getAttribute(PredefinedConstants.TARGET_STATE_ATT);
      if (state == ActivityInstanceState.Hibernated)
      {
         hibernatedButton.setSelected(true);
      }
      else //if (state == ActivityInstanceState.Suspended)
      {
         suspendedButton.setSelected(true);
      }
/*      else
      {
         unchangedButton.setSelected(true);
      }*/
   }

   public void apply()
   {
      action.removeAllAttributes();
      if (hibernatedButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_STATE_ATT,
               ActivityInstanceState.Hibernated);
      }
      else //if (suspendedButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_STATE_ATT,
               ActivityInstanceState.Suspended);
      }
   }
}
