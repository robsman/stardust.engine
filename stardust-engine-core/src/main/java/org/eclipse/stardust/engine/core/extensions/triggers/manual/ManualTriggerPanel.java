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
package org.eclipse.stardust.engine.core.extensions.triggers.manual;

import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.TriggerPropertiesPanel;
import org.eclipse.stardust.engine.core.model.gui.IdentifiableComboBoxRenderer;
import org.eclipse.stardust.engine.core.model.gui.SymbolIconProvider;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class ManualTriggerPanel extends TriggerPropertiesPanel
{
   private JList list;
   private ITrigger trigger;

   public ManualTriggerPanel()
   {
      setLayout(new BorderLayout());
      list = new JList(new DefaultListModel());
      list.setCellRenderer(new IdentifiableComboBoxRenderer(new SymbolIconProvider()));
      list.addListSelectionListener(new ListSelectionListener()
      {
         public void valueChanged(ListSelectionEvent e)
         {
            if (trigger != null)
            {
               IModelParticipant participant = (IModelParticipant) list.getSelectedValue();
               trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT,
                     participant.getId());
            }
         }
      });
      add(new JScrollPane(list), BorderLayout.CENTER);
   }

   public void setData(IProcessDefinition processDefinition, ITrigger trigger)
   {
      this.trigger = null;
      if (processDefinition == null)
      {
         return;
      }

      Map attributes = Collections.EMPTY_MAP;
      if (null != trigger)
      {
         attributes = trigger.getAllAttributes();
      }

      String participantId = (String) attributes.get(
            PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
      IModelParticipant participant = null;
      DefaultListModel model = (DefaultListModel) list.getModel();
      model.clear();
      IModel root = (IModel) processDefinition.getModel();
      for (Iterator i = root.getAllWorkflowParticipants(); i.hasNext();)
      {
         IModelParticipant p = (IModelParticipant) i.next();
         if (p.getId().equals(participantId))
         {
            participant = p;
         }
         model.addElement(p);
      }

      if (participant != null)
      {
         list.setSelectedValue(participant,  true);
      }

      this.trigger = trigger;
   }

   public void apply()
   {
      IModelParticipant participant = (IModelParticipant) list.getSelectedValue();
      if (participant == null)
      {
         trigger.removeAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT);
      }
      else
      {
         trigger.setAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT,
               participant.getId());
      }
      for (Iterator i = trigger.getAllAccessPoints(); i.hasNext();)
      {
         trigger.removeFromAccessPoints((org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint) i.next());
      }
   }

   public void validateSettings() throws ValidationException
   {
      if (trigger.getAttribute(PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT) == null)
      {
         throw new ValidationException("No participant set for trigger.", false);
      }
   }
}
