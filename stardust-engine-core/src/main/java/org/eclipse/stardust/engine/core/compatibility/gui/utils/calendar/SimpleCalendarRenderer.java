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
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.basic.BasicGraphicsUtils;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class SimpleCalendarRenderer extends JLabel implements CalendarRenderer
{
   private boolean hasFocus = false;
   private boolean isSelected = false;
   private boolean isHeader = false;

   public SimpleCalendarRenderer(boolean isHeader)
   {
      setOpaque(true);
      setBorder(null);
      setVerticalAlignment(JLabel.TOP);
      setHorizontalAlignment(JLabel.CENTER);
      setPreferredSize(new Dimension(19, 18));
      setMinimumSize(new Dimension(19, 18));
      this.isHeader = isHeader;
   }

   public Color getBackdrop()
   {
      return Color.white;
   }

   public Component getCalendarRendererComponent(JComponent parent, Object value,
         boolean isSelected, boolean hasFocus)
   {
      this.hasFocus = hasFocus;
      this.isSelected = isSelected;
      setText(value.toString());
      if (isSelected)
      {
         setBackground(Color.blue);
         setForeground(Color.white);
      }
      else
      {
         setBackground(Color.white);
         setForeground(Color.black);
      }
      return this;
   }

   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      int w = getSize().width;
      int h = getSize().height;
      if (!isHeader && hasFocus & isSelected)
      {
         g.setColor(Color.white);
         BasicGraphicsUtils.drawDashedRect(g, 0, 0, w, h);
      }
      if (isHeader) g.drawLine(0, h - 1, w, h - 1);
   }
}