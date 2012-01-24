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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.EventActionPropertiesPanel;
import org.eclipse.stardust.engine.core.extensions.actions.delegate.TargetWorklist;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.model.gui.SymbolIconProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DelegateEventActionPanel extends EventActionPropertiesPanel
      implements ActionListener
{
   private static final String PERFORMER_ACTION = "performer";
   private static final String USER_ACTION = "currentUser";
   private static final String PARTICIPANT_ACTION = "participant";
   private static final String RANDOMUSER_ACTION = "randomUser";

   private CardLayout layout;
   private JPanel details;
   private JRadioButton defaultPerformerButton;
   private JRadioButton currentUserButton;
   private JRadioButton participantButton;
   private JRadioButton randomUserButton;

   private JList participantsList;
   private ListSelectionListener listener;
   private IAction action;

   public DelegateEventActionPanel()
   {
      setLayout(new BorderLayout());
      add(getSelectorPanel(), BorderLayout.NORTH);

      details = new JPanel(layout = new CardLayout());
      details.add(new JPanel(), PERFORMER_ACTION);
      details.add(new JPanel(), USER_ACTION);
      details.add(getModelParticipantPanel(), PARTICIPANT_ACTION);
      details.add(new JPanel(), RANDOMUSER_ACTION);
      add(details);
   }

   private JPanel getModelParticipantPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(new JLabel("Participants:"), BorderLayout.NORTH);
      panel.add(new JScrollPane(participantsList = new JList(new DefaultListModel())));
      participantsList.setCellRenderer(new IdentifiableComboBoxRenderer(new SymbolIconProvider()));
      participantsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      participantsList.addListSelectionListener(listener = new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            IModelParticipant def = (IModelParticipant) participantsList.getSelectedValue();
            action.setDescription(def == null ? null : def.getName());
         }
      });
      return panel;

   }

   private JPanel getSelectorPanel()
   {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
      panel.setBorder(BorderFactory.createEtchedBorder());

      defaultPerformerButton = new JRadioButton("Default Performer");
      panel.add(defaultPerformerButton);
      defaultPerformerButton.setActionCommand(PERFORMER_ACTION);
      defaultPerformerButton.addActionListener(this);

      currentUserButton = new JRadioButton("Current User");
      panel.add(currentUserButton);
      currentUserButton.setActionCommand(USER_ACTION);
      currentUserButton.addActionListener(this);

      participantButton = new JRadioButton("Participant");
      panel.add(participantButton);
      participantButton.setActionCommand(PARTICIPANT_ACTION);
      participantButton.addActionListener(this);

      randomUserButton = new JRadioButton("Random User");
      panel.add(randomUserButton);
      randomUserButton.setActionCommand(RANDOMUSER_ACTION);
      randomUserButton.addActionListener(this);

      ButtonGroup group = new ButtonGroup();
      group.add(defaultPerformerButton);
      group.add(currentUserButton);
      group.add(participantButton);
      group.add(randomUserButton);

      JPanel outer = new JPanel(new BorderLayout());
      outer.setBorder(BorderFactory.createEmptyBorder(
            0, 0, GUI.VerticalWidgetDistance, 0));
      outer.add(new JLabel("Send To Worklist Of:"), BorderLayout.NORTH);
      outer.add(panel);
      return outer;
   }

   public void setData(EventHandlerOwner owner, IAction action)
   {
      this.action = action;

      TargetWorklist target = (TargetWorklist) action.getAttribute(
            PredefinedConstants.TARGET_WORKLIST_ATT);

      // fill in participant list
      participantsList.removeListSelectionListener(listener);
      String participantId = null;
      if (target == TargetWorklist.Participant)
      {
         participantId = action.getStringAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT);
      }
      List list = new ArrayList();
      IModelParticipant participant = null;
      cumulateParticipants(((IActivity) owner).getPerformer(), list);
      Collections.sort(list, new Comparator()
      {
         public int compare(Object o1, Object o2)
         {
            return ((IModelParticipant) o1).getName().compareTo(((IModelParticipant) o2).getName());
         }
      });
      DefaultListModel model = (DefaultListModel) participantsList.getModel();
      model.clear();
      for (int i = 0; i < list.size(); i++)
      {
         IModelParticipant p = (IModelParticipant) list.get(i);
         if ((null != participantId) && participantId.equals(p.getId()))
         {
            participant = p;
         }
         model.addElement(p);
      }
      if (target == TargetWorklist.Participant)
      {
         participantsList.setSelectedValue(participant, true);
      }
      participantsList.addListSelectionListener(listener);

      if (target == TargetWorklist.CurrentUser)
      {
         currentUserButton.setSelected(true);
         layout.show(details, USER_ACTION);
      }
      else if (target == TargetWorklist.Participant)
      {
         participantButton.setSelected(true);
         layout.show(details, PARTICIPANT_ACTION);
      }
      else if (target == TargetWorklist.RandomUser)
      {
         randomUserButton.setSelected(true);
         layout.show(details, RANDOMUSER_ACTION);
      }
      else
      {
         defaultPerformerButton.setSelected(true);
         layout.show(details, PERFORMER_ACTION);
      }
   }

   private void cumulateParticipants(IModelParticipant performer, List list)
   {
      if (!list.contains(performer))
      {
         list.add(performer);
      }
      if (performer instanceof IOrganization)
      {
         IOrganization org = (IOrganization) performer;
         for (Iterator i = org.getAllParticipants(); i.hasNext();)
         {
            cumulateParticipants((IModelParticipant) i.next(), list);
         }
      }
   }

   public void apply()
   {
      action.removeAllAttributes();
      if (defaultPerformerButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT,
               TargetWorklist.DefaultPerformer);
      }
      else if (currentUserButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT,
               TargetWorklist.CurrentUser);
      }
      else if (participantButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT,
               TargetWorklist.Participant);
         IModelParticipant def = (IModelParticipant) participantsList.getSelectedValue();
         if (def != null)
         {
            action.setAttribute(PredefinedConstants.TARGET_PARTICIPANT_ATT, def.getId());
         }
      }
      else if (randomUserButton.isSelected())
      {
         action.setAttribute(PredefinedConstants.TARGET_WORKLIST_ATT,
               TargetWorklist.RandomUser);
      }
   }

   public void actionPerformed(ActionEvent e)
   {
      layout.show(details, e.getActionCommand());
      if (e.getActionCommand().equals(PARTICIPANT_ACTION))
      {
         IModelParticipant def = (IModelParticipant) participantsList.getSelectedValue();
         action.setDescription(def == null ? null : def.getName());
      }
      else
      {
         action.setDescription(null);
      }
   }
}
