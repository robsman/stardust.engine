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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author rsauer
 * @version $Revision$
 */
public class JSpinnerField extends JComponent
      implements ChangeListener, MouseListener, FocusListener
{
   protected SpinModel model;
   protected SpinField spinField;
   protected SpinRenderer renderer;
   protected Format formatter;
   protected boolean wrap = true;
   protected boolean hasFocus = false;
   private JSpinner spinner;

   public JSpinnerField()
   {
      this(0, 0, 0, 0, false);
   }

   public JSpinnerField(int value, int extent, int min, int max, boolean wrap)
   {
      init(new DefaultSpinModel(value, extent, min, max, wrap),
            new DefaultSpinRenderer(),
            NumberFormat.getInstance(), wrap);
      refreshSpinView();
   }

   public JSpinnerField(SpinModel model, SpinRenderer renderer, Format formatter,
         boolean wrap)
   {
      init(model, renderer, formatter, wrap);
      refreshSpinView();
   }

   protected void init(SpinModel model, SpinRenderer renderer, Format formatter,
         boolean wrap)
   {
      this.model = model;
      this.renderer = renderer;
      this.formatter = formatter;
      this.wrap = wrap;
      spinField = new SpinField(this);
      setLayout(new BorderLayout());
      add(BorderLayout.CENTER, spinField);
      setBorder(spinField.getBorder());
      spinField.setBorder(null);
      spinner = new JSpinner(model);
      addKeyListener(spinner);
      addMouseListener(this);
      addFocusListener(this);
      model.addChangeListener(this);
      add(BorderLayout.EAST, spinner);
   }

   public void setLocale(Locale locale)
   {
      formatter = NumberFormat.getInstance(locale);
      updateFieldOrder();
   }

   public void updateFieldOrder()
   {
      if (spinField.getValue() == null) return;
      int[] fieldIDs = model.getFieldIDs();
      LocaleUtil.sortFieldOrder(formatter, spinField.getValue(), fieldIDs);
      model.setFieldIDs(fieldIDs);
   }

   public Format getFormatter()
   {
      return formatter;
   }

   public SpinRenderer getRenderer()
   {
      return renderer;
   }

   protected void refreshSpinView()
   {
      int fieldID = model.getActiveField();
      SpinRangeModel range = model.getRange(fieldID);
      spinField.setValue(new Double(range.getValue()));
   }

   public void stateChanged(ChangeEvent event)
   {
      refreshSpinView();
      repaint();
   }

   public void mouseClicked(MouseEvent event)
   {
      int fieldID = LocaleUtil.findMouseInField(getGraphics().getFontMetrics(), event.getX(),
            formatter, spinField.getValue(), model.getFieldIDs());
      model.setActiveField(fieldID);
      requestFocus();
      refreshSpinView();
   }

   public void mousePressed(MouseEvent event)
   {
   }

   public void mouseReleased(MouseEvent event)
   {
   }

   public void mouseEntered(MouseEvent event)
   {
   }

   public void mouseExited(MouseEvent event)
   {
   }

   public void focusGained(FocusEvent event)
   {
      hasFocus = true;
      repaint();
   }

   public void focusLost(FocusEvent event)
   {
      hasFocus = false;
      repaint();
   }

   public boolean isFocusTraversable()
   {
      return true;
   }

   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);
      spinner.setEnabled(enabled);
   }
}
