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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

import javax.swing.SwingConstants;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * @author rsauer
 * @version $Revision$
 */
public class JSpinner extends JComponent
      implements ActionListener, KeyListener, SwingConstants
{
   protected Vector listeners = new Vector();
   protected BasicArrowButton north, south;
   SpinModel model;

   public JSpinner(SpinModel model)
   {
      this.model = model;
      setLayout(new GridLayout(2, 1));
      setPreferredSize(new Dimension(16, 16));

      north = new BasicArrowButton(BasicArrowButton.NORTH);
      north.addActionListener(this);
      add(north);

      south = new BasicArrowButton(BasicArrowButton.SOUTH);
      south.addActionListener(this);
      add(south);
   }

   public void actionPerformed(ActionEvent event)
   {
      if (event.getSource() == north)
      {
         increment();
      }
      if (event.getSource() == south)
      {
         decrement();
      }
   }

   public void keyTyped(KeyEvent event)
   {
   }

   public void keyReleased(KeyEvent event)
   {
   }

   public void keyPressed(KeyEvent event)
   {
      int code = event.getKeyCode();
      if (isEnabled() && code == KeyEvent.VK_UP)
      {
         increment();
      }
      if (isEnabled() && code == KeyEvent.VK_DOWN)
      {
         decrement();
      }
      if (isEnabled() && code == KeyEvent.VK_LEFT)
      {
         model.setPrevField();
      }
      if (isEnabled() && code == KeyEvent.VK_RIGHT)
      {
         model.setNextField();
      }
   }

   protected void increment()
   {
      int fieldID = model.getActiveField();
      SpinRangeModel range = model.getRange(fieldID);
      range.setValueIsAdjusting(true);
      double value = range.getValue() + range.getExtent();
      if (value > range.getMaximum())
         value = range.getWrap() ?
               range.getMinimum() : range.getMaximum();
      range.setValue(value);
      model.setRange(fieldID, range);
      range.setValueIsAdjusting(false);
   }

   public void decrement()
   {
      int fieldID = model.getActiveField();
      SpinRangeModel range = model.getRange(fieldID);
      range.setValueIsAdjusting(true);
      double value = range.getValue() - range.getExtent();
      if (value < range.getMinimum())
         value = range.getWrap() ?
               range.getMaximum() : range.getMinimum();
      range.setValue(value);
      model.setRange(fieldID, range);
      range.setValueIsAdjusting(false);
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      north.setEnabled(enabled);
      south.setEnabled(enabled);
   }
}
