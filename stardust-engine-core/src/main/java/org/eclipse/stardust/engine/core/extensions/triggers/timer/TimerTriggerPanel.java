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
package org.eclipse.stardust.engine.core.extensions.triggers.timer;

import java.util.*;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.error.ValidationException;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.model.ITrigger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.DateTimeEntry;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.Mandatory;
import org.eclipse.stardust.engine.core.compatibility.gui.utils.PeriodEntry;
import org.eclipse.stardust.engine.core.compatibility.spi.model.gui.TriggerPropertiesPanel;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class TimerTriggerPanel extends TriggerPropertiesPanel
{
   private static final String LABEL_START_TIME = "Start Timestamp:";
   private static final String LABEL_IS_PERIODICAL = "Periodical:";
   private static final String LABEL_STOP_TIME = "Stop Timestamp:";
   private static final String LABEL_PERIODICITY = "Periodicity:";

   private DateTimeEntry startTimeEntry;
   private JCheckBox isPeriodicalEntry;
   private DateTimeEntry stopTimeEntry;
   private PeriodEntry periodicityEntry;

   private ITrigger trigger;

   public TimerTriggerPanel()
   {
      startTimeEntry = new DateTimeEntry();
      isPeriodicalEntry = new JCheckBox();
      isPeriodicalEntry.addChangeListener(new ChangeListener()
      {
         public void stateChanged(ChangeEvent e)
         {
            stopTimeEntry.setEnabled(isEnabled() && isPeriodicalEntry.isSelected());
            stopTimeEntry.setEditable(isEnabled() && isPeriodicalEntry.isSelected());
            periodicityEntry.setEnabled(isEnabled() && isPeriodicalEntry.isSelected());
            periodicityEntry.setEditable(isEnabled() && isPeriodicalEntry.isSelected());
         }
      });

      stopTimeEntry = new DateTimeEntry();

      periodicityEntry = new PeriodEntry();

      isPeriodicalEntry.setSelected(true);

      JLabel startTimeLabel = new JLabel(LABEL_START_TIME);
      startTimeLabel.setLabelFor(startTimeEntry);
      JLabel isPeriodicalLabel = new JLabel(LABEL_IS_PERIODICAL);
      isPeriodicalLabel.setLabelFor(isPeriodicalEntry);
      JLabel stopTimeLabel = new JLabel(LABEL_STOP_TIME);
      stopTimeLabel.setLabelFor(stopTimeEntry);
      JLabel periodicityLabel = new JLabel(LABEL_PERIODICITY);
      periodicityLabel.setLabelFor(periodicityEntry);

      FormLayout layout = new FormLayout("right:max(40dlu;pref), pref, left:100dlu:grow",
            "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");

      setLayout(layout);
      setBorder(Borders.DIALOG_BORDER);

      CellConstraints cc = new CellConstraints();

      add(startTimeLabel, cc.xy(1, 1));
      add(new Mandatory("", startTimeEntry, true), cc.xy(2, 1));
      add(startTimeEntry, cc.xy(3, 1));

      add(isPeriodicalLabel, cc.xy(1, 3));
      add(isPeriodicalEntry, cc.xy(3, 3));

      add(stopTimeLabel, cc.xy(1, 5));
      add(stopTimeEntry, cc.xy(3, 5));

      add(periodicityLabel, cc.xy(1, 7));
      add(periodicityEntry, cc.xy(3, 7));
   }

   public void setData(IProcessDefinition processDefinition, ITrigger trigger)
   {
      this.trigger = trigger;
      Map attributes = Collections.EMPTY_MAP;
      if (null != trigger)
      {
         attributes = trigger.getAllAttributes();
      }

      Long ts = (Long) attributes.get(PredefinedConstants
            .TIMER_TRIGGER_START_TIMESTAMP_ATT);
      if (null != ts)
      {
         Calendar startTime = Calendar.getInstance();
         startTime.setTime(new Date(ts.longValue()));
         startTimeEntry.setCalendar(startTime);
      }
      else
      {
         startTimeEntry.setCalendar(null);
      }

      Period periodicity = (Period) attributes.get(PredefinedConstants
            .TIMER_TRIGGER_PERIODICITY_ATT);
      if (null != periodicity)
      {
         isPeriodicalEntry.setSelected(true);
         periodicityEntry.setPeriod(periodicity);

         Long stopTs = (Long) attributes.get(PredefinedConstants
               .TIMER_TRIGGER_STOP_TIMESTAMP_ATT);
         if (null != stopTs)
         {
            Calendar stopTime = Calendar.getInstance();
            stopTime.setTime(new Date(stopTs.longValue()));
            stopTimeEntry.setCalendar(stopTime);
         }
         else
         {
            stopTimeEntry.setCalendar(null);
         }
      }
      else
      {
         isPeriodicalEntry.setSelected(false);
         periodicityEntry.setPeriod(null);
         stopTimeEntry.setCalendar(null);
      }
   }

   public void apply()
   {
      Calendar startTime = startTimeEntry.getCalendar();
      if (null != startTime)
      {
         trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_START_TIMESTAMP_ATT,
               new Long(startTime.getTime().getTime()));
      }

      if (isPeriodicalEntry.isSelected())
      {
         Period periodicity = periodicityEntry.getPeriod();
         if (null != periodicity)
         {
            trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_PERIODICITY_ATT,
                  periodicity);

            Calendar stopTime = stopTimeEntry.getCalendar();
            if (null != stopTime)
            {
               trigger.setAttribute(PredefinedConstants.TIMER_TRIGGER_STOP_TIMESTAMP_ATT,
                     new Long(stopTime.getTime().getTime()));
            }
            else
            {
               trigger.removeAttribute(PredefinedConstants
                     .TIMER_TRIGGER_STOP_TIMESTAMP_ATT);
            }
         }
         else
         {
            trigger.removeAttribute(PredefinedConstants.TIMER_TRIGGER_PERIODICITY_ATT);
            trigger.removeAttribute(PredefinedConstants.TIMER_TRIGGER_STOP_TIMESTAMP_ATT);
         }
      }
      else
      {
         trigger.removeAttribute(PredefinedConstants.TIMER_TRIGGER_PERIODICITY_ATT);
         trigger.removeAttribute(PredefinedConstants.TIMER_TRIGGER_STOP_TIMESTAMP_ATT);
      }
      for (Iterator i = trigger.getAllAccessPoints(); i.hasNext();)
      {
         trigger.removeFromAccessPoints((org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint) i.next());
      }
   }

   public void validateSettings() throws ValidationException
   {
      Long ts = (Long) trigger.getAttribute(PredefinedConstants
            .TIMER_TRIGGER_START_TIMESTAMP_ATT);

      if (null == ts)
      {
         throw new ValidationException("Please specify the start timestamp for the"
               + " timer trigger.", false);
      }
   }
}
