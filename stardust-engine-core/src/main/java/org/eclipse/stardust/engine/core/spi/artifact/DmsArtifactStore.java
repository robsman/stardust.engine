/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Roland.Stamm (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.spi.artifact;

import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentInfo;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.RuntimeArtifactBean;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;

public class DmsArtifactStore implements IArtifactStore
{
   protected static final String BASE_PATH = "/artifacts/runtime/";

   public String storeContent(RuntimeArtifactBean runtimeArtifactBean, byte[] content, String contentType)
   {
      String referenceId;
      // create DMS Document
      DocumentManagementService dms = getDms();

      String folderPath = getStoragePath(runtimeArtifactBean);

      // ensure folder exists
      DmsUtils.ensureFolderHierarchyExists(folderPath, dms);

      Document document = dms.getDocument(folderPath + "/"
            + runtimeArtifactBean.getArtifactId());
      if (document == null)
      {
         DocumentInfo documentInfo = DmsUtils.createDocumentInfo(runtimeArtifactBean.getArtifactId());
         documentInfo.setContentType(contentType);
         Document createdDocument = dms.createDocument(folderPath, documentInfo, content,
               null);
         referenceId = createdDocument.getId();
      }
      else
      {
         document.setContentType(contentType);
         Document updatedDocument = dms.updateDocument(document, content, null, false,
               null, null, false);
         referenceId = updatedDocument.getId();
      }
      return referenceId;
   }

   public byte[] retrieveContent(String referenceId)
   {
      DocumentManagementService dms = getDms();
      byte[] content = dms.retrieveDocumentContent(referenceId);
      return content;
   }

   @Override
   public void removeContent(String referenceId)
   {
      DocumentManagementService dms = getDms();
      dms.removeDocument(referenceId);
   }

   private String getStoragePath(RuntimeArtifactBean runtimeArtifactBean)
   {
      String path = BASE_PATH + runtimeArtifactBean.getArtifactTypeId() + "/"
            + runtimeArtifactBean.getOID();
      return RepositoryIdUtils.addRepositoryId(path,
            RepositoryManager.SYSTEM_REPOSITORY_ID);
   }

   private DocumentManagementService getDms()
   {
      return new DocumentManagementServiceImpl();
   }

}
