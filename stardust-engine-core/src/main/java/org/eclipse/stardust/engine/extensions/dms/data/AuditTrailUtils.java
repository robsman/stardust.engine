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
import java.util.*;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.Period;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.beans.BigData;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataValueFactory;

import com.sungard.infinity.bpm.vfs.*;


/**
 * @author rsauer
 * @version $Revision$
 */
public class AuditTrailUtils
{

   public static final String DOCS_DOCUMENTS = "documents";

   public static final String FOLDERS_FOLDERS = "folders";

   public static final String RES_REPOSITORY_ID = "repositoryId";

   public static final String RES_ID = "id";

   public static final String RES_PATH = "path";

   public static final String RES_NAME = "name";

   public static final String RES_DESCRIPTION = "description";

   public static final String RES_OWNER = "owner";

   public static final String RES_DATE_CREATED = "dateCreated";

   public static final String RES_DATE_LAST_MODIFIED = "dateLastModified";

   public static final String RES_PROPERTIES = "properties";

   public static final String FILE_SIZE = "size";

   public static final String FILE_CONTENT_TYPE = "contentType";

   public static final String FILE_REVISION_ID = "revisionId";

   public static final String FILE_REVISION_NAME = "revisionName";

   public static final String FILE_VERSION_LABELS = "versionLabels";

   public static final String PRP_NAME = "name";

   public static final String PRP_STRING_VALUE = "stringValue";

   public static final String PRP_TYPE_KEY = "typeKey";

   public static final String FOLDER_DOCUMENT_COUNT = "documentCount";

   public static final String FOLDER_FOLDER_COUNT = "folderCount";

   public static final String FOLDER_SUB_FOLDERS = "subFolders";

   public static final String VERSIONING_CREATE_REVISION = "createRevision";

   public static final String VERSIONING_VERSION_LABEL = "versionLabel";

   public static final String FOLDER_DOCUMENTS = "documents";

   public static final String FILE_LOCK_OWNER = "lockOwner";

   public static final String FILE_ENCODING = "encoding";

   public static final String RES_PARENT_ID = "parentId";

   public static final String RES_PARENT_PATH = "parentPath";

   public static final String FILE_ANNOTATIONS = "documentAnnotations";

   // DocumentType mapping
   public static final String DOC_DOCUMENT_TYPE_MAP = "documentType";

   public static final String DOC_DOCUMENT_TYPE_ID = "documentTypeId";

   public static final String DOC_DOCUMENT_TYPE_SCHEMA_LOCATION = "schemaLocation";

   public static IFileInfo toFileInfo(Map auditTrailDoc)
   {
      final String name = (String) auditTrailDoc.get(RES_NAME);
      if (StringUtils.isEmpty(name))
      {
         throw new PublicException(BpmRuntimeError.DMS_EMPTY_FILE_NAME.raise());
      }

      IFileInfo docInfo = VfsUtils.createFileInfo(name);

      VfsMediator.updateVfsFile(docInfo, auditTrailDoc);

      return docInfo;
   }

   public static IFolderInfo toFolderInfo(Map auditTrailFolder)
   {
      final String name = (String) auditTrailFolder.get(RES_NAME);
      if (StringUtils.isEmpty(name))
      {
         throw new PublicException(BpmRuntimeError.DMS_EMPTY_FOLDER_NAME.raise());
      }

      IFolderInfo folderInfo = VfsUtils.createFolderInfo(name);

      VfsMediator.updateVfsFolder(folderInfo, auditTrailFolder);

      return folderInfo;
   }

   public static boolean updateFileFromVfs(Map auditTrailDoc, IFile vfsDoc)
   {
      return updateFileFromVfs(auditTrailDoc, vfsDoc, null);
   }

   public static boolean updateFileFromVfs(Map auditTrailDoc, IFile vfsDoc,
         String prefixPath)
   {
      boolean modified = updateResourceFromVfs(auditTrailDoc, vfsDoc, prefixPath);

      modified |= updateEntry(auditTrailDoc, FILE_CONTENT_TYPE, vfsDoc.getContentType());

      modified |= updateEntry(auditTrailDoc, FILE_SIZE, new Long(vfsDoc.getSize()));

      modified |= updateEntry(auditTrailDoc, FILE_REVISION_ID, vfsDoc.getRevisionId());
      modified |= updateEntry(auditTrailDoc, FILE_REVISION_NAME, vfsDoc.getRevisionName());
      modified |= updateEntry(auditTrailDoc, FILE_VERSION_LABELS, new ArrayList(
            vfsDoc.getVersionLabels()));

      Map /* <String,Serializable> */vfsAnnotationsMap = CollectionUtils.copyMap(vfsDoc.getAnnotations());
      Object object = auditTrailDoc.get(FILE_ANNOTATIONS);
      if (object instanceof Map || object == null)
      {
         Map /* <String,Serializable> */auditTrailProperties = (Map) object;
         modified |= !vfsAnnotationsMap.equals(auditTrailProperties);
         auditTrailDoc.put(FILE_ANNOTATIONS, vfsAnnotationsMap);
      }
      return modified;
   }

   public static boolean updateFolderFromVfs(Map auditTrailFolder, IFolder vfsFolder)
   {
      return updateFolderFromVfs(auditTrailFolder, vfsFolder, null);
   }

   public static boolean updateFolderFromVfs(Map auditTrailFolder, IFolder vfsFolder,
         String prefixPath)
   {
      boolean modified = updateResourceFromVfs(auditTrailFolder, vfsFolder, prefixPath);

      modified |= updateEntry(auditTrailFolder, FOLDER_DOCUMENT_COUNT, Integer.valueOf(
            vfsFolder.getFileCount()));
      modified |= updateEntry(auditTrailFolder, FOLDER_FOLDER_COUNT, Integer.valueOf(
            vfsFolder.getFolderCount()));

      if (vfsFolder.getLevelOfDetail() != IFolder.LOD_NO_MEMBERS)
      {
         // update files
         List auditTrailDocuments = CollectionUtils.newLinkedList();
         auditTrailFolder.put(FOLDER_DOCUMENTS, auditTrailDocuments);
         for (Iterator i = vfsFolder.getFiles().iterator(); i.hasNext();)
         {
            IFile file = (IFile) i.next();
            Map auditTrailDoc = CollectionUtils.newHashMap();
            auditTrailDocuments.add(auditTrailDoc);
            modified |= updateFileFromVfs(auditTrailDoc, file, prefixPath);
         }

         // update subfolders
         modified |= updateSubFoldersFromVfs(auditTrailFolder, vfsFolder, prefixPath);
      }
      return modified;
   }

   private static boolean updateSubFoldersFromVfs(Map auditTrailFolder,
         IFolder vfsFolder, String prefixPath)
   {
      boolean modified = false;

      List /* <Map> */auditTrailSubFolders = (List) auditTrailFolder.get(FOLDER_SUB_FOLDERS);
      List /* <IFolder> */vfsSubFolders = vfsFolder.getFolders();

      if (isSameLength(auditTrailSubFolders, vfsSubFolders))
      {
         if (vfsSubFolders.size() > 0)
         {
            // same lengths, try update and check if every one was modified
            for (Iterator a = auditTrailSubFolders.iterator(), v = vfsSubFolders.iterator(); v.hasNext();)
            {
               modified = updateFolderFromVfs((Map) a.next(), (IFolder) v.next(),
                     prefixPath);
            }
         }
      }
      else
      {
         // different lengths -> modified
         modified = true;

         // overwrite the audit trail list
         auditTrailSubFolders = CollectionUtils.newArrayList();
         auditTrailFolder.put(FOLDER_SUB_FOLDERS, auditTrailSubFolders);

         for (Iterator v = vfsSubFolders.iterator(); v.hasNext();)
         {
            Map auditTrailSubFolder = CollectionUtils.newHashMap();
            updateFolderFromVfs(auditTrailSubFolder, (IFolder) v.next(), prefixPath);
            auditTrailSubFolders.add(auditTrailSubFolder);
         }
      }

      return modified;
   }

   private static boolean isSameLength(List auditTrailSubFolders, List vfsSubFolders)
   {
      if ((auditTrailSubFolders == null || auditTrailSubFolders.size() == 0)
            && (vfsSubFolders == null || vfsSubFolders.size() == 0))
      {
         return true;
      }

      if (auditTrailSubFolders != null && vfsSubFolders != null
            && auditTrailSubFolders.size() == vfsSubFolders.size())
      {
         return true;
      }

      // otherwise
      return false;
   }

   public static boolean updateResourceFromVfs(Map auditTrailRes, IResource vfsRes,
         String prefixPath)
   {
      boolean modified = false;

      // CRNT-15242 remove prefix from path of vfsResource
      String path = vfsRes.getPath();
      if ( !StringUtils.isEmpty(prefixPath))
      {
         if(prefixPath.equals(path))
         {
            path = "/";
         }
         else if (path.startsWith(prefixPath))
         {
            path = path.substring(prefixPath.length());
         }
      }
      // update attributes
      // CRNT-8785 (parentId and parentPath in jcr-vfs throw exception)
      // modified |= updateEntry(auditTrailRes, RES_PARENT_ID, vfsRes.getParentId());
      // modified |= updateEntry(auditTrailRes, RES_PARENT_PATH, vfsRes.getParentPath());
      modified |= updateEntry(auditTrailRes, RES_REPOSITORY_ID, vfsRes.getRepositoryId());
      modified |= updateEntry(auditTrailRes, RES_ID, vfsRes.getId());
      modified |= updateEntry(auditTrailRes, RES_PATH, path);
      modified |= updateEntry(auditTrailRes, RES_NAME, vfsRes.getName());
      modified |= updateEntry(auditTrailRes, RES_DESCRIPTION, vfsRes.getDescription());
      modified |= updateEntry(auditTrailRes, RES_OWNER, vfsRes.getOwner());
      modified |= updateEntry(auditTrailRes, RES_DATE_CREATED, vfsRes.getDateCreated());
      modified |= updateEntry(auditTrailRes, RES_DATE_LAST_MODIFIED,
            vfsRes.getDateLastModified());

      Map /* <String,Serializable> */vfsPropertiesMap = CollectionUtils.copyMap(vfsRes.getProperties());
      Map /* <String,Serializable> */auditTrailProperties = (Map) auditTrailRes.get(RES_PROPERTIES);
      modified |= !vfsPropertiesMap.equals(auditTrailProperties);
      auditTrailRes.put(RES_PROPERTIES, vfsPropertiesMap);

      // DocumentType mapping
      Map<String, Serializable> docType = new HashMap<String, Serializable>(4);
      docType.put(DOC_DOCUMENT_TYPE_ID, vfsRes.getPropertiesTypeId());
      docType.put(DOC_DOCUMENT_TYPE_SCHEMA_LOCATION, vfsRes.getPropertiesTypeSchemaLocation());

      Object rawDocType = auditTrailRes.get(DOC_DOCUMENT_TYPE_MAP);
      if (rawDocType instanceof Map)
      {
         if ( !CompareHelper.areEqual(rawDocType, docType))
         {
            auditTrailRes.put(DOC_DOCUMENT_TYPE_MAP, docType);
            modified = true;
         }
      }
      else if (rawDocType == null)
      {
         if (vfsRes.getPropertiesTypeId() != null
               || vfsRes.getPropertiesTypeSchemaLocation() != null)
         {
            auditTrailRes.put(DOC_DOCUMENT_TYPE_MAP, docType);
            modified = true;
         }
      }

      return modified;
   }

   private static boolean updateEntry(Map targetStruct, String propertyName, Object value)
   {
      boolean modified = false;

      if ( !CompareHelper.areEqual(targetStruct.get(propertyName), value))
      {
         modified = true;

         targetStruct.put(propertyName, value);
      }

      return modified;
   }

   public static int guessTypeKey(Serializable propertyValue)
   {
      if (propertyValue == null)
      {
         return BigData.NULL;
      }
      else if (propertyValue instanceof String)
      {
         return BigData.STRING;
      }
      else if (propertyValue instanceof Boolean)
      {
         return BigData.BOOLEAN;
      }
      else if (propertyValue instanceof Integer)
      {
         return BigData.INTEGER;
      }
      else if (propertyValue instanceof Long)
      {
         return BigData.LONG;
      }
      else if (propertyValue instanceof Short)
      {
         return BigData.SHORT;
      }
      else if (propertyValue instanceof Byte)
      {
         return BigData.BYTE;
      }
      else if (propertyValue instanceof Double)
      {
         return BigData.DOUBLE;
      }
      else if (propertyValue instanceof Float)
      {
         return BigData.FLOAT;
      }
      else if (propertyValue instanceof Date)
      {
         return BigData.DATE;
      }
      else if (propertyValue instanceof Period)
      {
         return BigData.PERIOD;
      }
      else
      {
         return BigData.NULL;
      }
   }

   public static Map convertToPropertyMap(List propertyList)
   {
      Map propertyMap = CollectionUtils.newHashMap();
      if (propertyList != null && propertyList.size() > 0)
      {
         for (Iterator i = propertyList.iterator(); i.hasNext();)
         {
            Map property = (Map) i.next();
            Integer typeKey = (Integer) property.get(AuditTrailUtils.PRP_TYPE_KEY);
            String propertyName = (String) property.get(AuditTrailUtils.PRP_NAME);
            String stringValue = (String) property.get(AuditTrailUtils.PRP_STRING_VALUE);
            Serializable propertyValue = (Serializable) StructuredDataValueFactory.convertTo(
                  typeKey.intValue(), stringValue);
            propertyMap.put(propertyName, propertyValue);
         }
      }
      return propertyMap;
   }

   public static List convertToPropertyList(Map propertyMap)
   {
      List propertyList = CollectionUtils.newArrayList();
      if (propertyMap != null && propertyMap.size() > 0)
      {
         for (Iterator i = propertyMap.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry e = (Map.Entry) i.next();
            String propertyName = (String) e.getKey();
            Serializable propertyValue = (Serializable) e.getValue();
            int typeKey = AuditTrailUtils.guessTypeKey(propertyValue);
            if (typeKey != BigData.NULL)
            {
               // can only convert primitives to properties
               Map genericProperty = CollectionUtils.newHashMap();
               genericProperty.put(AuditTrailUtils.PRP_NAME, propertyName);
               // use null as xsdTypeName since it is unknown when default metadata schema
               // is used
               String stringValue = StructuredDataValueFactory.convertToString(typeKey,
                     null, propertyValue);
               genericProperty.put(AuditTrailUtils.PRP_STRING_VALUE, stringValue);
               genericProperty.put(AuditTrailUtils.PRP_TYPE_KEY, new Integer(typeKey));
               propertyList.add(genericProperty);
            }
         }
      }
      return propertyList;
   }

   private AuditTrailUtils()
   {
      // utility class
   }

   public static Map unwrap(Object wrappedValue, DataDetails data)
   {
      String dataTypeId = data.getTypeId();
      // convert Folder, Document, List<Folder>, List<Document> to Map
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
      {
         if (wrappedValue == null)
         {
            wrappedValue = new DmsDocumentBean();
         }
         Map legoDocument = ((DmsResourceBean) wrappedValue).vfsResource();
         String excludeXPath = null;
         if ( !hasDefaultMetadataSchema(data))
         {
            excludeXPath = AuditTrailUtils.RES_PROPERTIES;
         }
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
               DmsPropertyFormatter.AS_LIST, excludeXPath);
         propertyFormatter.visit(legoDocument, "");
         return legoDocument;
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId))
      {
         if (wrappedValue == null)
         {
            wrappedValue = new DmsFolderBean();
         }
         Map legoFolder = ((DmsResourceBean) wrappedValue).vfsResource();
         String excludeXPath = null;
         if ( !hasDefaultMetadataSchema(data))
         {
            excludeXPath = AuditTrailUtils.RES_PROPERTIES;
         }
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
               DmsPropertyFormatter.AS_LIST, excludeXPath);
         propertyFormatter.visit(legoFolder, "");

         return legoFolder;
      }
      else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
      {
         if (wrappedValue == null)
         {
            wrappedValue = CollectionUtils.newList();
         }
         Map documentList = CollectionUtils.newHashMap();
         documentList.put(AuditTrailUtils.DOCS_DOCUMENTS, convertDmsResourcesToMaps(
               (List) wrappedValue, data));
         return documentList;
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId))
      {
         if (wrappedValue == null)
         {
            wrappedValue = CollectionUtils.newList();
         }
         Map folderList = CollectionUtils.newHashMap();
         folderList.put(AuditTrailUtils.FOLDERS_FOLDERS, convertDmsResourcesToMaps(
               (List) wrappedValue, data));
         return folderList;
      }
      else if (StructuredDataConstants.STRUCTURED_DATA.equals(dataTypeId))
      {
         // the value of a structured data mapping without data path
         // is always a Map and should not be wrapped/unwrapped
         return (Map) wrappedValue;
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.MDL_UNKNOWN_DATA_TYPE_ID.raise(dataTypeId));
      }
   }

   public static boolean hasDefaultMetadataSchema(AccessPoint accessPoint)
   {
      String metadataComplexTypeName = (String) accessPoint.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
      if (StringUtils.isEmpty(metadataComplexTypeName))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   static boolean hasDefaultMetadataSchema(Data data)
   {
      String metadataComplexTypeName = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
      if (StringUtils.isEmpty(metadataComplexTypeName))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   private static List convertDmsResourcesToMaps(
         List /* <DmsResourceBean> */dmsResourceList, DataDetails data)
   {
      List legoResourceList = CollectionUtils.newList();

      if (dmsResourceList != null)
      {
         for (Iterator i = dmsResourceList.iterator(); i.hasNext();)
         {
            DmsResourceBean dmsResource = (DmsResourceBean) i.next();

            Map legoResource = dmsResource.vfsResource();
            String excludeXPath = null;
            if ( !hasDefaultMetadataSchema(data))
            {
               excludeXPath = AuditTrailUtils.RES_PROPERTIES;
            }
            DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
                  DmsPropertyFormatter.AS_LIST, excludeXPath);
            propertyFormatter.visit(legoResource, "");
            legoResourceList.add(legoResource);
         }
      }
      return legoResourceList;
   }

   private static List /* <DmsFolderBean> */convertMapsToFolderList(List legoFolderList,
         Data data)
   {
      List dmsFolderBeans = CollectionUtils.newList();

      if (legoFolderList != null)
      {
         for (Iterator i = legoFolderList.iterator(); i.hasNext();)
         {
            Map legoFolder = (Map) i.next();
            DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
                  DmsPropertyFormatter.AS_MAP, null);
            propertyFormatter.visit(legoFolder, "");
            dmsFolderBeans.add(new DmsFolderBean(legoFolder));
         }
      }
      return dmsFolderBeans;
   }

   private static List /* <DmsDocumentBean> */convertMapsToDocumentList(
         List legoDocumentList, Data data)
   {
      List dmsDocumentBeans = CollectionUtils.newList();

      if (legoDocumentList != null)
      {
         for (Iterator i = legoDocumentList.iterator(); i.hasNext();)
         {
            Map legoDocument = (Map) i.next();
            DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
                  DmsPropertyFormatter.AS_MAP, null);
            propertyFormatter.visit(legoDocument, "");
            dmsDocumentBeans.add(new DmsDocumentBean(legoDocument));
         }
      }
      return dmsDocumentBeans;
   }

   public static Object wrap(Object value, DataDetails data)
   {
      if ( !(value instanceof Map) || value == null)
      {
         return value;
      }
      String dataTypeId = data.getTypeId();
      if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
      {
         Map legoDocument = (Map) value;
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
               DmsPropertyFormatter.AS_MAP, null);
         propertyFormatter.visit(legoDocument, "");
         return new DmsDocumentBean(legoDocument);
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId))
      {
         Map legoFolder = (Map) value;
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(
               DmsPropertyFormatter.AS_MAP, null);
         propertyFormatter.visit(legoFolder, "");
         return new DmsFolderBean(legoFolder);
      }
      else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
      {
         return convertMapsToDocumentList(
               (List) ((Map) value).get(AuditTrailUtils.DOCS_DOCUMENTS), data);
      }
      else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId))
      {
         return convertMapsToFolderList(
               (List) ((Map) value).get(AuditTrailUtils.FOLDERS_FOLDERS), data);
      }
      else if (StructuredDataConstants.STRUCTURED_DATA.equals(dataTypeId))
      {
         // the value of a structured data mapping without data path
         // is always a Map and should not be wrapped/unwrapped
         return value;
      }
      else
      {
         throw new PublicException(
               BpmRuntimeError.MDL_UNKNOWN_DATA_TYPE_ID.raise(dataTypeId));
      }

   }
}
