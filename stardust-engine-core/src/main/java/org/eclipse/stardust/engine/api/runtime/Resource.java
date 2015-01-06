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
package org.eclipse.stardust.engine.api.runtime;

/**
 * The <code>Resource</code> interface keeps information exising  
 * JCR resource (document or folder).
 * 
 * @author sauer
 * @version $Revision$
 */
public interface Resource extends ResourceInfo
{

   /**
    * Indicates the repository this resource is located in.
    * 
    * @return The repository ID.
    */
   String getRepositoryId();

   /**
    * Gets the unique ID of the JCR resource.
    * 
    * @return the unique ID of the JCR resource.
    */
   String getId();

   /**
    * Gets the path to the JCR resource. The path uniqely identifies
    * the JCR resource inside the repository. 
    * 
    * @return the path to the JCR resource.
    */
   String getPath();

   /**
    * Gets the unique ID of the parent of the JCR resource.
    * 
    * @return the unique ID of the parent of the JCR resource.
    */
   // CRNT-8785 (parentId and parentPath in jcr-vfs throw exception)
   //String getParentId();

   /**
    * Gets the path of the parent of the JCR resource.  
    * 
    * @return the path of the parent of the JCR resource.  
    */
   // CRNT-8785 (parentId and parentPath in jcr-vfs throw exception)
   //String getParentPath();

}
