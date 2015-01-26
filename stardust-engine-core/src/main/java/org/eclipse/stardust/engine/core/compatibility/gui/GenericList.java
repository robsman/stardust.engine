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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.*;

import org.eclipse.stardust.common.EnumerationIteratorWrapper;
import org.eclipse.stardust.common.error.InternalException;


/**
 * The generic list can handle java objects and can display them in a JList
 */
public class GenericList extends JList
{
   protected JPopupMenu popupMenu;

   /**
    * Constructor needs class of objects that will be listed.
    * <p/>
    * Creates list with zero rows.
    * 
    * @param type         The class of the objects that will be in the list
    * @param propertyName The name of the property that returns the String for
    *                     the list entries
    *                     <p/>
    *                     Hint: The default selectionmode is ListSelectionModel.SINGLE_SELECTION
    * @see javax.swing.ListSelectionModel#SINGLE_SELECTION
    */
   public GenericList(Class type, String propertyName)
   {
      this(type, null, propertyName);
   }

   /**
    * Constructor can additionally accept an iterator.
    * 
    * @param type         The class of the objects that will be in the list
    * @param objectItr    An Iterator
    * @param propertyName The name of the property that returns the String for
    *                     the list entries
    *                     <p/>
    *                     Hint: The default selectionmode is ListSelectionModel.SINGLE_SELECTION
    * @see javax.swing.ListSelectionModel#SINGLE_SELECTION
    */
   public GenericList(Class type, Iterator objectItr, String propertyName)
   {
      super();

      initialize(type, objectItr, propertyName);
   }

   /**
    * Constructor can additionally accept an iconMethod for returning an icon.
    * 
    * @param type         The class of the objects that will be in the list
    * @param objectItr    An Iterator
    * @param propertyName The name of the property that returns the String for
    *                     the list entries
    * @param iconMethod   The name of the Method that returns an Icon
    *                     <p/>
    *                     Hint: The default selectionmode is ListSelectionModel.SINGLE_SELECTION
    * @see javax.swing.ListSelectionModel#SINGLE_SELECTION
    */
   public GenericList(Class type, Iterator objectItr, String propertyName,
         String iconMethod)
   {
      this(type, objectItr, propertyName);

      setIconMethod(iconMethod);
   }

   /**
    * Initialize the list
    * 
    * @param type         The class of the objects in the iterator
    * @param objectItr    The Iterator holding the row's objects
    * @param propertyName The name of the getter-Method without the starting
    *                     'get'
    *                     <p/>
    *                     Hint: The selectionmode is set to ListSelectionModel.SINGLE_SELECTION
    * @see javax.swing.ListSelectionModel#SINGLE_SELECTION
    */
   private void initialize(Class type, Iterator objectItr, String propertyName)
   {
      // Init the listmodel that will do the rest of the work

      GenericListModel listModel =
            new GenericListModel(type, objectItr, propertyName);
      setModel(listModel);

      // Use a custom cell renderer

      setCellRenderer(new GenericListCellRenderer());

      // Select first index(row) if possible

      if (getModel().getSize() > 0)
      {
         setSelectedIndex(0);
      }

      if (getSelectionModel() != null)
      {
         getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      }
   }

   /**
    * Sets the list enabled or disabled.
    * 
    * @param enabled Indicates whether the list is enabled or not
    */
   public void setEnabled(boolean enabled)
   {
      super.setEnabled(enabled);

      if (!enabled)
      {
         setBackground(GUI.DisabledColor);
         setForeground(GUI.DisabledTextColor);
         clearSelection();
      }
      /*******
       // @todo .. realize a readonly state
       else if (isReadonly())
       {
       field.setBackground(GUI.ReadOnlyColor);
       field.setForeground(GUI.ReadOnlyTextColor);
       flagLabel.setIcon(GUI.getOptionalIcon());
       }
       **************/
      else // regular state
      {
         setBackground(GUI.DefaultColor);
         setForeground(GUI.DefaultTextColor);
         /*********
          // @todo ... realize a mandatory state
          if (mandatory && (getText() == null || getText().length() == 0))
          {
          flagLabel.setIcon(GUI.getMandatoryIcon());
          }
          else
          {
          flagLabel.setIcon(GUI.getOptionalIcon());
          }
          ****************/
      }
   }

   /**
    * Sets the icons for the list items visible or invisible.
    * 
    * @param iconsVisible Indicates whether the icons of list entries are visible or not
    */
   public void setIconsVisible(boolean iconsVisible)
   {
      ((GenericListCellRenderer) getCellRenderer()).setIconsVisible(iconsVisible);
   }

   /**
    * Reinitializes the list content.
    * 
    * @param enumeration The enumeration containing the list entries
    */
   public void setEnumeration(Enumeration enumeration)
   {
      setIterator(new EnumerationIteratorWrapper(enumeration));
   }

   /**
    * Reinitializes the list content.
    * 
    * @param objectItr The Iterator object containing the list entries
    */
   public void setIterator(Iterator objectItr)
   {
      ((GenericListModel) getModel()).setIterator(objectItr);
   }

   /**
    * Reinitializes the list content.
    * 
    * @param collection The collection containing the list entries
    */
   public void setCollection(Collection collection)
   {
      if (collection != null)
      {
         ((GenericListModel) getModel()).setIterator(collection.iterator());
      }
      else
      {
         ((GenericListModel) getModel()).setIterator(null);
      }
   }

   /**
    * Sets the method for retrieving the icons of the list's items representing
    * objects of class/interface type or subinterfaces/subclasses.
    * The method named name must return an object assignable to IconImage
    * 
    * @param name The name of the method that returns the icons
    */
   public void setIconMethod(String name)
   {
      ((GenericListModel) getModel()).setIconMethod(name);
   }

   /**
    * Sets the load inkrement.
    */
   public void setLoadInkrement(int inkrement)
   {
      if (getModel() instanceof GenericListModel)
      {
         ((GenericListModel) getModel()).maxCount = inkrement;
      }
      else
      {
         throw new InternalException("Unexpected model class");
      }
   }

   /**
    * Sets the method for retrieving the data of the list's items representing
    * objects of class/interface type or subinterfaces/subclasses.
    * The method named name must return an object assignable to IconImage
    * 
    * @param name The name of the property method
    */
   public void setPropertyMethod(String name)
   {
      invalidate();

      ((GenericListModel) getModel()).setPropertyMethod(name);

      revalidate();
   }

   /**
    * Sets the popup menu for the list items representing objects of class/interface
    * type or subinterfaces/subclasses.
    * 
    * @param popupMenu The popup menu that will be assigned to this list
    */
   public void setPopupMenu(JPopupMenu popupMenu)
   {
      this.popupMenu = popupMenu;
      PopupAdapter.create(this, popupMenu);
   }

   /**
    * Adds an object. Allows no duplicates.
    * 
    * @param object The unique object to be added to the list
    */
   public void addObject(Object object)
   {
      if (!((GenericListModel) getModel()).contains(object))
      {
         ((GenericListModel) getModel()).addElement(object);
      }
   }

   /**
    * Adds an object array. Allows no duplicates.
    * 
    * @param objectArray The array with unique objects to be added to the list
    */
   public void addObjectArray(Object[] objectArray)
   {
      if (objectArray != null)
      {
         int _max = objectArray.length;
         for (int _i = 0; _i < _max; _i++)
         {
            addObject(objectArray[_i]);
         }
      }
   }

   /**
    * Returns the load inkrement or '-1' if there is no inkrement
    */
   public int getLoadInkrement()
   {
      if (this.getModel() instanceof GenericListModel)
      {
         return ((GenericListModel) getModel()).maxCount;
      }
      else
      {
         return -1;
      }
   }

   /**
    * Gets (the first) selected object.
    */
   public Object getSelectedObject()
   {
      return getSelectedValue();
   }

   /**
    * Gets all selected objects.
    */
   public Object[] getSelectedObjects()
   {
      return getSelectedValues();
   }

   /**
    * Gets all objects contained in the list.
    */
   public Object[] getObjects()
   {
      return ((GenericListModel) getModel()).toArray();
   }

   /**
    * Gets the object at given index.
    * 
    * @param index The index of the object
    */
   public Object getObjectAt(int index)
   {
      return ((GenericListModel) getModel()).toArray()[index];
   }

   /**
    * Removes the object at given index.
    * 
    * @param index The index of the Object that should be deleted.
    */
   public void removeObjectAt(int index)
   {
      ((GenericListModel) getModel()).removeElementAt(index);
   }

   /**
    * Clears the list and removes all objects from the underlying model
    */
   public void removeAllObjects()
   {
      ((GenericListModel) getModel()).removeAllElements();
   }

   /**
    * Gets the number of objects in the list.
    */
   public int getObjectCount()
   {
      return ((GenericListModel) getModel()).getSize();
   }

   /**
    * Marks the object selected.
    * 
    * @param object The object to be selected
    */
   public void setSelectedObject(Object object)
   {
      setSelectedValue(object, true);
   }

   /**
    * Marks the object selected.
    * 
    * @param object       Object to be selected.
    * @param shouldScroll Scrolling allowed to the show the selected object.
    */
   public void setSelectedObject(Object object, boolean shouldScroll)
   {
      setSelectedValue(object, shouldScroll);
   }

   /**
    * Deselects all selected objects.
    */
   public void deselectAll()
   {
      getSelectionModel().clearSelection();
   }

   public void setIconProvider(IconProvider provider)
   {
      GenericListModel listModel = (GenericListModel) getModel();
      listModel.setIconProvider(provider);
   }

   /**
    * The renderer for any item in the list.
    */
   static class GenericListCellRenderer extends JLabel implements ListCellRenderer
   {
      private static ImageIcon selectedIcon;
      private static ImageIcon notSelectedIcon;
      private boolean iconsVisible;

      static
      {
         notSelectedIcon = new ImageIcon(GenericListCellRenderer.class.getResource("images/folder.gif"));
         selectedIcon = new ImageIcon(GenericListCellRenderer.class.getResource("images/folderOpen.gif"));
      }

      /**
       * Default constructor
       */
      public GenericListCellRenderer()
      {
         super();

         setOpaque(true);

         iconsVisible = false;
      }

      /**
       * Sets the icons for the list items visible or invisible.
       * 
       * @param iconsVisible Whether the icons are visible or not
       */
      public void setIconsVisible(boolean iconsVisible)
      {
         this.iconsVisible = iconsVisible;
      }

      /**
       * Needed to implement ListCellRenderer
       */
      public java.awt.Component getListCellRendererComponent(JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean hasFocus)
      {
         // Get the listmodel

         GenericListModel listModel = (GenericListModel) list.getModel();

         // Set icons

         if (iconsVisible)
         {
            if (listModel.getIconProvider() != null && value != null)
            {
               setIcon(listModel.getIconProvider().getIcon(value));
            }
            else
            {
               Method iconMethod = listModel.iconMethod;

               if (iconMethod != null)
               {
                  try
                  {
                     if (value != null)
                     {
                        setIcon((ImageIcon) iconMethod.invoke(value));
                     }
                     else
                     {
                        setIcon(null);
                     }
                  }
                  catch (Exception e)
                  {
                     throw new InternalException(e);
                  }
               }
               else
               {
                  if (isSelected)
                  {
                     setIcon(selectedIcon);
                  }
                  else
                  {
                     setIcon(notSelectedIcon);
                  }
               }
            }
         }

         // Set colors

         if (isSelected)
         {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         }
         else
         {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }

         // Set text

         String text = null;
         Method propertyMethod = ((GenericListModel) list.getModel()).propertyMethod;

         if (propertyMethod != null)
         {
            try
            {
               if (value != null)
               {
                  text = propertyMethod.invoke(value).toString();
               }
            }
            catch (Exception e)
            {
               throw new InternalException(e);
            }
         }
         else
         {
            if (value != null)
            {
               text = value.toString();
            }
         }

         setText(text);

         return this;
      }
   }

}


