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

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.Key;
import org.eclipse.stardust.common.Unknown;
import org.eclipse.stardust.common.error.InternalException;


/**
 * Combo box for displaying and modifying the value of keys. Keys are
 * are enumeration values, whose enumeration character is demarked by
 * inheriting from <code>org.eclipse.stardust.common.Key</code>.
 * 
 * @author unknown
 * @version $Revision$
 */
public class KeyBox extends JPanel
      implements Entry, ItemListener
{
   /*
    *	Type of the key.
    */
   private Class keyType;
   private boolean mandatory = false;
   private boolean readonly = false;
   private JComboBox comboBox;
   private JLabel flagLabel;

   /**
    * Constructs a combo box for a given key class.
    */
   public KeyBox(Class keyType)
   {
      this(keyType, false);
   }

   /**
    * Constructs a combo box for a given key object.
    */
   public KeyBox(Key key)
   {
      this(key, false);
   }

   /**
    * Constructs a combo box for a given key object, whose content must not be
    * undefined.
    */
   public KeyBox(Class keyType, boolean mandatory)
   {
      super();
      this.mandatory = mandatory;
      initialize(keyType, null);
   }

   /**
    * Constructs a combo box for a given key object, whose content must not be
    * undefined.
    */
   public KeyBox(Key key, boolean mandatory)
   {
      super();
      this.mandatory = mandatory;
      initialize(key.getClass(), key);
   }

   /**
    * Do the initialization (called from constructors).
    */
   protected void initialize(Class keyType, Key key)
   {
      Assert.isNotNull(keyType);
      Assert.condition(Key.class.isAssignableFrom(keyType));

      if (key != null)
      {
         Assert.condition(keyType.isInstance(key));
      }

      this.keyType = keyType;

      // Construct entry
      setLayout(new BorderLayout());
      comboBox = new JComboBox();
      comboBox.addItemListener(this);
      flagLabel = new JLabel(GUI.getMandatoryIcon());
      //	flagLabel.addFocusListener(this);

      add("West", flagLabel);
      add("Center", Box.createHorizontalStrut(3));
      add("East", comboBox);

      // Populate combo box
      populateBox();
      setValue(key);

      // Initialize widget appearance
      comboBox.setMaximumSize(getPreferredSize());
      performFlags();
   }

   /**
    * Populates the combo box with the value set of the key.
    */
   private void populateBox()
   {
      String strings[] = null;

      try
      {
         Method method = keyType.getMethod("getKeyList");
         strings = (String[]) method.invoke(keyType);
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }

      if (strings == null)
      {
         comboBox.addItem(" ");
      }
      else
      {
         DefaultComboBoxModel model = new DefaultComboBoxModel(strings);
         // Insert unknown value
         model.insertElementAt(" ", 0);
         comboBox.setModel(model);
      }
   }

   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p/>
    * The method is thought to be used for table cell editors or the like.
    */
   public JComponent getWrappedComponent()
   {
      return comboBox;
   }

   /**
    * Marks, wether the key box is used as a table cell.
    */
   public void setUsedAsTableCell(boolean isCell)
   {
      if (isCell)
      {
         removeAll();
         add("East", comboBox);
         setBackground(comboBox.getBackground());
         comboBox.setBorder(null);
      }
   }

   /**
    * Invoked when an item has been selected or deselected.
    */
   public void itemStateChanged(ItemEvent e)
   {
      performFlags();
   }

   /**
    * @return The combo box internally used by this object.
    */
   public JComboBox getComboBox()
   {
      return comboBox;
   }

   /**
    * En/disable mandatory input.
    */
   public void setMandatory(boolean mandatory)
   {
      this.mandatory = mandatory;

      performFlags();
   }

   /**
    * Gets mandatory status.
    */
   public boolean isMandatory()
   {
      return mandatory;
   }

   public boolean isReadonly()
   {
      return readonly;
   }

   public void setReadonly(boolean readonly)
   {
      this.readonly = readonly;
      comboBox.setEnabled(!readonly);
   }

   /**
    * @return <code>true</code> if the content of the field is empty;
    *         <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return (getObjectValue() == null);
   }

   /**
    * Overrides <code>super.getEnabled()</code> to show empty field if
    * set to false.
    */
   public boolean getEnabled()
   {
      return comboBox.isEnabled();
   }

   /**
    * Overrides <code>super.isEnabled()</code> to show empty field if
    * set to false.
    */
   public boolean isEnabled()
   {
      return getEnabled();
   }

   /**
    * Overrides <code>super.setEnabled()</code> to show empty field if
    * set to false.
    */
   public void setEnabled(boolean isEnabled)
   {
      comboBox.setEnabled(isEnabled);
   }

   /**
    * Check mandatory and editable flags to set color settings.
    */
   protected void performFlags()
   {
      flagLabel.setIcon(!isReadonly() && mandatory && comboBox.getSelectedIndex() == 0 ?
            GUI.getMandatoryIcon() : GUI.getOptionalIcon());
   }

   public int getIntValue()
   {
      int value = comboBox.getSelectedIndex();
      if (value <= 0)
      {
         return Unknown.KEY_VALUE;
      }
      return (value - 1);
   }

   public Key getValue()
   {
      return (Key) getObjectValue();
   }

   public void setValue(int keyValue)
   {
      if (!isEnabled())
      {
         return;
      }
      comboBox.setSelectedIndex(keyValue < 0 ? 0 : keyValue + 1);
      performFlags();
   }

   public void setValue(Key key)
   {
      setValue(key == null ? -1 : key.getValue());
   }

   public void setObjectValue(Object object) throws IllegalArgumentException
   {
      if ((object == null) || (object instanceof Key))
      {
         setValue((Key) object);
      }
      else
      {
         throw new IllegalArgumentException("The type \"" + object.getClass().getName()
               + "\" of the argument is not compatible with org.eclipse.stardust.common.Key.");
      }
   }

   /**
    * @return <code>null</code> if unitialized.
    */
   public Object getObjectValue()
   {
      try
      {
         int value = getIntValue();
         if (value < 0)
         {
            return null;
         }
         Constructor constructor = keyType.getConstructor(new Class[]{int.class});
         return constructor.newInstance(new Object[]{new Integer(value)});
      }
      catch (Exception e)
      {
         throw new InternalException("Cannot instantiate key class \"" + keyType.getName() + "\".");
      }
   }
}
