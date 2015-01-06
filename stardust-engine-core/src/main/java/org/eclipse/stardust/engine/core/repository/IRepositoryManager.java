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
package org.eclipse.stardust.engine.core.repository;

import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;

/**
 * @author sauer
 * @version $Revision: $
 */
public interface IRepositoryManager
{

   /**
    * Retrieves a read only view on the specified repository space for the current
    * partition.
    * 
    * @param space
    *           The space key
    * @return
    */
   IRepositoryContentProvider getContentProvider(RepositorySpaceKey space);

   /**
    * Returns the root folder for the current partition's space in the repository.
    * 
    * This folder might be used to directly access content with the
    * {@link DocumentManagementService}.
    * 
    * @return The root folder for the given repository space. Might be <code>null</code>
    *         if no resources were uploaded to that space and <code>create</code> was
    *         <code>false</code>.
    */
   Folder getContentFolder(RepositorySpaceKey space);

   /**
    * Returns the root folder for the current partition's space in the repository.
    * 
    * This folder might be used to directly access content with the
    * {@link DocumentManagementService}.
    * 
    * @param create
    *           <code>true</code> if the folder structure should be created if not already
    *           existing.
    * @return The root folder for the given repository space. Might be <code>null</code>
    *         if no resources were uploaded to that space and <code>create</code> was
    *         <code>false</code>.
    */
   Folder getContentFolder(RepositorySpaceKey space, boolean create);
   
   /**
    * Flushes all caches (folder, document and document content caches).
    */
   void resetCaches();

   interface Factory
   {
      IRepositoryManager getRepositoryManager();
   }

}
