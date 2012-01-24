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

import java.util.Iterator;

/**
 *
 */
public interface PersistentVector
{
   /**
    * Adds a relationship between this object and the specified object.
    * The object <tt>object</tt> may not be null.
    */
   public void add(Object object);

   /**
    * Removes a relationship between this object and the specified object.
    * The object <tt>object</tt> may not be null.
    * <p>
    * If the object was found and removed the method returns <tt>true</tt>
    * otherwise <tt>false</tt>
    */
   public boolean remove(Object object);

   /**
    * Removes all relationships with this object.
    */
   public void clear();

   /**
    * Tests whether this object is related to the specified object.
    */
   public boolean includes(Object object);

   /**
    * Tests whether any objects are related to this object.
    */
   public boolean exists();

   /**
    * Initializes an iterator to find all objects related to this object.
    */
   public Iterator scan();

   /**
    * Initializes an iterator to find all objects related to this object.
    */
   public Iterator iterator();

   /**
    * Returns the number of objects in the persistent vector.
    */
   public int size();
}
