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
package org.eclipse.stardust.engine.extensions.dms.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.core.runtime.beans.BpmRuntimeEnvironment;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;


import org.eclipse.stardust.vfs.IDocumentRepositoryService;
import org.eclipse.stardust.vfs.IFile;
import org.eclipse.stardust.vfs.IFileInfo;
import org.eclipse.stardust.vfs.IFolder;
import org.eclipse.stardust.vfs.IFolderInfo;
import org.eclipse.stardust.vfs.IResourceInfo;

/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsMediator
{

   public boolean writeDocumentToVfs(Map legoDocument, boolean createNewRevision,
         String versionLabel, boolean forceUpdate)
   {
      // TODO support multiple repositories
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService vfs = rtEnv.getDocumentRepositoryService();
      if (vfs == null)
      {
         throw new PublicException(
               "No JCR document repository service is set. Check the configuration.");
      }

      final String docId = (String) legoDocument.get(AuditTrailUtils.RES_ID);

      IFile vfsFile = !StringUtils.isEmpty(docId) ? vfs.getFile(docId) : null;

      if (null != vfsFile)
      {
         // TODO compare revisions
         boolean modified = updateVfsFile(vfsFile, legoDocument);

         if (modified || forceUpdate)
         {
            // locally modified, write change to VFS
            vfsFile = vfs.updateFile(vfsFile, false, false);
            if (createNewRevision)
            {
               vfsFile = vfs.createFileVersion(vfsFile, versionLabel, false);
            }
         }

         return AuditTrailUtils.updateFileFromVfs(legoDocument, vfsFile);
      }
      else
      {
         // TODO

         return false;
      }
   }

   public boolean writeFolderToVfs(Map legoFolder)
   {
      // TODO support multiple repositories
      BpmRuntimeEnvironment rtEnv = PropertyLayerProviderInterceptor.getCurrent();
      IDocumentRepositoryService vfs = rtEnv.getDocumentRepositoryService();
      if (vfs == null)
      {
         throw new PublicException(
               "No JCR document repository service is set. Check the configuration.");
      }

      final String folderId = (String) legoFolder.get(AuditTrailUtils.RES_ID);

      IFolder vfsFolder = !StringUtils.isEmpty(folderId) ? vfs.getFolder(folderId,
            IFolder.LOD_LIST_MEMBERS) : null;

      if (null != vfsFolder)
      {
         // TODO compare revisions
         boolean modified = updateVfsFolder(vfsFolder, legoFolder);

         if (modified)
         {
            // locally modified, write change to VFS
            vfsFolder = vfs.updateFolder(vfsFolder);
         }

         return AuditTrailUtils.updateFolderFromVfs(legoFolder, vfsFolder);
      }
      else
      {
         // TODO

         return false;
      }
   }

   public static boolean updateVfsFile(IFileInfo target, Map source)
   {
      boolean modified = updateVfsResource(target, source);

      if ( !CompareHelper.areEqual(source.get(AuditTrailUtils.FILE_CONTENT_TYPE),
            target.getContentType()))
      {
         modified = true;
         target.setContentType((String) source.get(AuditTrailUtils.FILE_CONTENT_TYPE));
      }

      // DocumentType mapping
      Map<String, Serializable> docType = new HashMap<String, Serializable>(4);
      docType.put(AuditTrailUtils.DOC_DOCUMENT_TYPE_ID, target.getPropertiesTypeId());
      docType.put(AuditTrailUtils.DOC_DOCUMENT_TYPE_SCHEMA_LOCATION,
            target.getPropertiesTypeSchemaLocation());

      Map<String, Serializable> sourceDocType = (Map<String, Serializable>) source.get(AuditTrailUtils.DOC_DOCUMENT_TYPE_MAP);

      // only exposed for Documents right now alias DocumentType
      if ( !CompareHelper.areEqual(sourceDocType, docType))
      {
         modified = true;
         if (sourceDocType != null)
         {
            target.setPropertiesTypeId((String) sourceDocType.get(AuditTrailUtils.DOC_DOCUMENT_TYPE_ID));
            target.setPropertiesTypeSchemaLocation((String) sourceDocType.get(AuditTrailUtils.DOC_DOCUMENT_TYPE_SCHEMA_LOCATION));
         }
         else
         {
            target.setPropertiesTypeId(null);
            target.setPropertiesTypeSchemaLocation(null);
         }
      }

      // merge annotations from source into target
      Object object = source.get(AuditTrailUtils.FILE_ANNOTATIONS);
      if (object instanceof Map || object == null)
      {
         Map srcAnnotations = (Map) object;

         Map<String, Serializable> targetAnnotations = CollectionUtils.copyMap(target.getAnnotations());

         boolean annotationsModified = mergePropertiesMap(targetAnnotations,
               srcAnnotations);
         modified |= annotationsModified;

         if (annotationsModified)
         {
            target.setAnnotations(targetAnnotations);
         }
      }

      return modified;
   }

   public static boolean updateVfsFolder(IFolderInfo target, Map source)
   {
      boolean modified = updateVfsResource(target, source);

      // subfolders and documents are not updated from source since these can only be
      // attached to the folder using Add Document and Create Folder

      return modified;
   }

   public static boolean updateVfsResource(IResourceInfo target, Map source)
   {
      boolean modified = false;

      String resourceName = (String) source.get(AuditTrailUtils.RES_NAME);
      if ( !CompareHelper.areEqual(resourceName, target.getName()))
      {
         modified = true;
         if (StringUtils.isEmpty(resourceName))
         {
            throw new PublicException(
                  "Setting empty name in documents or folders is not possible. Non-empty name must be supplied.");
         }
         target.setName(resourceName);
      }

      if ( !CompareHelper.areEqual(source.get(AuditTrailUtils.RES_DESCRIPTION),
            target.getDescription()))
      {
         modified = true;
         target.setDescription((String) source.get(AuditTrailUtils.RES_DESCRIPTION));
      }

      if ( !CompareHelper.areEqual(source.get(AuditTrailUtils.RES_OWNER),
            target.getOwner()))
      {
         modified = true;
         target.setOwner((String) source.get(AuditTrailUtils.RES_OWNER));
      }

      // merge properties from source into target
      Map srcProperties = (Map) source.get(AuditTrailUtils.RES_PROPERTIES);

      Map<String, Serializable> targetProperties = CollectionUtils.copyMap(target.getProperties());

      boolean propertiesModified = mergePropertiesMap(targetProperties, srcProperties);
      modified |= propertiesModified;

      if (propertiesModified)
      {
         target.setProperties(targetProperties);
      }

      return modified;
   }

   private static boolean mergePropertiesMap(Map<String, Serializable> target,
         Map srcProperties)
   {
      boolean modified = false;
      final Set unmergedProperties = CollectionUtils.copySet(target.keySet());
      if (null != srcProperties)
      {
         for (Iterator i = srcProperties.entrySet().iterator(); i.hasNext();)
         {
            final Map.Entry srcProperty = (Map.Entry) i.next();

            final String propertyName = (String) srcProperty.getKey();

            Serializable value = (Serializable) srcProperty.getValue();

            if ( !CompareHelper.areEqual(value, target.get(propertyName)))
            {
               modified = true;
               target.put(propertyName, value);
            }

            // mark property as merged
            unmergedProperties.remove(propertyName);
         }
      }

      // remove properties not existing in source
      for (Iterator i = unmergedProperties.iterator(); i.hasNext();)
      {
         String name = (String) i.next();

         modified = true;
         target.put(name, null);
      }
      return modified;
   }

}
