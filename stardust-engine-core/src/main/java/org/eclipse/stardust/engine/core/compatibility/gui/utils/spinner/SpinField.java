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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

/**
 * @author rsauer
 * @version $Revision$
 */
public class SpinField extends JComponent
{
   private CellRendererPane pane;
   private JSpinnerField field;
   private Object value;

   public SpinField(JSpinnerField field)
   {
      this.field = field;
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, pane = new CellRendererPane());
      JComponent renderer = (JComponent) field.getRenderer();
      setBorder(renderer.getBorder());
      renderer.setBorder(null);
   }

   public void setValue(Object value)
   {
      this.value = value;
      repaint();
   }

   public Object getValue()
   {
      return value;
   }

   public void paintComponent(Graphics g)
   {
      int w = getSize().width;
      int h = getSize().height;
      Component comp = field.getRenderer().
            getSpinCellRendererComponent(field, value,
                  field.hasFocus, field.formatter,
                  field.model.getActiveField());
      pane.paintComponent(g, comp, this, 0, 0, w, h);
   }

   public Dimension getPreferredSize()
   {
      return ((JComponent) field.getRenderer()).getPreferredSize();
   }

   public Dimension getMinimumSize()
   {
      return ((JComponent) field.getRenderer()).getMinimumSize();
   }
}
