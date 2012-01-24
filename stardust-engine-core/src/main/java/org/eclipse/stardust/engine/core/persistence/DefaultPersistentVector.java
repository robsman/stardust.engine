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
package org.eclipse.stardust.engine.core.persistence;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.engine.core.persistence.OrderedVector;
import org.eclipse.stardust.engine.core.persistence.PersistentVector;


/**
 * Relies on implicit clustering. We may move this class to the database
 * package and merge it with the JDBC classes.
 */
public class DefaultPersistentVector implements PersistentVector, Serializable
{
   private OrderedVector objects;

   /**
    *
    */
   public DefaultPersistentVector()
   {
      objects = new OrderedVector(false);
   }
   /**
    *
    */
   public DefaultPersistentVector(boolean ordered)
   {
      objects = new OrderedVector(ordered);
   }
   /**
    * Adds a relationship between this object and the specified object.
    * The object <tt>object</tt> may not be null.
    */
   public void add(Object object)
   {
      Assert.isNotNull(object);

      objects.add(object);
   }

   /**
    * Removes a relationship between this object and the specified object.
    * The object <tt>object</tt> may not be null.
    * <p>
    * If the object was found and removed the method returns <tt>true</tt>
    * otherwise <tt>false</tt>
    */
   public boolean remove(Object object)
   {
      Assert.isNotNull(object);

      return objects.remove(object);
   }

   /**
    * Removes all relationships with this object.
    */
   public void clear()
   {
      objects.clear();
   }

   /**
    * Tests whether this object is related to the specified object.
    */
   public boolean includes(Object object)
   {
      return objects.contains(object);
   }

   /**
    * Tests whether any objects are related to this object.
    */
   public boolean exists()
   {
      return objects.size() > 0;
   }

   /**
    * Initializes an iterator to find all objects related to this object.
    */
   public Iterator iterator()
   {
      return scan();
   }

   /**
    * Initializes an iterator to find all objects related to this object.
    */
   public Iterator scan()
   {
      // Avoid concurrency problems

      return ((Vector) objects.clone()).iterator();
   }

   /**
    * Returns the number of objects in the persistent vector.
    */
   public int size()
   {
      return objects.size();
   }

}
