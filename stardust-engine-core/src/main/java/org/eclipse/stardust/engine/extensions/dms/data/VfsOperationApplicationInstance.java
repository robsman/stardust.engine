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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Application;
import org.eclipse.stardust.engine.api.model.Modules;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.DmsUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.FolderInfo;
import org.eclipse.stardust.engine.core.extensions.ExtensionService;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentManagementServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryConstants;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SynchronousApplicationInstance;


/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsOperationApplicationInstance
      implements SynchronousApplicationInstance
{

   private DmsOperation operation;

   private DocumentManagementService dms;

   private String dmsId;

   private boolean useRuntimeDefinedTargetFolder;

   private Map /* <String,Map> */args;

   private String defaultPath;

   public void bootstrap(ActivityInstance activityInstance)
   {
      ExtensionService.initializeModuleExtensions(Modules.DMS);

      Activity activity = (Activity) activityInstance.getActivity();

      IProcessInstance processInstance = ProcessInstanceBean.findByOID(activityInstance.getProcessInstanceOID());

      this.defaultPath = DmsUtils.composeDefaultPath(
            processInstance.getScopeProcessInstanceOID(),
            processInstance.getScopeProcessInstance().getStartTime());

      Application application = activity.getApplication();

      this.operation = DmsOperation.fromId((String) application.getAttribute(DmsConstants.PRP_OPERATION_NAME));

      this.dmsId = (String) application.getAttribute(DmsConstants.PRP_OPERATION_DMS_ID);

      this.useRuntimeDefinedTargetFolder = Boolean.TRUE.equals(application.getAttribute(DmsConstants.PRP_RUNTIME_DEFINED_TARGET_FOLDER));

      this.args = CollectionUtils.newMap();

      this.dms = new DocumentManagementServiceImpl();
   }

   public void cleanup()
   {
      this.operation = null;
      this.dmsId = null;
      this.useRuntimeDefinedTargetFolder = false;
      this.args = null;
      this.dms = null;
   }

   public void setInAccessPointValue(String name, Object value)
   {
      if (value instanceof Document || value instanceof Folder)
      {
         // Lego structures are expected in invoke(), retrieve it in case of DMS objects
         Map auditTrailDoc = ((DmsResourceBean) value).vfsResource();
         if (auditTrailDoc != null)
         {
            args.put(name, auditTrailDoc);
         }
      }
      else
      {
         args.put(name, value);

         if (VfsOperationAccessPointProvider.AP_ID_DMS_ID.equals(name)
               && (value instanceof String))
         {
            this.dmsId = (String) value;
         }
      }
   }

   public Object getOutAccessPointValue(String name)
   {
      Object value;

      if (VfsOperationAccessPointProvider.AP_ID_DMS_ID.equals(name))
      {
         // dmsid
         value = dmsId;
      }
      else if (VfsOperationAccessPointProvider.AP_ID_DOCUMENT_IDS.equals(name)
            || VfsOperationAccessPointProvider.AP_ID_FOLDER_IDS.equals(name))
      {
         // Lists
         value = args.get(name);
         if (value == null)
         {
            value = CollectionUtils.newLinkedList();
            args.put(name, value);
         }
      }
      else
      {
         // others are Maps
         // it is safe to return the Lego variant as contrary to the name this will be
         // invoked for in mappings having a non-empty deref path
         value = args.get(name);
         if (value == null)
         {
            value = CollectionUtils.newHashMap();
            args.put(name, value);
         }
      }

      return value;
   }

   private Object getMandatoryArgument(String argumentName)
   {
      Object argumentValue = args.get(argumentName);
      if (argumentValue == null)
      {
         throw new PublicException(
               BpmRuntimeError.DMS_NO_VALUE_FOR_MANDATORY_IN_ACCESS_POINT_SUPPLIED
                     .raise(argumentName));
      }
      else
      {
         return argumentValue;
      }
   }

   private Object getOptionalArgument(String argumentName)
   {
      return args.get(argumentName);
   }

   public Map invoke(Set outDataTypes) throws InvocationTargetException
   {
      try
      {
         if (dms == null)
         {
            throw new PublicException(
                  BpmRuntimeError.DMS_NO_DUCUMENTMANAGEMENTSERVICE_AVAILABLE.raise());
         }

         Map result = CollectionUtils.newMap();

         if (DmsOperation.OP_CREATE_FOLDER == operation)
         {
            String parentFolderId;
            if (this.useRuntimeDefinedTargetFolder)
            {
               Map legoParentFolder = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_TARGET_FOLDER);
               parentFolderId = (String) legoParentFolder.get(AuditTrailUtils.RES_ID);
            }
            else
            {
               // take default folder when no parent folder is specified
               parentFolderId = getDefaultFolder().getId();
            }

            Map legoNewFolder = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER);
            FolderInfo newFolderInfo = DmsUtils.createFolderInfo((String) legoNewFolder.get(AuditTrailUtils.RES_NAME));

            Folder newFolder = dms.createFolder(parentFolderId, newFolderInfo);

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_FOLDER))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_FOLDER, newFolder);
            }
         }
         else if (DmsOperation.OP_FIND_FOLDERS == operation)
         {
            List<Folder> folders;
            String expression = (String) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_EXPRESSION);
            if (StringUtils.isEmpty(expression))
            {
               // should search using name pattern
               Map legoFolderSearchCriteria = (Map) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER_INFO);
               if (legoFolderSearchCriteria == null)
               {
                  throw new InternalException(
                        "Neither expression nor folder with namePattern is specified for the folder search.");
               }
               String namePattern = (String) legoFolderSearchCriteria.get(AuditTrailUtils.RES_NAME);
               if (StringUtils.isEmpty(namePattern))
               {
                  throw new InternalException(
                        "Neither expression nor folder with namePattern is specified for the folder search.");
               }
               folders = dms.findFoldersByName(namePattern, Folder.LOD_LIST_MEMBERS);
            }
            else
            {
               // should search using expression
               folders = dms.findFolders(expression, Folder.LOD_LIST_MEMBERS);
            }

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_FOLDER_LIST))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_FOLDER_LIST, folders);
            }

         }
         else if (DmsOperation.OP_REMOVE_FOLDER == operation)
         {
            Map legoFolder = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER);
            Boolean recursive = (Boolean) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_RECURSIVE);
            String folderPath = (String) legoFolder.get(AuditTrailUtils.RES_PATH);
            dms.removeFolder(RepositoryIdUtils.addRepositoryId(folderPath, this.dmsId), (recursive == null)
                  ? false
                  : recursive.booleanValue());
         }
         else if (DmsOperation.OP_ADD_DOCUMENT == operation)
         {
            Folder parentFolder;
            if (this.useRuntimeDefinedTargetFolder)
            {
               Map legoParentFolder = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_TARGET_FOLDER);
               parentFolder = getFolder(legoParentFolder);
            }
            else
            {
               // take default folder when no parent folder is specified
               parentFolder = getDefaultFolder();
            }

            Map legoDocumentInfo = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT);

            Document newFile = dms.createDocument(parentFolder.getId(),
                  new DmsDocumentBean(legoDocumentInfo));

            if (shouldVersion())
            {
               // version the newly created document
               String revisionComment = getVersionLabel();
               newFile = dms.versionDocument(newFile.getId(), null, revisionComment);
            }

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT, newFile);
            }
         }
         else if (DmsOperation.OP_FIND_DOCUMENTS == operation)
         {
            List<Document> documents;
            String expression = (String) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_EXPRESSION);
            if (StringUtils.isEmpty(expression))
            {
               // should search using name pattern
               Map legoDocumentSearchCriteria = (Map) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_INFO);
               if (legoDocumentSearchCriteria == null)
               {
                  throw new InternalException(
                        "Neither expression nor document with namePattern is specified for the document search.");
               }
               String namePattern = (String) legoDocumentSearchCriteria.get(AuditTrailUtils.RES_NAME);
               if (StringUtils.isEmpty(namePattern))
               {
                  throw new InternalException(
                        "Neither expression nor document with namePattern is specified for the document search.");
               }
               documents = dms.findDocumentsByName(namePattern);
            }
            else
            {
               // should search using expression
               documents = dms.findDocuments(expression);
            }

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_LIST))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_LIST, documents);
            }
         }
         else if (DmsOperation.OP_UPDATE_DOCUMENT == operation)
         {
            Map legoDocument = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT);

            Document document = new DmsDocumentBean(legoDocument);
            document = dms.updateDocument(document, shouldVersion(), null, getVersionLabel(), false);

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT,
                     document);
            }
         }
         else if (DmsOperation.OP_UPDATE_FOLDER == operation)
         {
            Map legoFolder = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER);

            Folder folder = new DmsFolderBean(legoFolder);
            folder = dms.updateFolder(folder);

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_FOLDER))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_FOLDER,
                     folder);
            }
         }
         else if (DmsOperation.OP_REMOVE_DOCUMENT == operation)
         {
            Map legoDocument = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT);
            String documentPath = (String) legoDocument.get(AuditTrailUtils.RES_PATH);
            dms.removeDocument(RepositoryIdUtils.addRepositoryId(documentPath, this.dmsId));
         }
         else if (DmsOperation.OP_GET_DOCUMENTS == operation)
         {
            List<String> ids = (List) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_IDS);
            List<Document> documents = dms.getDocuments(ids);
            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_LIST))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_LIST, documents);
            }
         }
         else if (DmsOperation.OP_GET_FOLDERS == operation)
         {
            List<String> ids = (List) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER_IDS);
            List<Folder> folders = dms.getFolders(ids, Folder.LOD_LIST_MEMBERS);
            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_FOLDER_LIST))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_FOLDER_LIST, folders);
            }
         }
         else if (DmsOperation.OP_GET_DOCUMENT == operation)
         {
            String id = (String) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT_ID);
            Document document = dms.getDocument(id);
            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT)
                  && document != null)
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT, document);
            }
         }
         else if (DmsOperation.OP_GET_FOLDER == operation)
         {
            String id = (String) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_FOLDER_ID);
            Folder folder = dms.getFolder(id, Folder.LOD_LIST_MEMBERS);
            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_FOLDER)
                  && folder != null)
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_FOLDER, folder);
            }
         }
         else if (DmsOperation.OP_VERSION_DOCUMENT == operation)
         {
            Map legoDocument = (Map) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_DOCUMENT);
            String versionLabel = (String) getMandatoryArgument(VfsOperationAccessPointProvider.AP_ID_VERSION_LABEL);
            String documentId = (String) legoDocument.get(AuditTrailUtils.RES_ID);
            Document versionedDocument = dms.versionDocument(documentId, null, versionLabel);

            if (outDataTypes.contains(VfsOperationAccessPointProvider.AP_ID_DOCUMENT))
            {
               result.put(VfsOperationAccessPointProvider.AP_ID_DOCUMENT,
                     versionedDocument);
            }
         }
         else
         {
            throw new InternalException("Unsupported operation: " + operation);
         }

         return result;
      }
      catch (Throwable e)
      {
         if (e instanceof InvocationTargetException)
         {
            throw (InvocationTargetException) e;
         }
         throw new InvocationTargetException(e);
      }
   }

   private String getVersionLabel()
   {
      Map versioning = (Map) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_VERSIONING);
      String revisionComment = null;
      if (versioning != null
            && !StringUtils.isEmpty((String) versioning.get(AuditTrailUtils.VERSIONING_VERSION_LABEL)))
      {
         revisionComment = (String) versioning.get(AuditTrailUtils.VERSIONING_VERSION_LABEL);
      }
      return revisionComment;
   }

   private boolean shouldVersion()
   {
      Map versioning = (Map) getOptionalArgument(VfsOperationAccessPointProvider.AP_ID_VERSIONING);
      if (versioning != null
            && Boolean.TRUE.equals(((Boolean) versioning.get(AuditTrailUtils.VERSIONING_CREATE_REVISION))))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   private Folder getDefaultFolder()
   {
      Folder folder = DmsUtils.ensureFolderHierarchyExists(
            RepositoryIdUtils.addRepositoryId(this.defaultPath, this.dmsId), dms);
      return folder;
   }

   private Folder getFolder(Map legoFolderInfo)
   {
      String folderId = (String) legoFolderInfo.get(AuditTrailUtils.RES_ID);
      if (StringUtils.isEmpty(folderId))
      {
         // folder id is not specified, check if the path is specified
         String folderPath = (String) legoFolderInfo.get(AuditTrailUtils.RES_PATH);
         if (StringUtils.isEmpty(folderPath))
         {
            throw new InternalException(
                  "Can not locate target folder, neither folder ID nor path are specified.");
         }
         else
         {
            if ( !folderPath.startsWith(RepositoryConstants.PATH_SEPARATOR))
            {
               folderPath = RepositoryConstants.PATH_SEPARATOR + folderPath;
            }

            Folder targetFolder = dms.getFolder(RepositoryIdUtils.addRepositoryId(folderPath,this.dmsId), Folder.LOD_NO_MEMBERS);

            if (targetFolder == null)
            {
               throw new InternalException(
                     "Can not locate target folder using folder XPath '" + folderPath
                           + "'.");
            }
            else
            {
               return targetFolder;
            }
         }
      }
      else
      {
         Folder targetFolder = dms.getFolder(folderId, Folder.LOD_NO_MEMBERS);

         if (targetFolder == null)
         {
            throw new InternalException("Can not locate target folder using folder ID '"
                  + folderId + "'.");
         }
         else
         {
            return targetFolder;
         }
      }
   }

}
