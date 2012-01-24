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
package org.eclipse.stardust.engine.core.extensions.actions.delegate;

import java.awt.*;

import javax.swing.*;

import org.eclipse.stardust.engine.api.model.EventAware;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.EventActionBinding;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.spi.runtime.gui.RuntimeActionPanel;
import org.eclipse.stardust.engine.core.extensions.actions.delegate.TargetWorklist;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.runtime.gui.RuntimeIconProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DelegateEventActionRuntimePanel extends RuntimeActionPanel
{
   private static final String PERFORMER_ACTION = "performer";
   private static final String USER_ACTION = "user";
   private static final String SPECIFY_USER_ACTION = "specifyUser";
   private static final String PARTICIPANT_ACTION = "participant";

   private CardLayout layout;
   private JPanel details;

   private JRadioButton defaultPerformerButton;
   private JRadioButton currentUserButton;
   private JRadioButton participantButton;
   private JRadioButton userButton;

   private JList participantsList;
   private EventActionBinding action;
   private JList usersList;

   public DelegateEventActionRuntimePanel()
   {
      setLayout(new BorderLayout());
      add(getSelectorPanel(), BorderLayout.NORTH);

      details = new JPanel(layout = new CardLayout());
      details.add(new JPanel(), PERFORMER_ACTION);
      details.add(new JPanel(), USER_ACTION);
      details.add(getParticipantPanel(), PARTICIPANT_ACTION);
      details.add(getUserPanel(), SPECIFY_USER_ACTION);
      add(details);
   }

   private JPanel getParticipantPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel("Participants:"), BorderLayout.NORTH);
      panel.add(new JScrollPane(participantsList = new JList()));
      participantsList.setCellRenderer(new IdentifiableComboBoxRenderer(new RuntimeIconProvider())
      {
         protected String getText(Object value)
         {
            Participant par = (Participant) value;
            return par.getName();
         }
      });
      participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      return panel;

   }

   private JPanel getUserPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel("Users:"), BorderLayout.NORTH);
      panel.add(new JScrollPane(usersList = new JList()));
      usersList.setCellRenderer(new IdentifiableComboBoxRenderer(new RuntimeIconProvider())
      {
         protected String getText(Object value)
         {
            User user = (User) value;
            return user.getAccount() + " : "
                  + user.getLastName() + ", " + user.getFirstName();
         }
      });
      usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      return panel;

   }

   private JPanel getSelectorPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
//      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      panel.setBorder(BorderFactory.createEtchedBorder());
      panel.add(defaultPerformerButton = new JRadioButton("Default Performer"));
      defaultPerformerButton.setActionCommand(PERFORMER_ACTION);
      panel.add(currentUserButton = new JRadioButton("Current User"));
      currentUserButton.setActionCommand(USER_ACTION);
      panel.add(participantButton = new JRadioButton("Participant"));
      participantButton.setActionCommand(PARTICIPANT_ACTION);
      panel.add(userButton = new JRadioButton("User"));
      userButton.setActionCommand(SPECIFY_USER_ACTION);

      ButtonGroup group = new ButtonGroup();
      group.add(defaultPerformerButton);
      group.add(currentUserButton);
      group.add(participantButton);
      group.add(userButton);

      JPanel outer = new JPanel(new BorderLayout());
      outer.setBorder(BorderFactory.createEmptyBorder(
            0, 0, GUI.VerticalWidgetDistance, 0));
      outer.add(new JLabel("Send To Worklist Of:"), BorderLayout.NORTH);
      outer.add(panel);
      return outer;
   }

   public void setData(EventAware owner, EventActionBinding action)
   {
      this.action = action;

      // set worklist data
      TargetWorklist target = (TargetWorklist) action.getAttribute(
            PredefinedConstants.TARGET_WORKLIST_ATT);
      if (target == null || target.equals(TargetWorklist.DefaultPerformer))
      {
         defaultPerformerButton.setSelected(true);
         layout.show(details, PERFORMER_ACTION);
      }
      else if (target.equals(TargetWorklist.CurrentUser))
      {
         currentUserButton.setSelected(true);
         layout.show(details, USER_ACTION);
      }
      else if (target.equals(TargetWorklist.Participant))
      {
         participantButton.setSelected(true);
         layout.show(details, PARTICIPANT_ACTION);
         Participant participant = null;
         String participantId = (String)
               action.getAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT);
         // todo: set participant data
         participantsList.setSelectedValue(participant, true);
      }
   }

   public void apply()
   {
      if (action != null)
      {
         if (defaultPerformerButton.isSelected())
         {
            action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, TargetWorklist.DefaultPerformer);
         }
         else if (currentUserButton.isSelected())
         {
            action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, TargetWorklist.CurrentUser);
         }
         else if (participantButton.isSelected())
         {
            action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, TargetWorklist.Participant);
            Participant def = (Participant) participantsList.getSelectedValue();
            if (def != null)
            {
               action.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, def.getId());
            }
         }
         else if (userButton.isSelected())
         {
            action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT, TargetWorklist.RandomUser);
            User def = (User) usersList.getSelectedValue();
            if (def != null)
            {
               action.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, def.getAccount());
            }
         }
      }
   }

   public void validateSettings()
   {
   }
}
