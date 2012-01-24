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
package org.eclipse.stardust.engine.core.compatibility.gui.utils.calendar;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.Border;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class DefaultCalendarRenderer extends JLabel implements CalendarRenderer
{
   private boolean isHeader = false;

   private static final Border raised =
         new ThinBorder(ThinBorder.RAISED);
   private static final Border lowered =
         new ThinBorder(ThinBorder.LOWERED);

   public DefaultCalendarRenderer(boolean isHeader)
   {
      setOpaque(true);
      setBorder(raised);
      setVerticalAlignment(JLabel.TOP);
      setHorizontalAlignment(isHeader ? JLabel.CENTER : JLabel.LEFT);
      setPreferredSize(new Dimension(19, 18));
      setMinimumSize(new Dimension(19, 18));
      this.isHeader = isHeader;
   }

   public Color getBackdrop()
   {
      return Color.lightGray;
   }

   public Component getCalendarRendererComponent(JComponent parent, Object value,
         boolean isSelected, boolean hasFocus)
   {
      setText(value.toString());
      if (isSelected)
      {
         setBorder(lowered);
         setBackground(hasFocus ? Color.blue : Color.lightGray);
         setForeground(hasFocus ? Color.white : Color.black);
      }
      else
      {
         setBorder(raised);
         setBackground(isHeader ?
               Color.gray : Color.lightGray);
         setForeground(Color.black);
      }
      return this;
   }
}