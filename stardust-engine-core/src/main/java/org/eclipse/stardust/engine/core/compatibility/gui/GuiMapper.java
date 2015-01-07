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

import java.lang.reflect.Method;

import javax.swing.*;

import org.eclipse.stardust.common.error.InternalException;


/**
 * Support for wrapper classes.
 */
class TypeDescription
{
   static Object tracekey = "ag.carnot.gui.TypeDescription";

   /**
    * Returns raw widget.
    */
   public static final int RAW_WIDGET = 0;
   /**
    * Returns titled border.
    */
   public static final int BORDER = 1;
   /**
    * Returns a box with JLabel + space + widget
    */
   public static final int VBOX = 2;

   protected Class type;
   protected Class wrapperType;
   protected Method getMethod;
   protected Method setMethod;
   protected Class widget;

   /**
    * Constructor type <-> widget.
    * @param type The type
    * @param widget and the widget
    */
   public TypeDescription(Class type, Class widget)
   {
      this.type = type;
      this.widget = widget;
      wrapperType = null;

      Class[] paras = {type};

      lookupMethods("getValue", null, "setValue", paras);
   }

   /**
    * Constructor type <-> WrapperType <-> widget.
    * @param type The type
    * @param wrapperType The wrapperType
    * @param widget and the widget
    */
   public TypeDescription(Class type,
         Class wrapperType,
         Class widget)
   {
      this.type = type;
      this.widget = widget;
      this.wrapperType = wrapperType;

      Class[] paras = {type};

      lookupMethods("getValue", null, "setValue", paras);
   }

   /**
    * Constructor type <-> WrapperType <-> widget (+ get-/set-Mothds)
    * @param type The type
    * @param wrapperType The wrapperType
    * @param widget and the widget
    * @param getMethodName The name of the getMethod
    * @param getMethodParas The parameter list for the getMethod
    * @param setMethod The name of the setMethod
    * @param setMethodParas The parameter list for the setMethod
    */
   public TypeDescription(Class type,
         Class wrapperType,
         Class widget,
         String getMethodName,
         Class[] getMethodParas,
         String setMethodName,
         Class[] setMethodParas)
   {
      this.type = type;
      this.widget = widget;
      this.wrapperType = wrapperType;

      lookupMethods(getMethodName, getMethodParas, setMethodName,
            setMethodParas);
   }

   /**
    * Check methods for existence.
    * @param getMethodName The name of the getMethod
    * @param getMethodParas The parameter list for the getMethod
    * @param setMethod The name of the setMethod
    * @param setMethodParas The parameter list for the setMethod
    */
   public void lookupMethods(String getMethodName,
         Class[] getMethodParas,
         String setMethodName,
         Class[] setMethodParas)
   {
      try
      {
         getMethod = widget.getMethod(getMethodName, getMethodParas);
      }
      catch (NoSuchMethodException exception)
      {
         throw new InternalException("Getter method '" + getMethodName
               + "' not found in class '" + widget.getName() + "'.\n");
      }

      try
      {
         setMethod = widget.getMethod(setMethodName, setMethodParas);
      }
      catch (NoSuchMethodException exception)
      {
         throw new InternalException("Setter method '" + setMethodName + "(" +
               setMethodParas[0] + ")" + "' not found in class " +
               widget.getName() + ".");
      }
   }

   /**
    * Check for matching of class.
    * @param newType Class type
    */
   public boolean match(Class newType)
   {
      if (newType == type || newType == wrapperType)
      {
         return true;
      }

      return false;
   }

   /**
    * Check for matching of class Key for comboboxes/enums <=> ENUM
    * (put in separate method for performance reasons).
    * @param newType The Class type to check
    */
   public boolean matchBaseKey(Class newType)
   {
      if (org.eclipse.stardust.common.Key.class.isAssignableFrom(newType))
      {
         return true;
      }

      return false;
   }

   /**
    * Create a widget for the field.
    */
   public JComponent createWidget()
   {
      try
      {
         return (JComponent) widget.newInstance();
      }
      catch (java.lang.IllegalAccessException exception)
      {
         throw new InternalException("Cannot create type for widget '" + widget.getName() + "'");
      }
      catch (java.lang.InstantiationException exception)
      {
         throw new InternalException("Cannot create type for widget '" + widget.getName() + "'");
      }
   }

   /**
    * Layout the widget
    * @param widgetObject The widget to be layed out
    * @param name The name of the widget
    * @param layoutType The type of the layout
    */
   public JComponent layoutWidget(JComponent widgetObject,
         String name,
         int layoutType)
   {
      if (layoutType == RAW_WIDGET)
      {
         return widgetObject;
      }

      JPanel panel = new JPanel();

      if (layoutType == BORDER)
      {
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.setBorder(BorderFactory.createTitledBorder(name));
         panel.add(widgetObject);
      }
      else
      {
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(new JLabel(name));
         panel.add(widgetObject);
      }

      return panel;
   }
}

/**
 * This class maps any field or bean property to a gui component.
 */
public class GuiMapper
{
   public static TypeDescription BOOLEAN;
   public static TypeDescription BYTE;
   public static TypeDescription SHORT;
   public static TypeDescription INT;
   public static TypeDescription LONG;
   public static TypeDescription FLOAT;
   public static TypeDescription DOUBLE;
   public static TypeDescription CALENDAR;
   public static TypeDescription CHAR;
   public static TypeDescription STRING;
   public static TypeDescription TIMESTAMP;
   public static TypeDescription MONEY;
   public static TypeDescription ENUM;
   public static TypeDescription ARRAY;
   public static TypeDescription OBJECT;

   static
   {
      Class[] paras = new Class[1];

      paras[0] = Boolean.TYPE;
      BOOLEAN = new TypeDescription(Boolean.TYPE, Boolean.class, BooleanEntry.class);
      BYTE = new TypeDescription(Byte.TYPE, Byte.class, ByteEntry.class);
      SHORT = new TypeDescription(Short.TYPE, Short.class, ShortEntry.class);
      INT = new TypeDescription(Integer.TYPE, Integer.class, IntegerEntry.class);
      LONG = new TypeDescription(Long.TYPE, Long.class, LongEntry.class);
      FLOAT = new TypeDescription(Float.TYPE, Float.class, FloatEntry.class);
      DOUBLE = new TypeDescription(Double.TYPE, Double.class, DoubleEntry.class);
      CHAR = new TypeDescription(Character.TYPE, Character.class, CharEntry.class);
      STRING = new TypeDescription(String.class, TextEntry.class);
      CALENDAR = new TypeDescription(java.util.Calendar.class, CalendarEntry.class);
      TIMESTAMP= new TypeDescription(java.util.Date.class, DateEntry.class);
      MONEY = new TypeDescription(org.eclipse.stardust.common.Money.class, MoneyEntry.class);
      ENUM = new TypeDescription(org.eclipse.stardust.common.Key.class, KeyBox.class);

      // Handle other objects and arrays

      paras[0] = java.lang.Object.class;

      /**
       * OBJECT = new TypeDescription(java.lang.Object.class, null, GenericObjectPanel.class,
       *                       "getValue", null, "setValue", paras);
       */
      ARRAY = new TypeDescription(null, null, org.eclipse.stardust.common.reflect.Table.class,
            "getValue", null, "setValue", paras);
   }

   /**
    * @return <code>true</code> for valid types.
    */
   public static boolean isSupportedType(Class type)
   {
      if (BOOLEAN.match(type) || BYTE.match(type) || SHORT.match(type) || INT.match(type)
            || LONG.match(type) || FLOAT.match(type) || DOUBLE.match(type) || CHAR.match(type)
            || STRING.match(type)
            || CALENDAR.match(type) || TIMESTAMP.match(type) || MONEY.match(type)
            || ENUM.matchBaseKey(type))
      {
         return true;
      }

      return false;
   }

   /**
    * Finds type of allowed object.
    * @param type The type of the object
    */
   protected static TypeDescription getSupportedType(Class type)
   {
      // If type is null a String is assumed

      if (type == null)
      {
         return STRING;
      }

      // Find correct type

      TypeDescription typeDescription = BOOLEAN;

      if (BOOLEAN.match(type))
      {
      }
      else if (CHAR.match(type))
      {
         typeDescription = CHAR;
      }
      else if (BYTE.match(type))
      {
         typeDescription = BYTE;
      }
      else if (SHORT.match(type))
      {
         typeDescription = SHORT;
      }
      else if (INT.match(type))
      {
         typeDescription = INT;
      }
      else if (LONG.match(type))
      {
         typeDescription = LONG;
      }
      else if (MONEY.match(type))
      {
         typeDescription = MONEY;
      }
      else if (CALENDAR.match(type))
      {
         typeDescription = CALENDAR;
      }
      else if (TIMESTAMP.match(type))
      {
         typeDescription = TIMESTAMP;
      }
      else if (STRING.match(type))
      {
         typeDescription = STRING;
      }
      else if (FLOAT.match(type))
      {
         typeDescription = FLOAT;
      }
      else if (DOUBLE.match(type))
      {
         typeDescription = DOUBLE;
      }
      else if (ENUM.matchBaseKey(type))
      {
         typeDescription = ENUM;
      }

      return typeDescription;
   }

   /**
    * Finds type of supported object.
    * @param type The type of the object
    */
   public static Class getType(Class type)
   {
      return getSupportedType(type).wrapperType;
   }

   /**
    * Maps a type to a component.
    * @param type The type for the object
    */
   public static JComponent getComponentForClass(Class type)
   {
      return getComponentForClass(type, false);
   }

   /**
    * Maps a type to a component.
    * @param type The type of the object
    * @param usedAsTableCell Indicates if this component will be used in a table cell
    */
   public static JComponent getComponentForClass(Class type,
         boolean usedAsTableCell)
   {
      TypeDescription typeDescription = null;
      if (!isSupportedType(type))
      {
         typeDescription = getSupportedType(String.class);
      }
      else
      {
         typeDescription = getSupportedType(type);
      }

      if (typeDescription != ENUM)
      {
         try
         {
            JComponent component = (JComponent) typeDescription.widget.newInstance();

            if (component instanceof Entry)
            {
               ((Entry) component).setUsedAsTableCell(usedAsTableCell);
            }

            return component;
         }
         catch (Exception x)
         {
            throw new InternalException(
                  "Cannot create instance for '" + type.getName() + "'.");
         }
      }
      else
      {
         KeyBox box = new KeyBox(type);

         box.setUsedAsTableCell(usedAsTableCell);

         return box;
      }
   }

   /**
    * Sets a value for an object.
    * @param component The target component
    * @param value The value for the target
    */
   public static void setComponentValue(JComponent component, Object value)
   {
      Class type = value.getClass();

      // Check component and type

      if (component == null || !isSupportedType(type))
      {
         return;
      }

      // Check for non-JDK entries first

      if (component instanceof Entry)
      {
         ((Entry) component).setObjectValue(value);
      }
      else if (component instanceof JTextField)
      {
         ((JTextField) component).setText((String) value);

         return;
      }
      else if (component instanceof JComboBox)
      {
         ((JComboBox) component).setSelectedIndex(((Integer) value).intValue());

         return;
      }
      else if (component instanceof JCheckBox)
      {
         ((JCheckBox) component).setSelected(((Boolean) value).booleanValue());

         return;
      }
   }

   /**
    * Gets a value by an object.
    * @return <code>null</code> as default.
    */
   public static Object getComponentValue(JComponent component)
   {
      // Value to be returned

      Object value = null;

      // First check component

      if (component == null)
      {
         return value;
      }

      // Name of component

      if (component instanceof Entry)
      {
         value = ((Entry) component).getObjectValue();
      }
      else if (component instanceof JTextField)
      {
         value = ((JTextField) component).getText();
      }
      else if (component instanceof JCheckBox)
      {
         value = new Boolean(((JCheckBox) component).isSelected());
      }
      else if (component instanceof JComboBox)
      {
         value = new Integer(((JComboBox) component).getSelectedIndex());
      }

      return value;
   }
}


