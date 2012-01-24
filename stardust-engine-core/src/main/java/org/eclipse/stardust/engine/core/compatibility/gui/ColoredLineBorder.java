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
package org.eclipse.stardust.engine.core.compatibility.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/** Provides a colored line border */
public class ColoredLineBorder implements Border
{
   protected static Insets insets = new Insets(2, 2, 2, 2);

   protected Color colors[];

   /** Default contructor generates a black line border
    */
   public ColoredLineBorder()
   {
      this(Color.black);
   }

   /** Constructor that accepts a color object
    *  for every side of the borders rectangle
    *  @param color The color for the line
    */
   public ColoredLineBorder(Color color)
   {
      this(color, color, color, color);
   }

   /** Constructor that accepts four separate colors
    *  for each side of the borders rectangle
    *  @param top The color for the top side of the border
    *  @param left The color for the left side of the border
    *  @param top The color for the top side of the border
    *  @param right The color for the right side of the border
    */
   public ColoredLineBorder(Color top, Color left, Color bottom, Color right)
   {
      colors = new Color[]{top, left, bottom, right};
   }

   /** Paints the border. Needed when implementing the Border interface
    */
   public void paintBorder(Component component, Graphics g,
         int x, int y, int width, int height)
   {
      // store old color
      Color oldColor = g.getColor();

      // precalculate bottom x and right y

      int x2 = x + width - 1;
      int y2 = y + height - 1;

      // top

      if (colors[0] != null)
      {
         g.setColor(colors[0]);
         g.drawLine(x, y, x2, y);
      }

      // left

      if (colors[1] != null)
      {
         g.setColor(colors[1]);
         g.drawLine(x, y, x, y2);
      }

      // bottom

      if (colors[2] != null)
      {
         g.setColor(colors[2]);
         g.drawLine(x, y2, x2, y2);
      }

      // right

      if (colors[3] != null)
      {
         g.setColor(colors[3]);
         g.drawLine(x2, y, x2, y2);
      }

      // set old color

      g.setColor(oldColor);
   }

   /** Gets the border insets */
   public Insets getBorderInsets(Component component)
   {
      return insets;
   }

   /** Indicates whether its opaque or not */
   public boolean isBorderOpaque()
   {
      return true;
   }

}