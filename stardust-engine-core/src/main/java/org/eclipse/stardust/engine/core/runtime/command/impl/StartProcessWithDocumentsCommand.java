/*******************************************************************************
 * Copyright (c) 2012, 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.runtime.command.impl;

import static java.util.Collections.emptyList;
import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDNamedComponent;
import org.eclipse.xsd.XSDSchema;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.model.*;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.repository.DocumentRepositoryFolderNames;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.core.struct.emfxsd.XPathFinder;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;

public class StartProcessWithDocumentsCommand implements ServiceCommand
{
   private static final String PROCESS_ATTACHMENTS = DmsConstants.DATA_ID_ATTACHMENTS;

   private static final long serialVersionUID = 1L;

   private final String processId;

   private Map<String, Serializable> initialDataValues;

   private final Boolean startSynchronously;

   private final List<StartProcessInputDocument> attachments;

   private final DataUsageEvaluator dataUsageEvaluator;

   private int modelOid;

   @SuppressWarnings("unchecked")
   public StartProcessWithDocumentsCommand(String processId, int modelOid,
         Map<String, ? extends Serializable> parameters, Boolean startSynchronously,
         List<StartProcessInputDocument> attachments)
   {
      this.processId = processId;
      this.modelOid = modelOid;
      initialDataValues = (Map<String, Serializable>) parameters;
      this.startSynchronously = startSynchronously;
      this.attachments = attachments;
      this.dataUsageEvaluator = new DataUsageEvaluator();
   }

   public ProcessInstance execute(ServiceFactory sf)
   {
      try
      {
         QueryService qs = new QueryServiceImpl();

         Model model = null;
         ProcessDefinition processDefinition = null;
         String unqualifiedProcessId = null;
         try
         {
            if (processId != null && processId.startsWith("{"))
            {
               processDefinition = qs.getProcessDefinition(processId);
               model = qs.getModel(processDefinition.getModelOID(), false);
               unqualifiedProcessId = QName.valueOf(processId).getLocalPart();
            }
            else
            {
               if (modelOid > 0)
               {
                  model = qs.getModel(modelOid);
               }
               else
               {
                  model = qs.getActiveModel();
               }
               processDefinition = model.getProcessDefinition(processId);
               unqualifiedProcessId = processId;
            }

         }
         finally
         {
            qs = null;
         }

         boolean eagerlyStoreAttachments = true;
         boolean allToAttachments = true;
         if (attachments != null)
         {
            // performance optimization: if all attachments use a predefined path, store
            // them directly into the DMS and pass the docs directly with startProcess
            for (StartProcessInputDocument inputDoc : attachments)
            {
               eagerlyStoreAttachments &= !isEmpty(inputDoc.getTargetFolder());
               allToAttachments &= isEmpty(inputDoc.getGlobalVariableId());
            }

            if (allToAttachments && eagerlyStoreAttachments)
            {
               checkProcessAttachmentSupport(unqualifiedProcessId, model);
               List<Document> theAttachments = unmarshalAndStoreInputDocuments(
                     attachments, sf, model, null);

               if (null == initialDataValues)
               {
                  initialDataValues = newHashMap();
               }
               initialDataValues.put(PROCESS_ATTACHMENTS, (Serializable) theAttachments);
            }
         }

         // Start the process
         ProcessInstance pi = sf.getWorkflowService().startProcess(processId,
               initialDataValues, Boolean.TRUE.equals(startSynchronously));

         if (attachments != null)
         {
            if (allToAttachments && !eagerlyStoreAttachments)
            {
               checkProcessAttachmentSupport(unqualifiedProcessId, model);
               List<Document> theAttachments = unmarshalAndStoreInputDocuments(
                     attachments, sf, model, pi);

               sf.getWorkflowService().setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS,
                     theAttachments);
            }
            else if (!allToAttachments)
            {
               // handle each document separately.

               List<Document> processAttachments = CollectionUtils.newArrayList();
               for (StartProcessInputDocument attachment : attachments)
               {
                  Document document = unmarshalAndStoreInputDocument(attachment, sf,
                        model, pi);

                  String dataId = attachment.getGlobalVariableId();
                  if (!isEmpty(dataId))
                  {
                     QName metaDataType = attachment.getMetaDataType();

                     checkSpecificDocumentSupport(document, metaDataType, dataId, model,
                           processDefinition);

                     // Imitating VfsDocumentAccessPathEvaluator.
                     // Inferring and storing DocumentType and directly saving data value.
                     ModelManager modelManager = ModelManagerFactory.getCurrent();
                     IModel imodel = modelManager.findModel(model.getModelOID());
                     IData idata = imodel.findData(DataUtils
                           .getUnqualifiedProcessId(dataId));

                     DocumentTypeUtils.inferDocumentTypeAndStoreDocument(idata, document,
                           sf.getDocumentManagementService());

                     ProcessInstanceBean iPi = ProcessInstanceBean.findByOID(pi.getOID());
                     iPi.setOutDataValue(idata, "", document);
                  }
                  else
                  {
                     processAttachments.add(document);
                  }

               }

               if (!processAttachments.isEmpty())
               {
                  checkProcessAttachmentSupport(unqualifiedProcessId, model);
                  sf.getWorkflowService().setOutDataPath(pi.getOID(),
                        PROCESS_ATTACHMENTS, processAttachments);
               }
            }

         }
         return pi;
      }
      catch (Exception f)
      {
         if (f instanceof UndeclaredThrowableException)
         {
            Throwable undeclaredThrowable = ((UndeclaredThrowableException) f)
                  .getUndeclaredThrowable();
            if (undeclaredThrowable instanceof InvocationTargetException)
            {
               Throwable targetException = ((InvocationTargetException) undeclaredThrowable)
                     .getTargetException();
               throw new ServiceCommandException((String) null, targetException);
            }
            else
            {
               throw new ServiceCommandException((String) null, f);
            }
         }
         else
         {
            throw new ServiceCommandException((String) null, f);
         }
      }
   }

   private List<Document> unmarshalAndStoreInputDocuments(
         List<StartProcessInputDocument> attachments, ServiceFactory sf, Model model,
         ProcessInstance pi) throws StartProcessCommandException
   {
      List<Document> theAttachments = emptyList();

      if ((null != attachments) && !isEmpty(attachments))
      {
         theAttachments = newArrayList();

         for (StartProcessInputDocument attachment : attachments)
         {
            Document doc = unmarshalAndStoreInputDocument(attachment, sf, model, pi);

            theAttachments.add(doc);
         }
      }

      return theAttachments;
   }

   private Document unmarshalAndStoreInputDocument(StartProcessInputDocument attachment,
         ServiceFactory sf, Model model, ProcessInstance pi)
         throws StartProcessCommandException
   {
      if (isEmpty(attachment.getTargetFolder()))
      {
         assert (null != pi);

         // use PI-OID based folder
         StringBuilder defaultPath = new StringBuilder(DmsUtils.composeDefaultPath(
               pi.getScopeProcessInstanceOID(), pi.getStartTime())).append("/");

         String dataId = attachment.getGlobalVariableId();
         if (isEmpty(dataId))
         {
            defaultPath
                  .append(DocumentRepositoryFolderNames.PROCESS_ATTACHMENTS_SUBFOLDER);
         }
         else
         {
            defaultPath
                  .append(DocumentRepositoryFolderNames.SPECIFIC_DOCUMENTS_SUBFOLDER);
         }

         attachment.setTargetFolder(defaultPath.toString());
      }

      DmsUtils.ensureFolderHierarchyExists(attachment.getTargetFolder(), sf.getDocumentManagementService());

      Document doc = storeDocumentIntoDms(sf.getDocumentManagementService(), model,
            attachment);
      return doc;
   }

   private Document storeDocumentIntoDms(DocumentManagementService dms, Model model,
         StartProcessInputDocument inputDoc) throws StartProcessCommandException
   {
      DocumentInfo docInfo = inputDoc.getDocumentInfo();
      String folderId = inputDoc.getTargetFolder();
      byte[] content = inputDoc.getContent();

      String documentPath = folderId;
      if (!folderId.endsWith("/"))
      {
         documentPath += "/";
      }
      documentPath += docInfo.getName();

      try
      {
         Document doc = (null != content) ? dms.createDocument(folderId, docInfo,
               content, /* TODO encoding? */null) : dms.createDocument(folderId, docInfo);
         if (inputDoc.isVersion())
         {
            doc = dms.versionDocument(doc.getId(), inputDoc.getComment(),
                  inputDoc.getLabel());
         }

         return doc;
      }
      catch (DocumentManagementServiceException dmse)
      {
         if (BpmRuntimeError.DMS_ITEM_EXISTS.raise().getId()
               .equals(dmse.getError().getId()))
         {
            throw new StartProcessCommandException("There already exists a file at "
                  + documentPath, "ItemAlreadyExists");
         }
         else if (BpmRuntimeError.DMS_FAILED_PATH_RESOLVE.raise(null).getId()
               .equals(dmse.getError().getId()))
         {
            throw new StartProcessCommandException(dmse.getMessage(), "InvalidName");
         }
         else if (BpmRuntimeError.DMS_UNKNOWN_FOLDER_ID.raise(null).getId()
               .equals(dmse.getError().getId()))
         {

            throw new StartProcessCommandException(dmse.getMessage(), "ItemDoesNotExist");
         }
         else if (BpmRuntimeError.DMS_DOCUMENT_TYPE_INVALID.raise(null).getId()
               .equals(dmse.getError().getId()))
         {
            throw new StartProcessCommandException(dmse.getMessage(),
                  "DocumentManagementServiceException");
         }
         else if (!isEmpty(dmse.getError().getId()) && !isEmpty(dmse.getMessage()))
         {
            // marshal as DocumentManagementServiceException if error ID exists.
            throw new StartProcessCommandException(dmse.getMessage(), "DocumentManagementServiceException");
         }
         else
         {
            throw new StartProcessCommandException("Failed storing file at "
                  + documentPath, "UnknownError");
         }
      }
   }

   private void checkSpecificDocumentSupport(Document document, QName metaDataType,
         String dataId, Model model, ProcessDefinition processDefinition)
   {
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      IModel imodel = modelManager.findModel(model.getModelOID());
      IData data = imodel.findData(DataUtils.getUnqualifiedProcessId(dataId));

      if (data == null)
      {
         // data does not exist
         throw new ObjectNotFoundException(
               BpmRuntimeError.MDL_UNKNOWN_DATA_ID.raise(dataId));
      }

      boolean usedInProcess = dataUsageEvaluator.isUsedInProcess(data, imodel,
            processDefinition.getId());
      if (!usedInProcess)
      {
         // data is not used by this process definition
         throw new ObjectNotFoundException(
               BpmRuntimeError.BPMRT_DATA_NOT_USED_BY_PROCESS.raise(dataId,
                     processDefinition.getId()));
      }

      // check metaDataType
      if (metaDataType != null)
      {
         String typeDeclarationId = (String) data
               .getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
         if (null != typeDeclaration)
         {
            Set<TypedXPath> definedXPaths = StructuredTypeRtUtils.getAllXPaths(model,
                  typeDeclaration);
            Set<TypedXPath> requestedXPaths = inferStructDefinition(
                  metaDataType, model);
            if (requestedXPaths != null && !requestedXPaths.equals(definedXPaths))
            {
               // has different schemas
               throw new InvalidValueException(
                     BpmRuntimeError.IPPWS_META_DATA_TYPE_INVALID.raise(metaDataType,
                           dataId));
            }
         }
      }
   }

   private static void checkProcessAttachmentSupport(String processId, Model model)
         throws StartProcessCommandException
   {
      ProcessDefinition processDefinition = model.getProcessDefinition(processId);
      if (processDefinition == null)
      {
         throw new StartProcessCommandException("The process with ID '" + processId
               + "' was not found in the model.", "ObjectNotFoundException");
      }
      DataPath attachmentsDefinition = processDefinition.getDataPath("PROCESS_ATTACHMENTS");
      if ((null == attachmentsDefinition)
            || !attachmentsDefinition.getDirection().isCompatibleWith(Direction.IN))
      {
         throw new StartProcessCommandException("The process with ID '" + processId
               + "' does not support attachments.", "InvalidConfiguration");
      }
   }

  /**
   * Infers the TypedXPaths from the QName of the type definition in the specified model.
   *
   * @param typeName The qualified name of the xsd type definition.
   * @param model the model to look up in.
   * @return Inferred XPaths.
   */
  private static Set<TypedXPath> inferStructDefinition(QName typeName, Model model)
  {
     Set<TypedXPath> xPaths = null;
     if (model != null)
     {
        for (TypeDeclaration type : (List<TypeDeclaration>) model.getAllTypeDeclarations())
        {
           XSDSchema schema = null;
           if (type.getXpdlType() instanceof SchemaType)
           {
              SchemaType schemaType = (SchemaType) type.getXpdlType();

              schema = schemaType.getSchema();
           }
           else if (type.getXpdlType() instanceof ExternalReference)
           {
              ExternalReference refType = (ExternalReference) type.getXpdlType();

              if (typeName.toString().equals(refType.getXref()))
              {
                 schema = refType.getSchema(model);
              }
           }

           xPaths = getXPathsFromSchema(typeName, schema);
           if ( !isEmpty(xPaths))
           {
              break;
           }
        }
     }
     return xPaths;
  }

  private static Set<TypedXPath> getXPathsFromSchema(QName typeName, XSDSchema schema)
  {
     Set<TypedXPath> xPaths = CollectionUtils.newSet();
     if (null != schema)
     {
        XSDNamedComponent metaDataType = XPathFinder.findTypeDefinition(schema,
              typeName.toString());

        if (null == metaDataType)
        {
           XSDElementDeclaration element = XPathFinder.findElement(schema,
                 typeName.toString());
           if ((null != element) && (null != element.getAnonymousTypeDefinition()))
           {
              // matching element name defining an anonymous type
              metaDataType = element;
           }
        }

        if (null != metaDataType)
        {
           xPaths = XPathFinder.findAllXPaths(schema, metaDataType);
        }
     }
     return xPaths;
  }
}
