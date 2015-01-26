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

import java.awt.*;
import java.text.DateFormat;

import javax.swing.*;

import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.compatibility.gui.AbstractDialog;
import org.eclipse.stardust.engine.core.compatibility.gui.GUI;


/**
 *
 */
public class ActivityInstanceDetailsDialog extends AbstractDialog
{
   private static ActivityInstanceDetailsDialog instance;

   private ActivityInstance activityInstanceDetails;
   private JPanel panel;

   protected ActivityInstanceDetailsDialog()
   {
      super(AbstractDialog.CLOSE_TYPE);
   }

   protected ActivityInstanceDetailsDialog(Frame parent)
   {
      super(AbstractDialog.CLOSE_TYPE, parent);
   }

   public JComponent createContent()
   {
      panel = new JPanel();

      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(GUI.getEmptyPanelBorder());

      return panel;
   }

   public void validateSettings() throws ValidationException
   {
   }

   public void setData(ActivityInstance activityInstance)
   {
      this.activityInstanceDetails = activityInstance;

      panel.removeAll();
      panel.add(new JLabel("Process Definition ID: " +
            activityInstance.getProcessDefinitionId()));
      panel.add(new JLabel("Activity ID: " +
            activityInstance.getActivity().getId()));
      panel.add(new JLabel("Activity Name: " +
            activityInstance.getActivity().getName()));
      DateFormat dateFormat = DateFormat.getDateInstance();
      DateFormat timeFormat = DateFormat.getTimeInstance();

      panel.add(new JLabel("Start Time: " +
            (dateFormat.format(activityInstance.getStartTime()) + " " +
                                    timeFormat.format(activityInstance.getStartTime()))));
      DateFormat dateFormat1 = DateFormat.getDateInstance();
      DateFormat timeFormat1 = DateFormat.getTimeInstance();

      panel.add(new JLabel("Last Modification Time: " +
            (dateFormat1.format(activityInstance.getLastModificationTime()) + " " +
                                    timeFormat1.format(activityInstance.getLastModificationTime()))));
      panel.add(new JLabel("Duration: " +
            (1.0 * (activityInstance.getLastModificationTime().getTime()
            - activityInstance.getStartTime().getTime())) / 1000 / 60 / 60
            + " hours"));
      panel.add(new JLabel("Type: " +
            activityInstance.getActivity().getImplementationType()));
      panel.add(new JLabel("Performer: " +
            (activityInstance.isAssignedToUser() ? activityInstance.getUserPerformerName()
      : activityInstance.getParticipantPerformerName())));
   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(ActivityInstance ai)
   {
      return showDialog(ai, null);
   }

   /**
    * @return boolean The flag "closedWithOk"
    */
   public static boolean showDialog(ActivityInstance ai, Frame parent)
   {
      if (instance == null)
      {
         instance = new ActivityInstanceDetailsDialog(parent);
      }

      instance.setData(ai);

      return showDialog("Activity Instance Details", instance);
   }
}
