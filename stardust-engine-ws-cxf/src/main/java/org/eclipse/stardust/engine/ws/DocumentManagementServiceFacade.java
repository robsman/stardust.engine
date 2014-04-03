/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/*
 * $Id: $
 * (C) 2000 - 2009 CARNOT AG
 */
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.ensureFolderExists;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.extractContentByteArray;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.storeDocumentIntoDms;
import static org.eclipse.stardust.engine.ws.DmsAdapterUtils.updateDocumentFromInfo;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery.DeployedModelState;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.api.ws.*;
import org.eclipse.stardust.engine.api.ws.GetDocuments.DocumentIdsXto;
import org.eclipse.stardust.engine.api.ws.GetFolders.FolderIdsXto;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.utils.XmlUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
@WebService(name = "IDocumentManagementService", serviceName = "StardustBpmServices", portName = "DocumentManagementServiceEndpoint", targetNamespace = "http://eclipse.org/stardust/ws/v2012a/api", endpointInterface = "org.eclipse.stardust.engine.api.ws.IDocumentManagementService")
public class DocumentManagementServiceFacade implements IDocumentManagementService
{
   public DocumentXto createDocument(String folderId, boolean createMissingFolders,
         DocumentInfoXto xto, DataHandler contentHandler,
         DocumentVersionInfoXto versionXto) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      try
      {
         if (createMissingFolders)
         {
            ensureFolderExists(dms, folderId);
         }

         Set<TypedXPath> metaDataXPaths = null;
         if ((null != xto.getMetaData()) && (null != xto.getMetaDataType()))
         {
            metaDataXPaths = inferStructDefinition(xto.getMetaDataType());
         }

         Document doc = storeDocumentIntoDms(dms, folderId, xto, metaDataXPaths,
               contentHandler, versionXto);

         return toWs(doc, xto.getMetaDataType(), metaDataXPaths);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DocumentsXto createDocuments(InputDocumentsXto inputDocuments,
         Boolean createMissingFolders) throws BpmFault
   {
      DocumentsXto docs = new DocumentsXto();
      for (InputDocumentXto att : inputDocuments.getInputDocument())
      {
         // TODO reject null folder?
         try
         {
            DocumentXto doc = createDocument(att.getTargetFolder(),
                  Boolean.TRUE.equals(createMissingFolders), //
                  att.getDocumentInfo(), att.getContent(), att.getVersionInfo());

            docs.getDocument().add(doc);
         }
         catch (ApplicationException e)
         {
            XmlAdapterUtils.handleBPMException(e);
         }

      }

      return docs;
   }

   public DocumentXto getDocument(String documentId, QName metaDataType) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      Document doc = null;
      Set<TypedXPath> metaDataXPaths = null;
      try
      {
         doc = dms.getDocument(documentId);

         if ((null != doc) && !isEmpty(doc.getProperties()))
         {
            if (null == metaDataType && doc.getDocumentType() != null)
            {
               metaDataType = QName.valueOf(doc.getDocumentType().getDocumentTypeId());
            }
            if (null != metaDataType)
            {
               metaDataXPaths = inferStructDefinition(metaDataType);
            }
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return (null != doc) ? toWs(doc, metaDataType, metaDataXPaths) : null;
   }

   public DataHandler getDocumentContent(String documentId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      Document doc = null;
      try
      {
         doc = dms.getDocument(documentId);
         if (null != doc)
         {
            // TODO use streaming API
            DocumentContentDataSource src = new DocumentContentDataSource(doc,
                  dms.retrieveDocumentContent(documentId));

            return new DataHandler(src);
         }
         else
         {
            BpmFaultXto faultInfo = new BpmFaultXto();
            faultInfo.setFaultCode(BpmFaultCodeXto.ITEM_DOES_NOT_EXIST);

            throw new BpmFault(documentId, faultInfo);
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public DocumentsXto getDocumentVersions(String documentId, QName metaDataType)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      List< ? extends Document> documents = null;
      Set<TypedXPath> metaDataXPaths = Collections.emptySet();
      try
      {
         documents = dms.getDocumentVersions(documentId);

         if (null != documents && !documents.isEmpty() && (null != metaDataType))
         {
            metaDataXPaths = inferStructDefinition(metaDataType);
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return toWs(documents, metaDataType, metaDataXPaths);
   }

   @SuppressWarnings("unchecked")
   public DocumentsXto getDocuments(DocumentIdsXto documentIds, QName metaDataType)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      List< ? extends Document> documents = null;
      Set<TypedXPath> metaDataXPaths = Collections.emptySet();
      try
      {
         if (documentIds != null)
         {
            documents = dms.getDocuments(documentIds.getDocumentId());
         }

         if (null != documents && !documents.isEmpty() && (null != metaDataType))
         {
            metaDataXPaths = inferStructDefinition(metaDataType);
         }

      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return toWs(documents, metaDataType, metaDataXPaths);
   }

   @SuppressWarnings("unchecked")
   public DocumentsXto findDocuments(DocumentQueryXto documentQuery) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      List< ? extends Document> documents = Collections.emptyList();
      Set<TypedXPath> metaDataXPaths = Collections.emptySet();
      try
      {
         if (documentQuery.getNamePattern() != null)
         {
            documents = dms.findDocumentsByName(documentQuery.getNamePattern());
         }
         else
         {
            documents = dms.findDocuments(documentQuery.getXpathQuery());
         }

         if (null != documents && !documents.isEmpty()
               && (null != documentQuery.getMetaDataType()))
         {
            metaDataXPaths = inferStructDefinition(documentQuery.getMetaDataType());
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }

      return toWs(documents, documentQuery.getMetaDataType(), metaDataXPaths);
   }

   public String requestDocumentContentDownload(String documentId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         return dms.requestDocumentContentDownload(documentId);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public String requestDocumentContentUpload(String documentId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         return dms.requestDocumentContentUpload(documentId);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DocumentXto versionDocument(String documentId, String versionComment, String versionLabel)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      Document doc = null;
      try
      {
         doc = dms.versionDocument(documentId, versionComment, versionLabel);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return toWs(doc, (QName) null, null);
   }

   public DocumentXto updateDocument(String documentId, DocumentInfoXto xto,
         DataHandler contentHandler, DocumentVersionInfoXto versionXto) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      try
      {
         QName metaDataType = null;
         Set<TypedXPath> metaDataXPaths = null;
         if (xto != null && (null != xto.getMetaData()))
         {
            metaDataType = xto.getMetaDataType();
            if (metaDataType == null && xto.getDocumentType() != null)
            {
               metaDataType = QName.valueOf(xto.getDocumentType().getDocumentTypeId());
            }
            if (null != metaDataType)
            metaDataXPaths = inferStructDefinition(metaDataType);
         }

         DocumentInfo docInfo = fromXto(xto, metaDataXPaths);

         boolean createVersion = false;
         String versionLabel = null;
         String versionComment = null;
         if (null != versionXto)
         {
            createVersion = true;
            versionLabel = versionXto.getLabel();
            versionComment = versionXto.getComment();
         }

         Document doc = dms.getDocument(documentId);
         if (null != doc)
         {
            // merge docInfo into doc
            if (docInfo != null)
            {
               updateDocumentFromInfo(doc, docInfo);
            }

            // TODO use streaming API
            doc = (null != contentHandler) //
                  ? dms.updateDocument(doc, //
                        extractContentByteArray(contentHandler), /* TODO encoding? */
                        null, createVersion, versionComment, versionLabel, false)
                  : dms.updateDocument(doc, createVersion, versionComment, versionLabel, false);

            return toWs(doc, metaDataType, metaDataXPaths);
         }
         else
         {
            BpmFaultXto faultInfo = new BpmFaultXto();
            faultInfo.setFaultCode(BpmFaultCodeXto.ITEM_DOES_NOT_EXIST);

            throw new BpmFault(documentId, faultInfo);
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public void removeDocument(String documentId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         dms.removeDocument(documentId);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public FolderXto getFolder(String folderId, FolderLevelOfDetailXto folderLOD,
         QName documentMetaDataType, QName folderMetaDataType) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      Folder folder = null;
      try
      {
         folder = dms.getFolder(folderId, umarshalFolderLOD(folderLOD));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return (null != folder) ? toWs(folder, wsEnv.getActiveModel(),
            documentMetaDataType, folderMetaDataType) : null;
   }

   @SuppressWarnings("unchecked")
   public FoldersXto getFolders(FolderIdsXto folderIds,
         FolderLevelOfDetailXto folderLevelOfDetail, QName documentMetaDataType,
         QName folderMetaDataType) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      try
      {
         if (folderIds != null)
         {
            List<Folder> folders = dms.getFolders(folderIds.getFolderId(),
                  umarshalFolderLOD(folderLevelOfDetail));

            if (folders != null && !folders.isEmpty())
            {
               return toWs(folders, wsEnv.getActiveModel(), documentMetaDataType,
                     folderMetaDataType);
            }
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   public FoldersXto findFolders(FolderQueryXto folderQuery) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         if (null != folderQuery)
         {
            List<Folder> folders = null;
            if ( !StringUtils.isEmpty(folderQuery.getNamePattern()))
            {
               folders = dms.findFoldersByName(folderQuery.getNamePattern(),
                     umarshalFolderLOD(folderQuery.getFolderLevelOfDetail()));
            }
            else
            {
               folders = dms.findFolders(folderQuery.getXpathQuery(),
                     umarshalFolderLOD(folderQuery.getFolderLevelOfDetail()));
            }
            if (folders != null && !folders.isEmpty())
            {
               return toWs(folders, wsEnv.getActiveModel(),
                     folderQuery.getDocumentMetaDataType(),
                     folderQuery.getFolderMetaDataType());
            }
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public FolderXto createFolder(String parentFolderId, FolderInfoXto folderInfo)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      Folder folder = null;
      try
      {
         folder = dms.createFolder(parentFolderId, unmarshalFolderInfo(folderInfo));

      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return toWs(folder, wsEnv.getActiveModel(), null, null);
   }

   public FolderXto updateFolder(FolderXto updateFolder) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      Folder folder = null;
      try
      {
         folder = dms.updateFolder(unmarshalFolder(updateFolder, wsEnv.getActiveModel()));

      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return toWs(folder, wsEnv.getActiveModel(), null, updateFolder.getMetaDataType());
   }

   public void removeFolder(String folderId, Boolean recursive) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         dms.removeFolder(folderId, recursive == null ? false : recursive);
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public PrivilegesXto getPrivileges(String resourceId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
         return XmlAdapterUtils.marshalPrivilegeList(dms.getPrivileges(resourceId));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public AccessControlPoliciesXto getPolicies(String resourceId,
         PolicyScopeXto policyScope) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      Set<AccessControlPolicy> ret = null;

      try
      {
         if (policyScope != null)
         {
            switch (policyScope)
            {
            case ALL:
               ret = dms.getPolicies(resourceId);
               break;
            case APPLICABLE:
               ret = dms.getApplicablePolicies(resourceId);
               break;
            case EFFECTIVE:
               ret = dms.getEffectivePolicies(resourceId);
               break;
            }
         }
         else
         {
            ret = dms.getPolicies(resourceId);
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return XmlAdapterUtils.marshalAccessControlPolicyList(ret);
   }

   public void setPolicy(String resourceId, AccessControlPolicyXto accessControlPolicy)
         throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {

         dms.setPolicy(resourceId, XmlAdapterUtils.fromWs(accessControlPolicy));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
   }

   public RepositoryMigrationReportXto migrateRepository(int batchSize,
         boolean evaluateTotalCount) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();
      try
      {
        return toWs(dms.migrateRepository(batchSize, evaluateTotalCount));
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public DocumentTypeResultsXto getDocumentTypes(String modelId) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      ServiceFactory sf = wsEnv.getServiceFactory();
      QueryService qs = sf.getQueryService();
      try
      {
         DeployedModelQuery modelQuery = DeployedModelQuery.findInState(DeployedModelState.VALID);
         if ( !StringUtils.isEmpty(modelId))
         {
            modelQuery.where(DeployedModelQuery.ID.isEqual(modelId));
         }
         Models models = qs.getModels(modelQuery);

         Map<Integer,Model> modelCache = new HashMap<Integer,Model>();
         for (DeployedModelDescription md : models)
         {
            Model model = wsEnv.getModel(md.getModelOID());
            modelCache.put(model.getModelOID(), model);
         }

         DocumentTypeResultsXto results = new DocumentTypeResultsXto();
         for (DeployedModelDescription md : models)
         {
            List<DocumentType> declaredDocumentTypes = DocumentTypeUtils.getDeclaredDocumentTypes(modelCache.get(md.getModelOID()), modelCache);
            if (declaredDocumentTypes != null && !declaredDocumentTypes.isEmpty())
            {
               XmlAdapterUtils.marshalDocumentTypeResult(results, md.getId(), Integer.valueOf(md.getModelOID()), declaredDocumentTypes);
            }
         }

        return results;
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }
      return null;
   }

   public XmlValueXto getDocumentTypeSchema(String schemaLocation) throws BpmFault
   {
      WebServiceEnv wsEnv = WebServiceEnv.currentWebServiceEnvironment();
      DocumentManagementService dms = wsEnv.getServiceFactory()
            .getDocumentManagementService();

      XmlValueXto xto = null;
      try
      {
         byte[] schemaDefinition = dms.getSchemaDefinition(schemaLocation);
         if (schemaDefinition != null)
         {
            xto = new XmlValueXto();

            xto.getAny().add(XmlUtils.parseString(new String(schemaDefinition)).getDocumentElement());
         }
      }
      catch (ApplicationException e)
      {
         XmlAdapterUtils.handleBPMException(e);
      }

      return xto;
   }

}
