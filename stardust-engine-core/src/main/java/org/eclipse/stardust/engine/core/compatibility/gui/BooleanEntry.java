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

import javax.swing.JCheckBox;
import java.awt.event.ActionListener;

public class BooleanEntry extends MandatoryWrapper implements Entry
{
   public BooleanEntry()
   {
      super(new JCheckBox());
   }

   public BooleanEntry(String name)
   {
      super(new JCheckBox(name));
   }

   /**
    * Marks, wether the key box is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return false;
   }

   public boolean isReadonly()
   {
      return !isEnabled();
   }

   public void setReadonly(boolean isReadonly)
   {
      setEnabled(!isReadonly);
/*      if (isReadonly)
      {
         setBackground(SystemColor.control);
      }*/
   }

   /**
    * Getter Method.
    */
   public boolean getValue()
   {
      return ((JCheckBox) getWrappedComponent()).isSelected();
   }

   public void setValue(Boolean value)
   {
      ((JCheckBox) getWrappedComponent()).setSelected(
            value == null ? false : value.booleanValue());
   }

   public void setValue(boolean value)
   {
      ((JCheckBox) getWrappedComponent()).setSelected(value);
   }

   public void setObjectValue(Object value) throws IllegalArgumentException
   {
      if (value == null)
      {
         value = new Boolean(false);
      }
      else if (value instanceof String)
      {
         value = new Boolean((String) value);
      }
      else if (!(value instanceof Boolean))
      {
         throw new IllegalArgumentException("not a Boolean");
      }
      setValue((Boolean) value);
   }

   public Object getObjectValue()
   {
      return new Boolean(getValue());
   }

   public void addActionListener(ActionListener listener)
   {
      ((JCheckBox) getWrappedComponent()).addActionListener(listener);
   }

   public void removeActionListener(ActionListener listener)
   {
      ((JCheckBox) getWrappedComponent()).removeActionListener(listener);
   }
}
