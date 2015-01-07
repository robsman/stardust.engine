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

import javax.swing.table.AbstractTableModel;
import java.sql.SQLException;
import java.sql.ResultSetMetaData;
import java.sql.ResultSet;
import java.util.Vector;

public class ResultSetTableModel extends AbstractTableModel
{
   private String[] names = new String[0];
   private Vector rows;

   public ResultSetTableModel(ResultSet rs)
   {
      try
      {
         ResultSetMetaData md = rs.getMetaData();
         names = new String[md.getColumnCount()];
         for (int i = 0; i < names.length; i++)
         {
            names[i] = md.getColumnLabel(i + 1);
         }
         rows = new Vector();
         while (rs.next())
         {
            Object[] row = new Object[names.length];
            for (int i = 0; i < row.length; i++)
            {
               row[i] = rs.getString(i + 1);
            }
            rows.add(row);
         }
      }
      catch (SQLException e)
      {
      }
      finally
      {
         try
         {
            rs.close();
         }
         catch (SQLException e)
         {
         }
      }
   }

   /**
    *  Returns a default name for the column using spreadsheet conventions:
    *  A, B, C, ... Z, AA, AB, etc.  If <code>column</code> cannot be found,
    *  returns an empty string.
    *
    * @param column  the column being queried
    * @return a string containing the default name of <code>column</code>
    */
   public String getColumnName(int column)
   {
      return names[column];
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
      return rows.size();
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
      return names.length;
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
      Object[] row = (Object[]) rows.get(rowIndex);
      return row[columnIndex];
   }
}
