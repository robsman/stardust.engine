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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.EnumerationIteratorWrapper;
import org.eclipse.stardust.common.error.InternalException;


/**
 *	Wrapper Class for a table.
 * <p>
 *	The 'real' table is stored internally. It can be retrieved by using the method
 *	<code>getTable()</code>. Also it is possible to access the colums directly
 *	by using the get/setValue-methods which are implemented in this Wrapper.
 *	But if you do so you are fully responsible for getting the appropriate
 * elements
 *	<p>
 *	Especially if you use the  <code>get/setValue</code>-methods be sure to
 *	use the right columns, i.e. make use of <code>getRowCount</code>,
 *	<code>getColumnCount</code> & <code>getStartingColumn()</code>.
 * <p>
 *	The easiset way to ensure that you are getting the right column is to
 *	identify the column by name:
 * <p>
 *	<pre>int column = getTable().getColumn(String identifier)</pre>
 * <p>
 *	If you want to perform checking of a column or simply want to get the
 * corresponding name of a column use
 * <p>
 * <pre>String string = getTable().getColumnName(int column)</pre>
 *
 */
public class GenericTable extends JPanel implements FocusListener
{
   final public static int NOTHING = 0;
   final public static int READONLY = 1;
   final public static int READWRITE = 2;

   private Color readOnlyColor;
   private boolean numberColumnDisplayed;
   /**
    * The internal 'real' table
    */
   protected JTable table;
   protected IteratorTableModel tableModel;
   /**
    * scrollpane for table
    */
   protected JScrollPane scrollPane;
   /**
    * Table status bar
    */
   protected JTextField statusBar;
   /**
    * Renderer for line columns // can have an actionListener assigned
    */
   protected ButtonRenderer buttonRenderer;
   /**
    *
    */
   protected PopupAdapter popup;

   private Vector popupMenuListener;

   /**
    * Constructor only needed for derived classes
    */
   protected GenericTable()
   {
      super();
   }

   /**
    * Constructor needs Names to be shown. Creates table with zero rows
    * @param type the class type for the table
    * @param propertyNames An array providing the method names for the columns
    * @param columnHeaders An array containing the labels for each column of the table
    */
   public GenericTable(Class type, String propertyNames[],
         String columnHeaders[])
   {
      this(type, null, propertyNames, columnHeaders);
   }

   /**
    * Constructor for creating a Table being used with property
    * classes as hashtables etc. cell content is not retrieved by invoking
    * <code>void setX(Type)/Type getX()</code> but
    * <code>void set("X", Type)/Type get("X")</code> if the <code>columnType</code>
    * is not null.<p>
    *
    * @param type The class Type for the model
    * @param propertyNames The names of the column properties
    * @param columnHeaders The header strings to be displayed
    * @param columnTypesForNamedValues The types of the columns
    * @param namedValueSetterPrefix
    * @param namedValueGetterPrefix
    */
   public GenericTable(Class type,
         String propertyNames[], String columnHeaders[],
         Class[] columnTypesForNamedValues,
         String namedValueSetterPrefix,
         String namedValueGetterPrefix)
   {
      super();

      Assert.isNotNull(propertyNames);
      Assert.isNotNull(columnHeaders);
      Assert.condition(propertyNames.length > 0);
      Assert.condition(columnHeaders.length == propertyNames.length);

      // Initialize the table model

      tableModel = new IteratorTableModel(type, null, propertyNames, columnHeaders,
            columnTypesForNamedValues, namedValueSetterPrefix,
            namedValueGetterPrefix);

      initialize();
   }

   /**
    * Constructor needs ObjectItr & Names to be shown
    * @param type the class type for the table
    * @param objectItr The iterator for the table rows
    * @param propertyNames An array providing the method names for the columns
    * @param columnHeaders An array containing the labels for each column of the table
    */
   public GenericTable(Class type, Iterator objectItr,
         String propertyNames[], String columnHeaders[])
   {
      super();

      Assert.isNotNull(propertyNames);
      Assert.isNotNull(columnHeaders);
      Assert.condition(propertyNames.length > 0);
      Assert.condition(columnHeaders.length == propertyNames.length);

      // Initialize the table model

      tableModel = new IteratorTableModel(type, objectItr, propertyNames, columnHeaders);

      initialize();
   }

   /**
    *
    */
   public void addPopupMenuListeners(TablePopupMenuListener listener)
   {
      if (listener != null)
      {
         popupMenuListener.add(listener);
      }
   }

   /**
    *
    */
   public Iterator getAllPopupMenuListeners()
   {
      if (popupMenuListener != null)
      {
         return popupMenuListener.iterator();
      }
      else
      {
         return null;
      }
   }

   /**
    * Common part of all constructors.
    */
   private void initialize()
   {
      numberColumnDisplayed = true;

      popupMenuListener = new Vector(30);

      initializeTable();

      // Let the model modifiy the statusBar

      ((IteratorTableModel) table.getModel()).setStatusBar(statusBar);

      // Layout the panel

      setLayout(new java.awt.BorderLayout());

      add(BorderLayout.CENTER, scrollPane);
      add(BorderLayout.SOUTH, statusBar);

      // Select first row if possible

      if (getRowCount() > 0)
      {
         setSelectedRow(0);
      }

      // Add FocusListener

      addFocusListener(this);
   }

   /**
    * Initialize the table.
    */
   private void initializeTable()
   {
      // Create the table

      table = new JTable(tableModel);

      // Add header listener for sorting

      tableModel.addMouseListenerToHeaderInTable(table);

      // Set selection behavior
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.setColumnSelectionAllowed(false);
      table.setRowSelectionAllowed(true);
      table.setCellSelectionEnabled(false);

      // Put the table into a scrollpane
      scrollPane = new JScrollPane(table);

      // StartPos of real data columns

      int colOffset = 0;

      // Check for line number column

      if (getNumberColumnDisplayed())
      {
         TableColumn indexColumn = table.getColumnModel().getColumn(0);

         indexColumn.setCellRenderer(buttonRenderer = new ButtonRenderer());
         indexColumn.setCellEditor(buttonRenderer);

         JTextField dummyField = new JTextField();
         java.awt.FontMetrics metrics = dummyField.getFontMetrics(dummyField.getFont());

         indexColumn.setPreferredWidth(metrics.stringWidth("1www"));
         indexColumn.setMinWidth(metrics.stringWidth("1www"));
         indexColumn.setMaxWidth(1102);
      }

      TableColumn column;

      // Set cell renderers and editors for property columns

      for (int i = 1; i < tableModel.getColumnCount(); i++)
      {
         column = table.getColumnModel().getColumn(i);

         column.setCellRenderer(new GenericTableCellRenderer(tableModel.getColumnType(i)));
         column.setCellEditor(new GenericTableCellEditor(tableModel.getColumnType(i)));
      }

      // Init the statusBar

      statusBar = new JTextField()
      {
         public boolean isFocusTraversable()
         {
            return false;
         }
      };

      statusBar.setVisible(false);
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
      return tableModel.getEditable();
   }

   /**
    * Set whether table is editable or not
    * @param editable Sets the table to be editable or not
    */
   public void setEditable(boolean editable)
   {
      tableModel.setEditable(editable);
   }

   /**
    * Gets the background color for readonly table entries.
    */
   public Color getReadonlyColor()
   {
      return readOnlyColor;
   }

   /**
    * Sets the background color for readonly table entries.
    */
   public void setReadonlyColor(Color color)
   {
      readOnlyColor = color;

      // Update table

      table.repaint();
   }

   /**
    * Checks wether the statusbar is visible.
    */
   public boolean isStatusBarVisible()
   {
      return getStatusBarVisible();
   }

   /**
    * Checks wether the statusbar is visible.
    */
   public boolean getStatusBarVisible()
   {
      return statusBar.isVisible();
   }

   /**
    * Sets the statusbar visible or invisible.
    */
   public void setStatusBarVisible(boolean isVisible)
   {
      boolean currentlyVisible = statusBar.isVisible();

      if (currentlyVisible != isVisible)
      {
         statusBar.setVisible(isVisible);
      }
   }

   /**
    * Returns the load inkrement or '-1' if there is no inkrement
    */
   public int getLoadInkrement()
   {
      if (table.getModel() instanceof IteratorTableModel)
      {
         return ((IteratorTableModel) table.getModel()).maxCount;
      }
      else
      {
         return -1;
      }
   }

   /**
    * Sets the load inkrement.
    */
   public void setLoadIncrement(int inkrement)
   {
      if (table.getModel() instanceof IteratorTableModel)
      {
         ((IteratorTableModel) table.getModel()).maxCount = inkrement;
      }
      else
      {
         throw new InternalException("Unexpected model class");
      }
   }

   /**
    * Gets whether the number column is displayed.
    */
   public boolean isNumberColumnDisplayed()
   {
      return getNumberColumnDisplayed();
   }

   /**
    * Gets whether the number column is displayed.
    */
   public boolean getNumberColumnDisplayed()
   {
      return numberColumnDisplayed;
   }

   /**
    * Sets whether the number column is displayed.
    */
   public void setNumberColumnDisplayed(boolean numberColumnDisplayed)
   {
      this.numberColumnDisplayed = numberColumnDisplayed;

      TableColumn numberColumn = table.getColumnModel().getColumn(0);

      if (numberColumnDisplayed)
      {
         table.addColumn(numberColumn);
         table.moveColumn(getColumnCount() - 1, 0);
      }
      else
      {
         table.removeColumn(numberColumn);
      }
   }

   /**
    * Constructor needs Names to be shown. Creates table with zero rows
    * @param type the class type for the table
    * @param propertyNames An array providing the method names for the columns
    * @param columnHeaders An array containing the labels for each column of the table
    */
   public void setModel(Class type, String propertyNames[], String columnHeaders[])
   {
      Assert.isNotNull(table, "Table instance is not null");
      Assert.isNotNull(propertyNames);
      Assert.isNotNull(columnHeaders);
      Assert.condition(propertyNames.length > 0);
      Assert.condition(columnHeaders.length == propertyNames.length);

      IteratorTableModel _oldModel = null;

      try
      {
         if ((table.getModel() != null)
               && (table.getModel() instanceof IteratorTableModel)
         )
         {
            _oldModel = (IteratorTableModel) table.getModel();
         }

         int _selectionMode = table.getSelectionModel().getSelectionMode();
         boolean _isEditable = isEditable();
         boolean _numberColumnDisplayed = isNumberColumnDisplayed();
         boolean _statusbarVisible = isStatusBarVisible();
         boolean _showVerticalline = table.getShowVerticalLines();
         boolean _showHorizontalLines = table.getShowHorizontalLines();
         boolean _cellSelectionEnabled = table.getCellSelectionEnabled();
         boolean _columnSelectionAllowed = table.getColumnSelectionAllowed();
         boolean _rowSelectionAlowed = table.getRowSelectionAllowed();
         Color _backgroundColor = table.getBackground();

         // Initialize the table model (!!! but don't reset the properties like 'showNumbers'!!
         // => so don't call method initialize

         tableModel = new IteratorTableModel(type, null, propertyNames, columnHeaders);
         table.setModel(tableModel);

         table.setSelectionMode(_selectionMode);
         setEditable(_isEditable);
         setStatusBarVisible(_statusbarVisible);
         table.setShowVerticalLines(_showVerticalline);
         table.setShowHorizontalLines(_showHorizontalLines);
         table.setCellSelectionEnabled(_cellSelectionEnabled);
         table.setColumnSelectionAllowed(_columnSelectionAllowed);
         table.setRowSelectionAllowed(_rowSelectionAlowed);
         table.setBackground(_backgroundColor);


         // Add header listener for sorting
         tableModel.addMouseListenerToHeaderInTable(table);

         // Let the model modifiy the statusBar
         ((IteratorTableModel) table.getModel()).setStatusBar(statusBar);

         // Check for line number column

         if (getNumberColumnDisplayed())
         {
            TableColumn _indexColumn = table.getColumnModel().getColumn(0);

            _indexColumn.setCellRenderer(buttonRenderer = new ButtonRenderer());
            _indexColumn.setCellEditor(buttonRenderer);

            JTextField _dummyField = new JTextField();
            java.awt.FontMetrics _metrics = _dummyField.getFontMetrics(_dummyField.getFont());

            _indexColumn.setPreferredWidth(_metrics.stringWidth("1www"));
            _indexColumn.setMinWidth(_metrics.stringWidth("1www"));
            _indexColumn.setMaxWidth(1102);
         }

         TableColumn _column = null;

         // Set cell renderers and editors for property columns

         for (int _i = 1; _i < tableModel.getColumnCount(); _i++)
         {
            _column = table.getColumnModel().getColumn(_i);

            _column.setCellRenderer(new GenericTableCellRenderer(tableModel.getColumnType(_i)));
            _column.setCellEditor(new GenericTableCellEditor(tableModel.getColumnType(_i)));
         }

         setNumberColumnDisplayed(_numberColumnDisplayed);

         // Select first row if possible
         if (getRowCount() > 0)
         {
            setSelectedRow(0);
         }

         // reset the old model
         if (_oldModel != null)
         {
            _oldModel.setStatusBar(null);
         }

      }
      catch (Exception _ex)
      {
         throw new InternalException(_ex);
      }
   }

   /**
    * Gets the internal table.
    */
   public JTable getTable()
   {
      return table;
   }

   /**
    * Fetch all objects in model.
    */
   public void fetchAllObjects()
   {
      ((IteratorTableModel) table.getModel()).fetchAllObjects();
   }

   /**
    * Sets a new enumeration for displaying its objects.
    */
   public void setEnumeration(Enumeration e)
   {
      setIterator(new EnumerationIteratorWrapper(e));
   }

   /**
    * Sets a new Iterator for displaying its objects.
    */
   public void setIterator(Iterator objectItr)
   {
      ((IteratorTableModel) table.getModel()).setIterator(objectItr);

      // Select first row if possible

      if (getRowCount() > 0)
      {
         setSelectedRow(0);
         addFocusListener(this);
      }
   }

   /**
    * Sets a new Collection for displaying its objects.
    */
   public void setCollection(Collection collection)
   {
      if (collection != null)
      {
         setIterator(collection.iterator());
      }
      else
      {
         setIterator(null);
      }
   }

   /**
    * Gets the number of rows e.g. number of internal objects.
    */
   public int getObjectCount()
   {
      return table.getModel().getRowCount();
   }

   /**
    * Gets the number of rows.
    */
   public int getRowCount()
   {
      return table.getModel().getRowCount();
   }

   /**
    * Gets number of columns.
    */
   public int getColumnCount()
   {
      return table.getModel().getColumnCount();
   }

   /**
    * Gets the selected row.
    */
   public int getSelectedRow()
   {
      return table.getSelectedRow();
   }

   /**
    * Gets the selected row.
    */
   public int[] getSelectedRows()
   {
      return table.getSelectedRows();
   }

   /**
    * Sets the selected row.
    */
   public void setSelectedRow(int i)
   {
      table.setRowSelectionInterval(i, i);
   }

   /**
    * Sets the selected row.
    */
   public void setSelectedRows(int start, int end)
   {
      table.setRowSelectionInterval(start, end);
   }

   /**
    * Selects an object, if contained in the list.
    */
   public boolean setSelectedObject(Object object)
   {
      if (object == null)
      {
         table.clearSelection();

         return true;
      }

      // Find object that is to be selected

      int max = getObjectCount();

      for (int i = 0; i < max; i++)
      {
         if (object.equals(((IteratorTableModel) table.getModel()).getObjectAt(i)))
         {
            setSelectedRow(i);
            scrollPane.getViewport().scrollRectToVisible(table.getCellRect(i, 0, true));

            break;
         }
      }

      return false;
   }

   /**
    * Gets a specific object.
    */
   public Object getValueAt(int row, int col)
   {
      return table.getValueAt(row, col);
   }

   /**
    * Set specific object.
    */
   public void setValueAt(Object aValue, int row, int col)
   {
      table.setValueAt(aValue, row, col);
   }

   /**
    * Gets Object at given row.
    */
   public Object getObjectAt(int row)
   {
      return ((IteratorTableModel) table.getModel()).getObjectAt(row);
   }

   /**
    * Gets all objects.
    *
    * This is intended for getting objects. If you want to set the values of an
    * object and make that change visible you are responsible for the table's
    * update.
    */
   public Vector getObjects()
   {
      return ((IteratorTableModel) table.getModel()).getObjects();
   }

   /** Set the selection mode of the internal table.
    *
    * @see javax.swing.ListSelectionModel for further information
    */
   public void setSelectionMode(int mode)
   {
      table.setSelectionMode(mode);
   }

   /**
    * Get Object of selected row.
    *
    * @return Returns null if no row is selected.
    */
   public Object getSelectedObject()
   {
      int row = table.getSelectedRow();

      if (row < 0)
      {
         return null;
      }

      return ((IteratorTableModel) table.getModel()).getObjectAt(row);
   }

   /**
    * Gets the objects for all selected rows.<P>
    *
    * @return Returns null if no row is selected.
    */
   public Object[] getSelectedObjects()
   {
      int[] rows = table.getSelectedRows();

      if (rows == null)
      {
         return null;
      }

      Object[] objects = new Object[rows.length];
      for (int i = 0; i < rows.length; i++)
      {
         objects[i] = ((IteratorTableModel) table.getModel()).getObjectAt(rows[i]);
      }

      return objects;
   }

   /**
    * Remove all objects.
    */
   public void removeAllObjects()
   {
      ((IteratorTableModel) table.getModel()).removeAllRows();
   }

   /**
    * Remove an object.
    */
   public void removeObjectAt(int i)
   {
      ((IteratorTableModel) table.getModel()).removeRow(i);
   }

   /**
    * Remove some objects.
    */
   public void removeObjectsAt(int[] rows)
   {
      ((IteratorTableModel) table.getModel()).removeRows(rows);
   }

   /**
    * Remove selected objects.
    */
   public void removeSelectedObjects()
   {
      ((IteratorTableModel) table.getModel()).removeRows(getSelectedRows());
   }

   /**
    * Adds an actionListener if GenericTable has line numbers.
    */
   public void addLineNumberListener(ActionListener actionListener)
   {
      IteratorTableModel tableModel = (IteratorTableModel) table.getModel();

      if (!getNumberColumnDisplayed() || buttonRenderer == null)
      {
         return;
      }

      buttonRenderer.addActionListener(actionListener);
   }

   /**
    *
    */
   public void removePopupMenuListeners(TablePopupMenuListener listener)
   {
      if (listener != null)
      {
         popupMenuListener.remove(listener);
      }
   }

   /**
    * Remove an actionListener if GenericTable has line numbers.
    */
   public void removeLineNumberListener(ActionListener actionListener)
   {
      IteratorTableModel tableModel = (IteratorTableModel) table.getModel();

      if (!getNumberColumnDisplayed() || buttonRenderer == null)
      {
         return;
      }

      buttonRenderer.removeActionListener(actionListener);
   }

   /**
    * Sets custom popup menu.
    */
   public void setPopupMenu(JPopupMenu popupMenu)
   {
      PopupAdapter.create(scrollPane, popupMenu);
      PopupAdapter.create(table.getTableHeader(), popupMenu);

      popup = PopupAdapter.create(this, popupMenu);
   }

   /**
    * Set tooltip for header and scrollpane.
    */
   public void setToolTipText(String text)
   {
      table.getTableHeader().setToolTipText(text);
      scrollPane.setToolTipText(text);
   }

   /**
    * Implements FocusListener.
    */
   public void focusGained(FocusEvent e)
   {
   }

   /**
    * Implements FocusListener.
    */
   public void focusLost(FocusEvent e)
   {
      // Temporary focus events are not handled in here

      if (e.isTemporary())
      {
         return;
      }

      // Only needed for handling editing in table

      if (table.isEditing())
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               if (table.hasFocus())
               {
                  return;
               }

               TableCellEditor cellEditor = table.getCellEditor();

               if (cellEditor == null)
               {
                  return;
               }
               else
               {
                  ButtonRenderer editor = (ButtonRenderer) cellEditor;

                  if (editor.isVisible() &&
                        !table.hasFocus() && !editor.hasFocus())
                  {
                     editor.stopCellEditing();
                  }
               }
            }
         });
      }
   }

   /**
    * Implements MouseListener
    */
   public void addMouseListener(MouseListener l)
   {
      table.getTableHeader().addMouseListener(l);
      scrollPane.addMouseListener(l);
      table.addMouseListener(l);
   }
}

