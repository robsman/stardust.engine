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

import java.util.Stack;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;


/**
 * @author sauer
 * @version $Revision: $
 */
public class DocumentRepositoryUtils
{

   public static Folder getSubFolder(DocumentManagementService docService,
         String folderPath)
   {
      Stack missingFolders = new Stack();

      Folder folder = docService.getFolder(folderPath);
      while (null == folder && !"/".equals(folderPath))
      {
         missingFolders.push(folderPath.substring(folderPath.lastIndexOf("/") + 1));
         
         folderPath = folderPath.substring(0, folderPath.lastIndexOf("/"));
         if (StringUtils.isEmpty(folderPath))
         {
            folderPath = "/";
         }
         else
         {
            folder = docService.getFolder(folderPath);
         }
      }
      
      while ( !missingFolders.isEmpty())
      {
         String folderName = (String) missingFolders.pop();
         
         folder = docService.createFolder(
               (null != folder) ? folder.getId() : folderPath,
               DmsUtils.createFolderInfo(folderName));
      }

      return folder;
   }
   
}
