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
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

import org.eclipse.stardust.common.reflect.Reflect;


// @todo (france, ub): what is this?

public class SimpleBeanTableModel extends AbstractTableModel
{
   private static final String WARNING_TITLE = "Warning";

   private static final String SET_VALUE_ERROR =
      "Couldn't set the value for field '{0}': {1}";
   private static final String GET_VALUE_ERROR =
      "Couldn't get the value for field '{0}': {1}";

   private Vector props;
   private HashMap setters;
   private HashMap getters;

   private Object bean;
   private String[] editableProperties;
   private Component parent;

// BooleanEntry
// ByteEntry
// CharEntry
// DoubleEntry
// FloatEntry
// IntegerEntry
// LongEntry
// ShortEntry
// TextEntry

   public SimpleBeanTableModel(Component parent, Object bean, String[] editableProperties)
   {
      this.parent = parent;
      this.bean = bean;
      this.editableProperties = editableProperties;
      createProperties();
   }

   /**
    * Returns the number of rows in the model. A
    * <code>JTable</code> uses this method to determine how many rows it
    * should display.  This method should be quick, as it
    * is called frequently during rendering.
    *
    * @return the number of rows in the model
    * @see #getColumnCount
    */
   public int getRowCount()
   {
      return props.size();
   }

   /**
    * Returns the number of columns in the model. A
    * <code>JTable</code> uses this method to determine how many columns it
    * should create and display by default.
    *
    * @return the number of columns in the model
    * @see #getRowCount
    */
   public int getColumnCount()
   {
      return 2;
   }

   /**
    * Returns the value for the cell at <code>columnIndex</code> and
    * <code>rowIndex</code>.
    *
    * @param	rowIndex	the row whose value is to be queried
    * @param	columnIndex 	the column whose value is to be queried
    * @return	the value Object at the specified cell
    */
   public Object getValueAt(int rowIndex, int columnIndex)
   {
      if (columnIndex == 0)
      {
         return props.elementAt(rowIndex);
      }
      return getValue((String) props.elementAt(rowIndex));
   }

   public Object getValue(String name)
   {
      Method getter = (Method) getters.get(name);
      try
      {
         return getter.invoke(bean);
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(parent, MessageFormat.format(GET_VALUE_ERROR,
            new Object[] {name, e.getMessage()}),
            WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
      }
      return null;
   }

   /**
    *  This empty implementation is provided so users don't have to implement
    *  this method if their data model is not editable.
    *
    *  @param  aValue   value to assign to cell
    *  @param  rowIndex   row of cell
    *  @param  columnIndex  column of cell
    */
   public void setValueAt(Object aValue, int rowIndex, int columnIndex)
   {
      setValue((String) props.elementAt(rowIndex), aValue);
   }

   public void setValue(String name, Object aValue)
   {
      Method setter = (Method) setters.get(name);
      Class clazz = setter.getParameterTypes()[0];
      if (clazz.isPrimitive())
      {
         clazz = Reflect.getWrapperClassFromPrimitiveClassName(clazz);
      }
      Object value = Reflect.convertStringToObject(
            clazz.getName(), aValue == null ? null : aValue.toString());
      try
      {
         setter.invoke(bean, new Object[]{value});
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog(parent, MessageFormat.format(SET_VALUE_ERROR,
            new Object[] {name, e.getMessage()}),
            WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
      }
   }

   /**
    *  Returns false.  This is the default implementation for all cells.
    *
    *  @param  rowIndex  the row being queried
    *  @param  columnIndex the column being queried
    *  @return false
    */
   public boolean isCellEditable(int rowIndex, int columnIndex)
   {
      return columnIndex > 0 && isEditable((String) props.get(rowIndex));
   }

   private void createProperties()
   {
      props = new Vector();
      getters = new HashMap();
      setters = new HashMap();

      List gettersAndSetters = Reflect.collectGetSetMethods(bean.getClass());
      for (int i = 0; i < gettersAndSetters.size(); i++)
      {
         Method[] methods = (Method[]) gettersAndSetters.get(i);
         if (methods.length == 2)
         {
            String key = methods[1].getName().substring(3);
            props.add(key);
            getters.put(key, methods[0]);
            setters.put(key, methods[1]);
         }
      }
      Collections.sort(props);
   }

   private boolean isEditable(String name)
   {
      if (editableProperties != null)
      {
         for (int i = 0; i < editableProperties.length; i++)
         {
            if (name.equals(editableProperties[i]))
            {
               return true;
            }
         }
      }
      else
      {
         return true;
      }
      return false;
   }

   public Object getObject()
   {
      return bean;
   }

   public Iterator getPropertiesIterator()
   {
      return props.iterator();
   }
}
