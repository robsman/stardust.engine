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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;


/**
 *  Generic, configurable search panel for searching objects with specific
 * attributes.
 */
public class GenericSearchPanel extends JPanel implements ActionListener
{
   private String predicateProperties[];
   private String[] fieldLabels;
   private Method findMethod;
   private Object invokeOnObject;
   private Class invokeOnType;
   private Class retrievedType;
   private JPanel searchButtonPanel;
   private JButton searchButton;
   private JCheckBox exactBox;
   private JCheckBox sensitiveBox;
   private GenericTable table;
   private LabeledComponentsPanel predicateEntries;
   private String defaultPredicate;

   /**
    * @param invokeOnType
    * @param findMethodName
    * @param retrievedType
    * @param predicateProperties
    * @param fieldLabels
    * @param propertyNames
    * @param columnHeaders
    */
   public GenericSearchPanel(Class invokeOnType, String findMethodName,
         Class retrievedType, String[] predicateProperties,
         String[] fieldLabels, String[] propertyNames,
         String[] columnHeaders)
   {
      Assert.isNotNull(invokeOnType);
      Assert.isNotNull(retrievedType);
      Assert.isNotNull(findMethodName);

      this.retrievedType = retrievedType;
      this.predicateProperties = predicateProperties;
      this.fieldLabels = fieldLabels;
      this.invokeOnType = invokeOnType;
      this.invokeOnObject = null;

      // Lookup scan methods

      lookupMethod(invokeOnType, findMethodName);

      // Layout GUI

      layoutPanel(propertyNames, columnHeaders);

      defaultInit();
   }

   /**
    *
    */
   private void lookupMethod(Class invokeOnType, String findMethodName)
   {
      try
      {
         findMethod = invokeOnType.getMethod(findMethodName, new Class[]{String.class});
      }
      catch (NoSuchMethodException e)
      {
         throw new InternalException("No such method \"" + findMethod + "\" in class \"" +
               invokeOnType.getName() + "\"");
      }
      catch (SecurityException e)
      {
         throw new InternalException("The method \"" + findMethod + "\" is not accessible for class \"" +
               invokeOnType.getName() + "\"");
      }
   }

   /**  */
   private void defaultInit()
   {
      defaultPredicate = "";
   }

   /**
    *
    */
   private void layoutPanel(String propertyNames[], String columnHeaders[])
   {
      Assert.isNotNull(propertyNames);
      Assert.isNotNull(columnHeaders);

      // Layout only once

      if (searchButton != null)
      {
         return;
      }

      // Simple layout

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      setBorder(GUI.getEmptyPanelBorder());

      JPanel searchPanel = new JPanel();

      searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
      searchPanel.setBorder(GUI.getTitledPanelBorder("Suchkriterien"));

      // Search panel

      searchButton = new JButton("Neue Suche");
      searchButton.addActionListener(this);

      exactBox = new JCheckBox("Exakte Suche", false);
      sensitiveBox = new JCheckBox("Gross-/Kleinschreibung beachten", true);

      searchButtonPanel = new JPanel();

      searchButtonPanel.setLayout(new BoxLayout(searchButtonPanel, BoxLayout.X_AXIS));
      searchButtonPanel.add(searchButton);
      searchButtonPanel.add(Box.createHorizontalStrut(GUI.HorizontalWidgetDistance));
      searchButtonPanel.add(exactBox);
      searchButtonPanel.add(Box.createHorizontalStrut(GUI.HorizontalWidgetDistance));
      searchButtonPanel.add(sensitiveBox);
      searchButtonPanel.add(Box.createHorizontalGlue());
      searchPanel.add(searchButtonPanel);
      searchPanel.add(Box.createVerticalStrut(10));

      // Predicate entries

      predicateEntries = getPredicatePanel();

      JPanel predicateEntryPanel = new JPanel();
      predicateEntryPanel.setLayout(new BoxLayout(predicateEntryPanel,
            BoxLayout.X_AXIS));
      predicateEntryPanel.add(predicateEntries);
      predicateEntryPanel.add(Box.createHorizontalGlue());
      searchPanel.add(predicateEntryPanel);

      // Results table

      table = new GenericTable(retrievedType, propertyNames, columnHeaders);

      table.getTable().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      table.setNumberColumnDisplayed(false);
      table.setStatusBarVisible(false);
      table.getTable().setShowVerticalLines(true);
      ;
      table.getTable().setShowHorizontalLines(false);
      ;
      table.getTable().setCellSelectionEnabled(false);
      ;
      table.getTable().setColumnSelectionAllowed(false);
      ;
      table.getTable().setRowSelectionAllowed(true);
      table.setBackground(Color.white);

      // SysConsole add

      add(searchPanel);
      add(Box.createVerticalStrut(10));
      add(table);

      // Register keyboardActions

      registerKeyBoardActions();
   }

   /**
    *
    */
   public LabeledComponentsPanel getPredicatePanel()
   {
      LabeledComponentsPanel content = new LabeledComponentsPanel();

      boolean isSized = false;

      for (int i = 0; i < predicateProperties.length; i++)
      {
         try
         {
            Method method = retrievedType.getMethod("get" + predicateProperties[i]);

            Entry entry = (Entry) GuiMapper.getComponentForClass(method.getReturnType());

            // Ensure save size of textfields (to stop fields from undesired growing)

            if (!isSized && entry instanceof AbstractEntry)
            {
               isSized = true;
               ((AbstractEntry) entry).setColumns(24);
            }

            // Add the field with correct name

            if (fieldLabels == null)
            {
               content.add((JComponent) entry, predicateProperties[i], 0);
            }
            else
            {
               content.add((JComponent) entry, fieldLabels[i], 0);
            }
         }
         catch (NoSuchMethodException e)
         {
            throw new InternalException("The property access method \"get" + predicateProperties[i] + "\" does not exist.");
         }
      }

      content.pack();

      return content;
   }

   /**
    */
   public void setStartObject(Object object)
   {
      invokeOnObject = object;

      search();
   }

   /**
    *	Constructs a predicate for the find method, using the content of the
    * predicate fields.
    */
   private String getPredicate(boolean isExact, boolean isSensitive)
   {
      String predicate = "";
      String delim = "";
      String wildChar;
      String stringEquals;

      if (isExact)
      {
         wildChar = "";
      }
      else
      {
         wildChar = "*";
      }

      if (isSensitive)
      {
         stringEquals = " =~ ";
      }
      else
      {
         stringEquals = " =~~ ";
      }

      // Search related strings

      Entry entry;
      Object value;
      String string;

      for (int i = 0; i < predicateProperties.length; i++)
      {
         entry = (Entry) predicateEntries.getFieldAt(i);

         // @todo handle other fields also (not only textfields)

         if (entry instanceof JTextField)
         {
            string = ((JTextField) entry).getText();

            if (string.length() <= 0)
            {
               continue;
            }

            value = entry.getObjectValue();

            // Add to predicate

            predicate += delim + predicateProperties[i];

            if (value instanceof String)
            {
               predicate += stringEquals
                     + "\"" + string + wildChar + "\"";
            }
            else if (value instanceof Boolean)
            {
               if (Boolean.TRUE.equals((Boolean) value))
               {
                  predicate += " == 1";
               }
               else
               {
                  predicate += " == 0";
               }
            }
            else
            {
               predicate += " >= " + string;
            }

            delim = " AND ";
         }
      }

      return predicate;
   }

   /**
    *
    */
   private void registerKeyBoardActions()
   {
   }

   /**
    *
    */
   public void addKeyListener(KeyListener keyListener)
   {
      for (int i = 0; i < predicateProperties.length; i++)
      {
         predicateEntries.getFieldAt(i).addKeyListener(keyListener);
      }
   }

   /**
    *
    */
   public void removeKeyListener(KeyListener keyListener)
   {
      for (int i = 0; i < predicateProperties.length; i++)
      {
         predicateEntries.getFieldAt(i).removeKeyListener(keyListener);
      }
   }

   /**
    * Performs the find method.
    */
   private void search()
   {
      Assert.isNotNull(findMethod);

      // Correct predicate

      String predicate = getPredicate(exactBox.isSelected(),
            sensitiveBox.isSelected());

      if (!"".equals(defaultPredicate))
      {
         if ("".equals(predicate))
         {
            predicate = defaultPredicate;
         }
         else
         {
            predicate = defaultPredicate + " && " + predicate;
         }
      }

      // Populate the iterator

      Object iteratorObject = null;

      try
      {
         iteratorObject = findMethod.invoke(invokeOnObject, new Object[]{predicate});
      }
      catch (IllegalAccessException e)
      {
         throw new InternalException("The method \"" + findMethod.getName() + "\" cannot be accessed.");
      }
      catch (InvocationTargetException e)
      {
         throw new InternalException("The method \"" + findMethod.getName() + "\" has thrown an error: " + e.getMessage());
      }

      // Show found object in table

      if (iteratorObject instanceof java.util.Enumeration)
      {
         table.setEnumeration((java.util.Enumeration) iteratorObject);
      }
      else if (iteratorObject instanceof java.util.Iterator)
      {
         table.setIterator((java.util.Iterator) iteratorObject);
      }
   }

   /** */
   public void addLineNumberListener(ActionListener listener)
   {
      table.addLineNumberListener(listener);
   }

   /** */
   public void removeLineNumberListener(ActionListener listener)
   {
      table.removeLineNumberListener(listener);
   }

   /**
    * Sets the predicate used, if all predicate fields are empty.
    */
   public void setDefaultPredicate(String defaultPredicate)
   {
      if (defaultPredicate == null)
      {
         this.defaultPredicate = "";
      }
      else
      {
         this.defaultPredicate = defaultPredicate.trim();
      }
   }

   /**
    *  Sets the predicate used, if all predicate fields are empty.
    */
   public String getDefaultPredicate()
   {
      return defaultPredicate;
   }

   /**
    *
    */
   public void setInteractiveMode(boolean isInteractive)
   {
      searchButtonPanel.setVisible(isInteractive);
   }

   /**
    *
    */
   public boolean isInteractiveMode()
   {
      return searchButtonPanel.isVisible();
   }

   /**
    *
    */
   public void setExactMode(boolean isExact)
   {
      exactBox.setSelected(isExact);
   }

   /**
    *
    */
   public boolean isExactMode()
   {
      return exactBox.isSelected();
   }

   /**
    *
    */
   public void setSensitiveMode(boolean isSensitive)
   {
      sensitiveBox.setSelected(isSensitive);
   }

   /**
    *
    */
   public boolean isSensitiveMode()
   {
      return sensitiveBox.isSelected();
   }

   /**
    * For direct search button access
    */
   public JButton getSearchButton()
   {
      return searchButton;
   }

   /**
    * @return	The objects that have been selected in the table.<p>
    *				Returns null if no row is selected.
    */
   public Object getSelectedObject()
   {
      return table.getSelectedObject();
   }

   /**
    * @return	The objects that have been selected in the table.<p>
    *				Returns null if no row is selected.
    */
   public Object[] getSelectedObjects()
   {
      return table.getSelectedObjects();
   }

   /**
    * @return The generic table managed by this panel.
    */
   public GenericTable getTable()
   {
      return table;
   }

   /**
    *
    */
   public void actionPerformed(ActionEvent e)
   {
      GUI.setWaiting(this, true);

      Object source = e.getSource();

      if (searchButton.equals(source))
      {
         search();
      }

      GUI.setWaiting(this, false);
   }
}
