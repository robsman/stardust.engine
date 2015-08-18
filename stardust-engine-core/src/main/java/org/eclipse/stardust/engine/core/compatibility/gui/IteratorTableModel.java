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
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Custom model for GenericTable
 * <p>
 * Once retrieved, the property values are cached.
 */
public class IteratorTableModel extends AbstractTableModel
{
   private static final Logger trace = LogManager.getLogger(IteratorTableModel.class);

   static protected final String STRING_MORE_OBJECTS_FOUND = "(more objects exist)";
   static protected final String STRING_NOMORE_OBJECTS_FOUND = "(all objects read)";
   static protected final String STRING_OBJECT_FOUND = "object found";
   static protected final String STRING_OBJECTS_FOUND = "objects found";
   static protected final char SPACE = ' ';

   /**
    * Objects to be read in at once
    */
   public int maxCount = 20;
   /**
    * Boolean for enabling fetching
    */
   private boolean fetchIt = true;
   /**
    * Boolean for making fetch superior to sort
    */
   private boolean isFetching = false;
   /**
    * Vectors that caches the properties retrieved by the iterator
    */
   private Vector[] propertyCache;
   /**
    * Vector that caches the objects retrieved by the iterator
    */
   private Vector objectCache;
   /**
    * Type for a class
    */
   protected Class type;
   /**
    * Iterator
    */
   protected Iterator objectItr;
   /**
    * Property count
    */
   protected int propertyCount;
   /**
    * Names of the properties
    */
   protected String[] propertyNames;
   /**
    * Array of column headers
    */
   protected String[] columnHeaders;
   /**
    * Array containing the class for each table column
    */
   protected Class[] columnTypes;
   /**
    * Array containing the class for each table column
    */
   protected Class[] columnTypesForNamedValues;
   /**
    * Array containing the getter methods for read access
    */
   protected Method[] getterMethods;
   /**
    * Array containing the setter methods for write access
    */
   protected Method[] setterMethods;
   private String namedValueSetterPrefix;
   private String namedValueGetterPrefix;
   private boolean editable = true;
   /**
    * For sorting.
    */
   int indexes[];

   /**
    * Indicates if the data in this model has to be sorted.
    *
    * @see #sortingColumn
    * @see #ascending
    */
   boolean isSorted = false;

   /**
    * For sorting.
    */
   boolean ascending = true;
   /**
    * Columns taken for sorting
    */
   int sortingColumn;
   /**
    * Virtual statusbar
    */
   private JTextField statusBar;

   /**
    * Constructor for creating an IteratorTableModel
    * @param type The class Type for the model
    * @param propertyNames The names of the column properties
    * @param columnHeaders The header strings to be displayed
    */
   public IteratorTableModel(Class type, String propertyNames[],
         String columnHeaders[])
   {
      this(type, null, propertyNames, columnHeaders);
   }

   /**
    * Constructor for creating an IteratorTableModel
    * @param type The class Type for the model
    * @param objectItr The iterator containing the rows for the table
    * @param propertyNames The names of the column properties
    * @param columnHeaders The header strings to be displayed
    */
   public IteratorTableModel(Class type, Iterator objectItr,
         String propertyNames[], String columnHeaders[])
   {
      super();

      this.type = type;
      this.objectItr = objectItr;
      this.propertyNames = propertyNames;
      this.columnHeaders = columnHeaders;
      this.columnTypesForNamedValues = new Class[propertyNames.length];
      this.namedValueSetterPrefix = null;
      this.namedValueGetterPrefix = null;

      initialize();
   }

   /**
    * Constructor for creating an IteratorTableModel being used with property
    * classes as hashtables etc. cell content is not retrieved by invoking
    * <code>void setX(Type)/Type getX()</code> but
    * <code>void set("X", Type)/Type get("X")</code> if the <code>columnType</code>
    * is not null.<p>
    *
    * @param type The class Type for the model
    * @param propertyNames The names of the column properties
    * @param columnHeaders The header strings to be displayed
    * @param columnTypesForNamedValues The types of the columns
    */
   public IteratorTableModel(Class type, Iterator objectItr,
         String propertyNames[], String columnHeaders[],
         Class[] columnTypesForNamedValues,
         String namedValueSetterPrefix,
         String namedValueGetterPrefix)
   {
      super();

      this.type = type;
      this.objectItr = objectItr;
      this.propertyNames = propertyNames;
      this.columnHeaders = columnHeaders;
      this.columnTypesForNamedValues = columnTypesForNamedValues;
      this.namedValueSetterPrefix = namedValueSetterPrefix;
      this.namedValueGetterPrefix = namedValueGetterPrefix;

      initialize();
   }

   /**
    * Initialize the table (columns, rows, etc.)
    */
   private void initialize()
   {
      // Set max. number of cols for direct access

      propertyCount = propertyNames.length;

      // Initialize table object and property cache

      objectCache = new Vector();
      propertyCache = new Vector[propertyCount];

      for (int n = 0; n < propertyCount; ++n)
      {
         propertyCache[n] = new Vector();
      }

      // Build table columns

      prepareColumns();

      // Fill the table

      fetchObjects(maxCount);

      indexes = new int[0];
   }

   /**
    * Set a new Iterator containing the rows.
    * @param objectItr The iterator for the table
    */
   public void setIterator(Iterator objectItr)
   {
      this.objectItr = objectItr;

      removeAllRows();

      // fill the table again

      fetchObjects(maxCount);
   }

   /**
    * Fetches all objects.
    */
   public void fetchAllObjects()
   {
      if (objectItr == null)
      {
         return;
      }

      while (objectItr.hasNext())
      {
         addRow(objectItr.next());
      }
   }

   /**
    * Fetches (count) objects.
    * @param count The amount of objects to be fetched
    */
   public void fetchObjects(int count)
   {
      if (objectItr == null)
      {
         return;
      }

      isFetching = true;

      int counter = 0;

      while (objectItr.hasNext())
      {
         addRow(objectItr.next());

         counter++;

         if (counter >= count)
         {
            break;
         }
      }

      // Now reallocate indexes if objects have been added

      if (counter > 0)
      {
         reallocateIndexes();
      }

      // Update statusBar if wanted

      if (statusBar != null)
      {
         int size = objectCache.size();
         StringBuffer status = new StringBuffer(150);

         status.append(size);
         status.append(SPACE);
         // Adjust text to # of iterators
         if (size > 1)
         {
            status.append(STRING_OBJECTS_FOUND);
         }
         else
         {
            status.append(STRING_OBJECT_FOUND);
         }

         // Adjust status text

         status.append(SPACE);
         if (objectItr.hasNext())
         {
            status.append(STRING_MORE_OBJECTS_FOUND);
         }
         else
         {
            status.append(STRING_NOMORE_OBJECTS_FOUND);
         }

         statusBar.setText(status.toString());
      }

      isFetching = false;

      // reapply sorting

      if (isSorted)
      {
         sort(this);
      }
   }

   /**
    * Retrieves setter and getter methods based on the provided property names.
    */
   private void prepareColumns()
   {
      // Assign maximum length of arrays

      columnTypes = new Class[propertyCount];
      getterMethods = new Method[propertyCount];
      setterMethods = new Method[propertyCount];

      // Prepare the fields etc.

      for (int i = 0; i < propertyCount; i++)
      {
         if (columnTypesForNamedValues[i] != null)
         {
            columnTypes[i] = columnTypesForNamedValues[i];

            try
            {
               getterMethods[i] = type.getMethod(namedValueGetterPrefix, new Class[]{String.class});
            }
            catch (Exception e)
            {
               throw new InternalException("Getter method \"" + namedValueGetterPrefix +
                     "\" for table column not found or not accessible.");
            }

            try
            {
               setterMethods[i] = type.getMethod(namedValueSetterPrefix, new Class[]{String.class, Object.class});
            }
            catch (Exception e)
            {
               setterMethods[i] = null;
            }
         }
         else
         {
            try
            {
               getterMethods[i] = type.getMethod("get" + propertyNames[i]);
               columnTypes[i] = getterMethods[i].getReturnType();

               Assert.isNotNull(columnTypes[i]);
               Assert.condition(columnTypes[i] != Void.TYPE);
            }
            catch (Exception e)
            {
               throw new InternalException("Getter method \"" + "get" + propertyNames[i] +
                     "\" for table column not found or not accessible.");
            }

            try
            {
               setterMethods[i] = type.getMethod("set" + propertyNames[i],
                     new Class[]{columnTypes[i]});
            }
            catch (Exception e)
            {
               setterMethods[i] = null;
            }
         }
      }
   }

   /**
    * Adds a row for another object.
    * @param object The object that will be added to the table model
    */
   private void addRow(Object object)
   {
      objectCache.add(object);

      int pos = objectCache.size();

      // Object we are calling our getter method on

      for (int n = 0; n < propertyCount; ++n)
      {
         try
         {
            if (columnTypesForNamedValues[n] != null)
            {
               propertyCache[n].add(getterMethods[n].invoke(object, new Object[]{propertyNames[n]}));
            }
            else
            {
               propertyCache[n].add(getterMethods[n].invoke(object));
            }
         }
         catch (InvocationTargetException e)
         {
            trace.debug("", e.getTargetException());
            propertyCache[n].add(new String("#*#"));
         }
         catch (Exception e)
         {
            trace.debug("", e);
            propertyCache[n].add(new String("###"));
         }
      }

      fireTableRowsInserted(pos, pos);

      // Next line is needed for correct drawing of new component at end of table

      fireTableRowsUpdated(pos, pos);
   }

   /**
    * Checks whether the table is editable or not.
    */
   public boolean isEditable()
   {
      return getEditable();
   }

   /**
    * Returns whether the table is editable or not.
    */
   public boolean getEditable()
   {
      return editable;
   }

   /**
    * Set whether table is editable or not
    * @param editable Sets the table to be editable or not
    */
   public void setEditable(boolean editable)
   {
      this.editable = editable;
   }

   /**
    * @return Number of total visible columns including number column
    */
   public int getColumnCount()
   {
      return propertyCount + 1;
   }

   /**
    * @return Number of property columns
    */
   public int getPropertyCount()
   {
      return propertyCount;
   }

   /**
    * @return Number of rows in model.
    */
   public int getRowCount()
   {
      return objectCache.size();
   }

   /**
    * Returns true if the cell at rowIndex and columnIndex is editable.
    * @param rowIndex The index of the row
    * @param columnIndex The index of the column
    */
   public boolean isCellEditable(int rowIndex, int columnIndex)
   {
      if (!editable ||
            columnIndex == 0)
      {
         return false;
      }

      return setterMethods[columnIndex - 1] != null;
   }

   /**
    * @return Object at given row.
    */
   public Object getObjectAt(int row)
   {
      return objectCache.elementAt(indexes[row]);
   }

   /**
    * @return   All cached objects.
    *       This is intended for getting objects. If you want to set the
    *       values of an object and make that change visible you are
    *       responsible for the table's update.
    */
   public Vector getObjects()
   {
      return objectCache;
   }

   /**
    * @return Value of pos(row, column) including number column.
    */
   public Object getValueAt(int row, int column)
   {
      Assert.condition(row < getRowCount());

      // Last possible row shown ?

      if ((fetchIt == true) && (row == getRowCount() - 1))
      {
         fetchObjects(maxCount);
      }

      if (row >= indexes.length)
      {
         reallocateIndexes();
      }

      if (column == 0)
      {
         return new String("" + (indexes[row] + 1));
      }

      return propertyCache[column - 1].elementAt(indexes[row]);
   }

   /**
    * Sets Value at pos(row, column).
    * Note: these are full table coordinates e.g. row 0 could hold the row
    * numbers => so be sure to know the 'real' table structure
    * @param object The object to be set
    * @param row The row for the object
    * @param column The column for the object
    */
   public void setValueAt(Object object, int row, int column)
   {
      // Line numbers cannot be set

      if (column == 0)
      {
         return;
      }

      // Get the setter method

      Method setter = setterMethods[column - 1];

      if (setter == null)
      {
         return;
      }

      // Object we are calling our setter method on

      Object rowObject = objectCache.elementAt(indexes[row]);

      try
      {
         if (columnTypesForNamedValues[column] != null)
         {
            trace.debug( "setting " + propertyNames[column] + " with " + object);
            setter.invoke(rowObject, new Object[]{propertyNames[column], object});
         }
         else
         {
            setter.invoke(rowObject, new Object[]{object});
         }

         propertyCache[column - 1].setElementAt(object, indexes[row]);
      }
      catch (PublicException e)
      {
         JOptionPane.showMessageDialog(null,
               "Beim Setzen dieses Wertes ist ein Fehler aufgetreten:\n\n"
               + e.getMessage(),
               "Error",
               JOptionPane.ERROR_MESSAGE);
      }
      catch (Exception e)
      {
         throw new InternalException(e);
      }
   }

   /**
    * Returns the name of a given column
    * @param column The column index
    * @return The Name of the column.
    */
   public String getColumnName(int column)
   {
      if (column == 0)
      {
         return "Nr";
      }

      return columnHeaders[column - 1];
   }

   /**
    * Returns the class type of a given Column
    * @param column The column index
    * @return The Type of the column
    */
   public Class getColumnType(int column)
   {
      if (column == 0)
      {
         return String.class;
      }

      return columnTypes[column - 1];
   }

   /**
    * Obtains, wether a table column is a readonly column.
    * @param column The column index
    * @return If returned true the column is readonly
    */
   public boolean getReadonlyAt(int column)
   {
      if (column == 0)
      {
         return true;
      }

      return (setterMethods[column - 1] == null);
   }

   /**
    * Adds an empty row to the existing rows
    */
   public void addRow()
   {
      if (type == null)
      {
         throw new InternalException("Could not add row: No type given to model.");
      }

      try
      {
         objectCache.addElement(new Object());

         int i = objectCache.size();
         fireTableRowsInserted(i, i);

         // Next line is needed for correct drawing of new component at end of table

         fireTableRowsUpdated(i, i);
      }
      catch (Exception e)
      {
         throw new InternalException("Could not add row: Type " + type
               + " does not have a default constructor.");
      }
   }

   /**
    * Remove given row/use visible order
    * @param row The row index
    */
   public void removeRow(int row)
   {
      removeRow(row, true);
   }

   /**
    * remove row at given row index
    * @param row The row index
    * @param isUsingVisibleOrder decides whether to use visible or internal
    * order of objects for identification of row
    */
   public void removeRow(int row, boolean isUsingVisibleOrder)
   {
      // One line is minimum

      if (row < 0 || row >= getRowCount())
      {
         return;
      }

      if (isUsingVisibleOrder)
      {
         objectCache.removeElementAt(indexes[row]);

         for (int n = 0; n < propertyCache.length; ++n)
         {
            propertyCache[n].removeElementAt(indexes[row]);
         }
      }
      else
      {
         objectCache.removeElementAt(row);

         for (int n = 0; n < propertyCache.length; ++n)
         {
            propertyCache[n].removeElementAt(row);
         }
      }

      fireTableRowsDeleted(row, row);
   }

   /**
    * Remove all rows that are listed in the int array
    * @param rows The list of the rows to be removed
    */
   public void removeRows(int[] rows)
   {
      if (rows == null)
      {
         return;
      }

      // Sort the array to remove rows in descending order

      int sortedRows[] = rows;

      // Map visible rows -> internal ones

      for (int i = 0; i < sortedRows.length; i++)
      {
         sortedRows[i] = indexes[sortedRows[i]];
      }

      // Sort the internal array (to delete bottom->up)

      Arrays.sort(sortedRows);

      // Now remove rows from table

      for (int i = sortedRows.length - 1; i >= 0; i--)
      {
         removeRow(sortedRows[i], false);
      }
   }

   /**
    * Remove all rows in the table
    */
   public void removeAllRows()
   {
      // Cache number of rows

      int rowCount = getRowCount();

      if (rowCount > 0)
      {
         // Clear caches

         objectCache.removeAllElements();

         for (int n = 0; n < propertyCache.length; ++n)
         {
            propertyCache[n].removeAllElements();
         }

         // Notify table

         fireTableRowsDeleted(0, rowCount - 1);
      }

      if (statusBar != null)
      {
         // todo use customizable message

         statusBar.setText("");
      }
   }

   /**
    * Init the indexes to standard behaviour.
    */
   private void reallocateIndexes()
   {
      int rowCount = getRowCount();

      // Set up a new array of indexes with the right number of elements
      // for the new data model.

      indexes = new int[rowCount];

      // Initialise with the identity mapping.

      for (int row = 0; row < rowCount; row++)
      {
         indexes[row] = row;
      }
   }

   /**
    * compare rows by a given column for sorting purposes <P>
    * (build for Entry fields) @see compareObjects
    * @param row1 The first row
    * @param row2 The second row
    * @param column The column to be compared
    * @return 0: If value is equal
    *         1: If value at row1 is greater
    *        -1: If value at row2 is greater
    */
   private int compareRowsByColumn(int row1, int row2, int column)
   {
      // Get values to be compared => disable fetching while in this method
      // do not forget to enable it before reaching return

      fetchIt = false;

      // Store result of compare

      int value = compareObjects(propertyCache[column - 1].elementAt(row1),
            propertyCache[column - 1].elementAt(row2));

      // Allow fetching again

      fetchIt = true;

      return value;
   }

   /**
    * Compares two objects of same type.
    * @param o1 The first object
    * @param o2 The second object
    * @return 0: If objects are equal (includes: both are null)
    *        -1: if first  object is smaller than the second one or null <br>
    *         1: if second object is smaller than the first  one or null
    */
   private int compareObjects(Object o1, Object o2)
   {
      // If both values are null return 0

      if (o1 == null && o2 == null)
      {
         return 0;
      }
      else if (o1 == null)
      {
         return -1;
      }
      else if (o2 == null)
      {
         return 1;
      }

      Class type = o1.getClass();

      if (type != o2.getClass())
      {
         throw new InternalException("Could not compare: 2 different classes: "
               + type + " & " + o2.getClass());
      }

      // Find the correct compare

      if (type.getSuperclass() == java.lang.Number.class)
      {
         double d1 = ((Number) o1).doubleValue();
         double d2 = ((Number) o2).doubleValue();

         if (d1 < d2)
         {
            return -1;
         }
         else if (d1 > d2)
         {
            return 1;
         }
         else
         {
            return 0;
         }
      }
      else if ((type == java.util.Date.class)
            || (type == java.sql.Date.class))
      {
         long n1 = ((java.util.Date) o1).getTime();
         long n2 = ((java.util.Date) o2).getTime();

         if (n1 < n2)
         {
            return -1;
         }
         else if (n1 > n2)
         {
            return 1;
         }
         else
         {
            return 0;
         }
      }
      else if ((type == java.util.Calendar.class)
            || (type == java.util.GregorianCalendar.class))
      {
         long n1 = ((java.util.Calendar) o1).getTime().getTime();
         long n2 = ((java.util.Calendar) o2).getTime().getTime();

         if (n1 < n2)
         {
            return -1;
         }
         else if (n1 > n2)
         {
            return 1;
         }
         else
         {
            return 0;
         }
      }
      else if (type == Boolean.class)
      {
         boolean b1 = ((Boolean) o1).booleanValue();
         boolean b2 = ((Boolean) o2).booleanValue();

         if (b1 == b2)
         {
            return 0;
         }
         else if (b1)
         {
            return 1;
         }
         else
         {
            return -1;
         }
      }
      else
      {
         String s1 = o1.toString();
         String s2 = o2.toString();

         int result = s1.compareTo(s2);

         if (result < 0)
         {
            return -1;
         }
         else if (result > 0)
         {
            return 1;
         }
         else
         {
            return 0;
         }
      }
   }

   /**
    * Compare two rows starting at column 0
    * @param row1 The first row
    * @param row2 The second row
    */
   public int compare(int row1, int row2)
   {
/*   there is no support for sorting by multiple rows
      for (int level = 0; level < sortingColumns.size(); level++)
      {
         Integer column = (Integer) sortingColumns.elementAt(level);

         int result = compareRowsByColumn(row1, row2, column.intValue());

         if (result != 0)
         {
            return ascending ? result : -result;
         }
      }
*/
      int result = compareRowsByColumn(row1, row2, sortingColumn);
      if (result != 0)
      {
         return ascending ? result : -result;
      }
      return 0;
   }

   /**
    * Called if the tables state has changed
    * @param e The event
    */
   public void tableChanged(TableModelEvent e)
   {
      fireTableChanged(e);
   }

   /**
    * Checks the model
    */
   public void checkModel()
   {
      if (indexes.length != getRowCount())
      {
         trace.warn("Sorter not informed of a change in model.");
      }
   }

   /**
    * Sort the
    */
   private void sort(Object sender)
   {
      // Do not allow sorting if data is being fetched

      if (isFetching)
      {
         return;
      }

      // do the sort

      checkModel();
      shuttlesort((int[]) indexes.clone(), indexes, 0, indexes.length);
   }

   /** This is a home-grown implementation which we have not had time
    * to research - it may perform poorly in some circumstances. It
    * requires twice the space of an in-place algorithm and makes
    * NlogN assigments shuttling the values between the two
    * arrays. The number of compares appears to vary between N-1 and
    * NlogN depending on the initial order but the main reason for
    * using it here is that, unlike qsort, it is stable. */
   private void shuttlesort(int from[], int to[], int low, int high)
   {
      if (high - low < 2)
      {
         return;
      }

      int middle = (low + high) / 2;
      shuttlesort(to, from, low, middle);
      shuttlesort(to, from, middle, high);

      int p = low;
      int q = middle;

      /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

      if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0)
      {
         for (int i = low; i < high; i++)
         {
            to[i] = from[i];
         }

         return;
      }

      // A normal merge.

      for (int i = low; i < high; i++)
      {
         if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
         {
            to[i] = from[p++];
         }
         else
         {
            to[i] = from[q++];
         }
      }
   }

   /**
    *
    */
   private void sortByColumn(int column, boolean ascending)
   {
      if (column == 0)
      {
         reallocateIndexes();
         tableChanged(new TableModelEvent(this));

         return;
      }

      this.isSorted = true;
      this.ascending = ascending;
      this.sortingColumn = column;
      sort(this);

      // notify table

      tableChanged(new TableModelEvent(this));
   }

   /**
    * Add a statusBar to show number of objects.
    * <p>
    * (set by fetchObjects)
    */
   public void setStatusBar(JTextField statusBar)
   {
      this.statusBar = statusBar;

      if (statusBar != null)
      {
         this.statusBar.setEditable(false);
         this.statusBar.setForeground(java.awt.Color.black);
         this.statusBar.setBackground(java.awt.Color.lightGray);
      }

   }

   /**
    * Add a mouse listener to the Table to trigger a table sort
    * when a column heading is clicked in the JTable.
    */
   public void addMouseListenerToHeaderInTable(JTable table)
   {
      // Install the sorter

      final IteratorTableModel sorter = this;
      final JTable tableView = table;

      // Add HeaderRenderers
      // first column shows empty field for hasLineNumbers() == true

      int i = 0;

      //@todo fix that

      if (true    /***table.getNumberColumnDisplayed()***/)
      {
         tableView.getColumnModel().getColumn(i).setHeaderRenderer(
               new SortedTableHeaderRenderer()
               {
                  public Component getTableCellRendererComponent(JTable table2,
                        Object value, boolean isSelected, boolean hasFocus,
                        int row, int column)
                  {
                     return super.getTableCellRendererComponent(table2, "",
                           isSelected, hasFocus, row, column);
                  }
               });

         i++;
      }

      // The standard header renderer

      final SortedTableHeaderRenderer renderer = new SortedTableHeaderRenderer();
      int max = tableView.getColumnCount();

      for (; i < max; i++)
      {
         tableView.getColumnModel().getColumn(i).setHeaderRenderer(renderer);
      }

      tableView.setColumnSelectionAllowed(false);

      MouseAdapter listMouseListener = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK)
                  == InputEvent.BUTTON3_MASK)
            {
               return;
            }

            // request the focus for our table

            tableView.requestFocus();

            // handle editing of cells

            if (tableView.isEditing())
            {
               tableView.getCellEditor().stopCellEditing();
            }

            // get Column for sorting and do that

            TableColumnModel columnModel = tableView.getColumnModel();
            int viewColumn = columnModel.getColumnIndexAtX(e.getX());
            int column = tableView.convertColumnIndexToModel(viewColumn);

            if (e.getClickCount() == 1 && column != -1)
            {
               ascending = !ascending;

               sorter.sortByColumn(column, ascending);
               renderer.setSortingColumn(column, ascending);

               // now call an immediate update of the headers

               tableView.getTableHeader().resizeAndRepaint();
            }
         }
      };

      JTableHeader th = tableView.getTableHeader();

      th.addMouseListener(listMouseListener);
   }

   /**
    * temporary workaround for header repaint after user added a row
    * @param table the JTable
    */
   public void repaintHeader(JTable table)
   {
      int count = table.getColumnCount();

      if ((count < 1) || (count == 1))
      {
         return;
      }

      TableCellRenderer renderer = table.getColumnModel().getColumn(1).getHeaderRenderer();

      if (renderer instanceof SortedTableHeaderRenderer)
      {
         ((SortedTableHeaderRenderer) renderer).setSortingColumn(0, true);
      }

      table.getTableHeader().resizeAndRepaint();
   }
}
