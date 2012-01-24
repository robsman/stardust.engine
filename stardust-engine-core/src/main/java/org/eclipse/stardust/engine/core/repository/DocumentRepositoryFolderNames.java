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

/**
 * @author sauer
 * @version $Revision: $
 */
public class DocumentRepositoryFolderNames
{
   public static final String REPOSITORY_ROOT_FOLDER = "/ipp-repository/";
   
   public static final String SYSTEM_FOLDER =  "system/";
   
   public static final String PARTITIONS_FOLDER =  "partitions/";
   
   public static final String REALMS_FOLDER =  "realms/";
   
   public static final String USERS_FOLDER =  "users/";
   
   public static final String PREFS_FOLDER =  "preferences/";
   
   public static final String ARTIFACTS_FOLDER =  "artifacts/";
   
   public static final String SKINS_FOLDER =  "skins/";
   
   public static final String CONTENT_FOLDER =  "content/";
   
   public static final String BUNDLES_FOLDER =  "bundles/";
   
   public static final String PROCESS_ATTACHMENT_FOLDER = "process-instances/";
   
   public static String getRepositoryRootFolder()
   {
      // TODO must this be configurable?
      
      return REPOSITORY_ROOT_FOLDER;
   }
}
