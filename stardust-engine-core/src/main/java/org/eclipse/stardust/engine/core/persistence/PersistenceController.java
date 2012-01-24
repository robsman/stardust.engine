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

import java.util.Set;

/**
 *
 */
public interface PersistenceController
{
   /**
    * Retrieves the corresponding persistent object. If not existing, it will be created.
    */
   Persistent getPersistent();

   void markDeleted();

   void markDeleted(boolean writeThrough);

   void fetchLink(String linkName);

   void fetchVector(String vectorName);
   
   boolean isLocked();
   
   void markLocked();
   
   void markModified();

   void markModified(String fieldName);

   void fetch();

   void reload() throws PhantomException;

   void reloadAttribute(String attributeName) throws PhantomException;

   boolean isModified();
   
   /**
    * @return The names of all modified fields, <code>null</code> if no hint on which
    *         fields were updated is existent.
    */
   Set<String> getModifiedFields();

   boolean isCreated();

   void close();

   Session getSession();

   void markCreated();

}
