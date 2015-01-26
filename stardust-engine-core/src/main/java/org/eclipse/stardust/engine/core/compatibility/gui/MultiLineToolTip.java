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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/** Show tooltips that are longer than one line of code */
public class MultiLineToolTip extends JToolTip
{
   private static final String uiClassID = "ToolTipUI";

   String tipText;
   JComponent component;

   protected int columns = 0;
   protected int fixedwidth = 0;

   /** */
   public MultiLineToolTip()
   {
      updateUI();
   }

   /** */
   public void updateUI()
   {
      setUI(MultiLineToolTipUI.createUI(this));
   }

   /** */
   public void setColumns(int columns)
   {
      this.columns = columns;
      this.fixedwidth = 0;
   }

   /** */
   public int getColumns()
   {
      return columns;
   }

   /** */
   public void setFixedWidth(int width)
   {
      this.fixedwidth = width;
      this.columns = 0;
   }

   /** */
   public int getFixedWidth()
   {
      return fixedwidth;
   }
}// MultiLineToolTip

/** UI for displaying MultiLineToolTip */
class MultiLineToolTipUI extends BasicToolTipUI
{
   static MultiLineToolTipUI sharedInstance = new MultiLineToolTipUI();

   Font smallFont;
   static JToolTip tip;
   protected CellRendererPane rendererPane;

   private static JTextArea textArea;

   /** */
   public static ComponentUI createUI(JComponent c)
   {
      return sharedInstance;
   }

   /** */
   public MultiLineToolTipUI()
   {
      super();
   }

   /** */
   public void installUI(JComponent c)
   {
      super.installUI(c);

      tip = (JToolTip) c;
      rendererPane = new CellRendererPane();
      c.add(rendererPane);
   }

   /** */
   public void uninstallUI(JComponent c)
   {
      super.uninstallUI(c);

      c.remove(rendererPane);
      rendererPane = null;
   }

   /** */
   public void paint(Graphics g, JComponent c)
   {
      Dimension size = c.getSize();
      textArea.setBackground(c.getBackground());
      rendererPane.paintComponent(g, textArea, c, 1, 1,
            size.width - 1, size.height - 1, true);
   }

   /** */
   public Dimension getPreferredSize(JComponent c)
   {
      String tipText = ((JToolTip) c).getTipText();

      if (tipText == null)
      {
         return new Dimension(0, 0);
      }

      // build textArea once

      if (textArea == null)
      {
         textArea = new JTextArea(tipText);
         rendererPane.removeAll();
         rendererPane.add(textArea);
         textArea.setWrapStyleWord(true);
      }
      else
      {
         textArea.setText(tipText);
      }

      // calculate width

      int width = ((MultiLineToolTip) c).getFixedWidth();
      int columns = ((MultiLineToolTip) c).getColumns();

      if (columns > 0)
      {
         int textLength = tipText.length();

         if (textLength <= columns)
         {
            textArea.setColumns(textLength);
            textArea.setLineWrap(false);
         }
         else
         {
            textArea.setColumns(columns);
            textArea.setLineWrap(true);
         }

         textArea.setSize(textArea.getPreferredSize());
      }
      else if (width > 0)
      {
         textArea.setLineWrap(true);
         Dimension d = textArea.getPreferredSize();
         d.width = width;
         d.height++;
         textArea.setSize(d);
      }
      else
      {
         textArea.setLineWrap(false);
      }

      Dimension dim = textArea.getPreferredSize();

      dim.height += 1;
      dim.width += 1;

      return dim;
   }

   /** */
   public Dimension getMinimumSize(JComponent c)
   {
      return getPreferredSize(c);
   }

   /** */
   public Dimension getMaximumSize(JComponent c)
   {
      return getPreferredSize(c);
   }
}// MultiLineToolTipUI

