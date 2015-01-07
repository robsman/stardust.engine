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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JPanel;

/**
 * @author Claude Duguay
 * @author rsauer
 * @version $Revision$
 */
public class CalendarHeader extends JPanel
{
   private CalendarRenderer renderer;
   private CellRendererPane renderPane = new CellRendererPane();

   private static final String[] header =
         {"S", "M", "T", "W", "T", "F", "S"};

   private double xunit = 0;
   private double yunit = 0;

   public CalendarHeader(CalendarRenderer renderer)
   {
      this.renderer = renderer;
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, renderPane);
   }

   public void paintComponent(Graphics g)
   {
      xunit = getSize().width / 7;
      yunit = getSize().height;
      for (int x = 0; x < 7; x++)
      {
         drawCell(g, (int) (x * xunit), 0,
               (int) xunit, (int) yunit, header[x], false);
      }
   }

   private void drawCell(Graphics g, int x, int y,
         int w, int h, String text, boolean selected)
   {
      Component render = renderer.
            getCalendarRendererComponent(this, text, selected, false);
      renderPane.paintComponent(g, render, this, x, y, w, h);
   }

   public boolean isFocusTraversable()
   {
      return false;
   }

   public Dimension getPreferredSize()
   {
      Dimension dimension =
            ((Component) renderer).getPreferredSize();
      int width = dimension.width * 7;
      int height = dimension.height;
      return new Dimension(width, height);
   }

   public Dimension getMinimumSize()
   {
      Dimension dimension =
            ((Component) renderer).getMinimumSize();
      int width = dimension.width * 7;
      int height = dimension.height;
      return new Dimension(width, height);
   }
}