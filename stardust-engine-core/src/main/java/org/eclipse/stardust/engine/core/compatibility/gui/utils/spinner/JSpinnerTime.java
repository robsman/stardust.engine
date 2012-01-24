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

import java.text.DateFormat;
import java.util.Locale;
import java.util.Calendar;

/**
 * @author rsauer
 * @version $Revision$
 */
public class JSpinnerTime extends JSpinnerField
{
   public JSpinnerTime()
   {
      this(Calendar.getInstance());
   }

   public JSpinnerTime(SpinRenderer renderer, DateFormat format)
   {
      this(Calendar.getInstance(), renderer, format);
   }

   public JSpinnerTime(Calendar time)
   {
      super(new TimeSpinModel(), new DefaultSpinRenderer(),
            DateFormat.getTimeInstance(DateFormat.MEDIUM), true);
      getTimeModel().setTime(time);
      refreshSpinView();
   }

   public JSpinnerTime(Calendar time, SpinRenderer renderer, DateFormat format)
   {
      super(new TimeSpinModel(), renderer, format, true);
      getTimeModel().setTime(time);
      refreshSpinView();
   }

   public void setLocale(Locale locale)
   {
      setFormat(DateFormat.getTimeInstance(DateFormat.MEDIUM, locale));
   }

   public void setFormat(DateFormat formatter)
   {
      this.formatter = formatter;

      updateFieldOrder();
   }

   public TimeSpinModel getTimeModel()
   {
      return (TimeSpinModel) model;
   }

   protected void refreshSpinView()
   {
      spinField.setValue(getTimeModel().getTime().getTime());
   }

}
