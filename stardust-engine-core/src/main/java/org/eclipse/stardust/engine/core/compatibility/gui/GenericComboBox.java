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

import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 *
 */
public class GenericComboBox extends JPanel
      implements Entry, ActionListener, ItemListener
{
   public static final Logger trace = LogManager.getLogger(GenericComboBox.class);

   private List actionListeners;

   private Method displayMethod;

   private boolean mandatory = false;
   private boolean readonly = false;
   private boolean sorted = false;
   private JComboBox comboBox;
   private JLabel flagLabel;

   /**
    *	Constructs a combo box for a given key class.
    */
   public GenericComboBox(Class type, String displayAttribute)
   {
      this(type, displayAttribute, null, null, false, false);
   }

   /**
    *	Constructs a combo box for a given key object, whose content must not be
    * undefined.
    */
   public GenericComboBox(Class type, String displayAttribute,
         boolean mandatory, boolean sorted)
   {
      this(type, displayAttribute, null, null, mandatory, sorted);
   }

   // @todo/belgium (ub) implement sort
   /**
    */
   public GenericComboBox(Class type, String displayAttribute, Object[] values,
         Object selection, boolean mandatory, boolean sorted)
   {
      this.actionListeners = new LinkedList();

      this.mandatory = mandatory;
      this.sorted = sorted;

      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      comboBox = new JComboBox();
      comboBox.addActionListener(this);

      comboBox.addItemListener(this);

      flagLabel = new JLabel(GUI.getMandatoryIcon());
      //	flagLabel.addFocusListener(this);

      add(flagLabel);
      add(Box.createHorizontalStrut(3));
      add(comboBox);


      comboBox.setRenderer(new GenericComboBoxRenderer());

      // @todo (ub): check whether values are of type 'type'
      if (type!= null)
      {
         setType(type, displayAttribute);
         setValues(values);
      }

      if (selection == null)
      {
         comboBox.setSelectedIndex(-1);
      }
      else
      {
         comboBox.setSelectedItem(selection);
      }

      // Initialize widget appearance

      comboBox.setMaximumSize(getPreferredSize());
      performFlags();
   }

   public void setType(Class type, String displayAttribute)
   {
      try
      {
         this.displayMethod = type.getMethod("get" + capitalize(displayAttribute));
      }
      catch (Exception e)
      {
         trace.warn("", e);
         throw new InternalException(e);
      }
   }

   /**
    * If the entry is wrapping a "native" component like JEntryField or JComboBox,
    * this component is returned. Otherwise, the method returns this.
    * <p>
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

   public void actionPerformed(ActionEvent e)
   {
      ActionEvent event = new ActionEvent(this, e.getID(), e.getActionCommand(),
            e.getModifiers());
      for (Iterator itr = actionListeners.iterator(); itr.hasNext();)
      {
         ((ActionListener) itr.next()).actionPerformed(event);
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
    *	Gets mandatory status.
    */
   public boolean isMandatory()
   {
      return mandatory;
   }

   /**
    *
    */
   public boolean isReadonly()
   {
      return readonly;
   }

   /**
    *
    */
   public void setReadonly(boolean readonly)
   {
      this.readonly = readonly;

      comboBox.setEnabled(!readonly);

      performFlags();
   }

   /**
    * @return  <code>true</code> if the content of the field is empty;
    *          <code>false</code> otherwise.
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
    *	Check mandatory and editable flags to set color settings.
    */
   protected void performFlags()
   {
      Assert.isNotNull(comboBox);
      Assert.isNotNull(flagLabel);

      if (isReadonly())
      {
         comboBox.setBackground(SystemColor.control);
         flagLabel.setIcon(GUI.getOptionalIcon());
      }
      else
      {
         //comboBox.setBackground(Color.white);

         if (mandatory && comboBox.getSelectedIndex() == 0)
         {
            flagLabel.setIcon(GUI.getMandatoryIcon());
         }
         else
         {
            flagLabel.setIcon(GUI.getOptionalIcon());
         }
      }
   }

   /**
    *
    */
   public Object getValue()
   {
      return getObjectValue();
   }

   public Object getObjectValue()
   {
      return comboBox.getSelectedItem();
   }

   /**
    *
    */
   public void setValue(Object value)
   {
      comboBox.setSelectedItem(value);
      performFlags();
   }

   /**
    *
    */
   public void setObjectValue(Object object)
         throws IllegalArgumentException
   {
      setValue(object);
   }

   public void setValues(Collection values)
   {
      setValues(values.toArray());
   }

   public void setValues(Object[] values)
   {
      if (values == null)
      {
         values = new Object[0];
      }

      if (sorted)
      {
         Arrays.sort(values, new Comparator()
         {
            public int compare(Object o1, Object o2)
            {
               try
               {
                  String s1 = (String) displayMethod.invoke(o1);
                  if (s1 == null)
                  {
                     s1 = "";
                  }
                  String s2 = (String) displayMethod.invoke(o2);
                  if (s2 == null)
                  {
                     s2 = "";
                  }
                  return s1.compareTo(s2);
               }
               catch (Exception e)
               {
                  throw new InternalException(e);
               }
            }
         });
      }

      comboBox.setModel(new DefaultComboBoxModel(values));
   }

   public void addActionListener(ActionListener actionListener)
   {
      actionListeners.add(actionListener);
   }

   public void removeActionListener(ActionListener listener)
   {
      actionListeners.remove(listener);
   }

   public void setSelectedIndex(int i)
   {
      comboBox.setSelectedIndex(i);
   }

   public Object getSelectedItem()
   {
      return comboBox.getSelectedItem();
   }

   public void setSelectedItem(Object value)
   {
      comboBox.setSelectedItem(value);
   }

   private class GenericComboBoxRenderer extends BasicComboBoxRenderer
   {
      Font normalFont;
      Font boldFont;

      public GenericComboBoxRenderer()
      {
         super();
         setBorder(null);
         normalFont = getFont();
         boldFont = new Font(normalFont.getName(), Font.BOLD, normalFont.getSize());
      }

      public Component getListCellRendererComponent(JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus)
      {
         if (isSelected)
         {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
            setFont(boldFont);
         }
         else
         {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setFont(normalFont);
         }

         if (value != null)
         {
            try
            {
               setText(String.valueOf(displayMethod.invoke(value)));
            }
            catch (Exception e)
            {
               trace.warn("", e);
               throw new InternalException(e);
            }
         }
         else
         {
            setText("");
         }
         return this;
      }
   }

   static String capitalize(String s)
   {
      if (s.length() == 0)
      {
         return s;
      }
      char chars[] = s.toCharArray();
      chars[0] = Character.toUpperCase(chars[0]);
      return new String(chars);
   }
}
