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

import java.awt.Component;
import java.text.FieldPosition;
import java.text.Format;

import javax.swing.JTextField;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DefaultSpinRenderer extends JTextField
      implements SpinRenderer
{
   public DefaultSpinRenderer()
   {
      setOpaque(true);
      setEditable(false);
   }

   public DefaultSpinRenderer(int columns)
   {
      super(columns);

      setOpaque(true);
      setEditable(false);
   }

   public Component getSpinCellRendererComponent(JSpinnerField spin, Object value, boolean hasFocus,
         Format formatter, int selectedFieldID)
   {
      String text = formatter.format(value);
      setText(text);
      FieldPosition pos = LocaleUtil.getFieldPosition(formatter, value, selectedFieldID);
      // Make non-selections expand to full selections
      if (pos.getBeginIndex() == pos.getEndIndex())
      {
         pos.setBeginIndex(0);
         pos.setEndIndex(text.length());
      }
      if (hasFocus)
         select(pos.getBeginIndex(), pos.getEndIndex());
      else
         select(0, 0);
      return this;
   }
}
