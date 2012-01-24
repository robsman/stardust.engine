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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.spinner;

import java.util.Calendar;
import java.text.DateFormat;

/**
 * @author rsauer
 * @version $Revision$
 */
public class TimeSpinModel extends DefaultSpinModel
{
   private Calendar time = Calendar.getInstance();

   public TimeSpinModel()
   {
      setRange(DateFormat.HOUR_OF_DAY1_FIELD,
            new DefaultSpinRangeModel());
      setRange(DateFormat.MINUTE_FIELD,
            new DefaultSpinRangeModel());
      setRange(DateFormat.SECOND_FIELD,
            new DefaultSpinRangeModel());
      setActiveField(DateFormat.HOUR_OF_DAY1_FIELD);
      setTime(time);
   }

   public void setRange(int fieldID, SpinRangeModel range)
   {
      super.setRange(fieldID, range);
      if (fieldID == DateFormat.HOUR_OF_DAY1_FIELD)
      {
         time.set(Calendar.HOUR_OF_DAY, (int) range.getValue());
      }
      else if (fieldID == DateFormat.MINUTE_FIELD)
      {
         time.set(Calendar.MINUTE, (int) range.getValue());
      }
      else if (fieldID == DateFormat.SECOND_FIELD)
      {
         time.set(Calendar.SECOND, (int) range.getValue());
      }
   }

   public SpinRangeModel getRange(int fieldID)
   {
      SpinRangeModel range = super.getRange(fieldID);
      if (fieldID == DateFormat.HOUR_OF_DAY1_FIELD)
      {
         range.setExtent(1.0);
         range.setValue(time.get(Calendar.HOUR_OF_DAY));
         range.setMinimum(time.getActualMinimum(Calendar.HOUR_OF_DAY));
         range.setMaximum(time.getActualMaximum(Calendar.HOUR_OF_DAY));
      }
      else if (fieldID == DateFormat.MINUTE_FIELD)
      {
         range.setExtent(1.0);
         range.setValue(time.get(Calendar.MINUTE));
         range.setMinimum(time.getActualMinimum(Calendar.MINUTE));
         range.setMaximum(time.getActualMaximum(Calendar.MINUTE));
      }
      else if (fieldID == DateFormat.SECOND_FIELD)
      {
         range.setExtent(1.0);
         range.setValue(time.get(Calendar.SECOND));
         range.setMinimum(time.getActualMinimum(Calendar.SECOND));
         range.setMaximum(time.getActualMaximum(Calendar.SECOND));
      }
      return range;
   }

   public void setTime(Calendar time)
   {
      this.time = time;
      getRange(DateFormat.HOUR_OF_DAY1_FIELD);
      getRange(DateFormat.MINUTE_FIELD);
      getRange(DateFormat.SECOND_FIELD);
      fireStateChanged();
   }

   public Calendar getTime()
   {
      return time;
   }

   public static void main(String[] args)
   {
      TimeSpinModel model = new TimeSpinModel();
      int activeField = model.getActiveField();
      System.out.println(model.getRange(activeField));
      model.setNextField();
      activeField = model.getActiveField();
      System.out.println(model.getRange(activeField));
      model.setNextField();
      activeField = model.getActiveField();
      System.out.println(model.getRange(activeField));
      model.setNextField();
      activeField = model.getActiveField();
      System.out.println(model.getRange(activeField));
   }
}
