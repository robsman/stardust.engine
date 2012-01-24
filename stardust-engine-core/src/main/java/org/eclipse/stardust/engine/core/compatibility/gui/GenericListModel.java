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
import java.util.Iterator;

import javax.swing.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;


/**
 *  List model for the generic list.
 */
public class GenericListModel extends DefaultListModel
{
   /*
    * The class of all objects being obtained by the iterator
    */
   private Class type;
   /**
    * Iterator
    */
   protected Iterator objectItr;
   /**
    * the method used to get the informational data
    */
   protected Method propertyMethod;
   /**
    * method to get the icon from
    */
   protected Method iconMethod;
   /**
    * objects to be read in at once
    */
   public int maxCount = 10000;
   /**
    * boolean to decide whether a fetch is needed or not.
    */
   boolean fetchIt = false;

   private IconProvider iconProvider;

   /**
    * Constructor needs type, objectItr & propertyName
    */
   public GenericListModel(Class type, Iterator objectItr, String propertyName)
   {
      Assert.isNotNull(type);
      Assert.isNotNull(propertyName);

      // Set type and propertyMethod

      this.type = type;

      setPropertyMethod(propertyName);

      // Populate the model

      setIterator(objectItr);
   }

   /** Sets the method for retrieving the data of the nodes representing
    * objects of class/interface type or subinterfaces/subclasses.
    * The method named name must return an object
    * @param name The name of the property method
    */
   public void setPropertyMethod(String name)
   {
      try
      {
         propertyMethod = type.getMethod("get" + name);
      }
      catch (NoSuchMethodException x)
      {
         throw new InternalException(x);
      }
   }

   /** Sets the method for retrieving the icons of the nodes representing
    * objects of class/interface type or subinterfaces/subclasses.
    * The method named name must return an object assignable to IconImage
    * @param name The name of the method that returns the icons
    */
   public void setIconMethod(String name)
   {
      try
      {
         iconMethod = type.getMethod(name);
      }
      catch (NoSuchMethodException x)
      {
         throw new InternalException(x);
      }
   }

   /**
    * Sets a new iterator.
    * @param objectItr The Iterator
    */
   public void setIterator(Iterator objectItr)
   {
      this.objectItr = objectItr;

      // Clear the list

      clear();

      // Populate it again

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

      // Fetch all objects

      while (objectItr.hasNext())
      {
         addElement(objectItr.next());
      }
   }

   /**
    * Fetches <code>count</code> objects.
    * @param count The number of objects to be fetched
    */
   public void fetchObjects(int count)
   {
      if (objectItr == null)
      {
         return;
      }

      // Fetch max. count objects

      int counter = 0;

      while (objectItr.hasNext())
      {
         addElement(objectItr.next());
         counter++;

         if (counter >= count)
         {
            break;
         }
      }
   }

   /**
    * Gets the object at given index.
    * @param index The index of the object
    */
   public Object getElementAt(int index)
   {
      if (index == (size() - 1))
      {
         if (fetchIt)
         {
            fetchIt = false;
            fetchObjects(maxCount);
         }
         else
         {
            fetchIt = true;
         }// end switch

      }

      return super.get(index);
   }

   /**
    * Gets the object at the given index
    * @param index the index of the object
    */
   public Object elementAt(int index)
   {
      return getElementAt(index);
   }

   /**
    * Gets the object at the given index
    * @param index The index of the object
    */
   public Object get(int index)
   {
      return getElementAt(index);
   }

   public IconProvider getIconProvider()
   {
      return iconProvider;
   }

   public void setIconProvider(IconProvider provider)
   {
     this.iconProvider = provider;
   }
}
