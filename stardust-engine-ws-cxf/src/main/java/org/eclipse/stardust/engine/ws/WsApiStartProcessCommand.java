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
package org.eclipse.stardust.engine.ws;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.ws.DataFlowUtils.unmarshalInitialDataValues;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.checkProcessAttachmentSupport;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.unmarshalInputDocument;
import static org.eclipse.stardust.engine.ws.XmlAdapterUtils.unmarshalInputDocuments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.ws.DocumentInfoXto;
import org.eclipse.stardust.engine.api.ws.InputDocumentXto;
import org.eclipse.stardust.engine.api.ws.InputDocumentsXto;
import org.eclipse.stardust.engine.api.ws.ParametersXto;
import org.eclipse.stardust.engine.core.runtime.beans.DataUsageEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManager;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.QueryServiceImpl;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.utils.DataUtils;
import org.eclipse.stardust.engine.core.struct.StructuredTypeRtUtils;
import org.eclipse.stardust.engine.core.struct.TypedXPath;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;


public class WsApiStartProcessCommand implements ServiceCommand
{
   private static final String PROCESS_ATTACHMENTS = DmsConstants.DATA_ID_ATTACHMENTS;

   private static final long serialVersionUID = 1L;

   private final String processId;

   private final ParametersXto parameters;

   private final Boolean startSynchronously;

   private final InputDocumentsXto attachments;

   private final DataUsageEvaluator dataUsageEvaluator;

   public WsApiStartProcessCommand(String processId, ParametersXto parameters,
         Boolean startSynchronously, InputDocumentsXto attachments)
   {
      this.processId = processId;
      this.parameters = parameters;
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
               model = qs.getActiveModel();
               processDefinition = model.getProcessDefinition(processId);
               unqualifiedProcessId = processId;
            }
         }
         finally
         {
            qs = null;
         }


         @SuppressWarnings("unchecked")
         Map<String, Object> initialDataValues = (Map) unmarshalInitialDataValues(
               unqualifiedProcessId, parameters, model);

         boolean eagerlyStoreAttachments = true;
         boolean allToAttachments = true;
         if (attachments != null)
         {
            // performance optimization: if all attachments use a predefined path, store
            // them directly into the DMS and pass the docs directly with startProcess
            for (InputDocumentXto inputDoc : attachments.getInputDocument())
            {
               eagerlyStoreAttachments &= !isEmpty(inputDoc.getTargetFolder());
               allToAttachments &= isEmpty(inputDoc.getGlobalVariableId());
            }

            if (allToAttachments && eagerlyStoreAttachments)
            {
               checkProcessAttachmentSupport(unqualifiedProcessId, model);
               List<Document> theAttachments = unmarshalInputDocuments(attachments, sf,
                     model, null);

               if (null == initialDataValues)
               {
                  initialDataValues = newHashMap();
               }
               initialDataValues.put(PROCESS_ATTACHMENTS, theAttachments);
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
               List<Document> theAttachments = unmarshalInputDocuments(attachments, sf,
                     model, pi);

               sf.getWorkflowService().setOutDataPath(pi.getOID(), PROCESS_ATTACHMENTS,
                     theAttachments);
            }
            else if ( !allToAttachments)
            {
               // handle each document separately.

               List<Document> processAttachments = CollectionUtils.newArrayList();
               for (InputDocumentXto attachment : attachments.getInputDocument())
               {
                  Document document = unmarshalInputDocument(attachment, sf, model, pi);

                  String dataId = attachment.getGlobalVariableId();
                  if ( !isEmpty(dataId))
                  {
                     DocumentInfoXto documentInfoXto = attachment.getDocumentInfo();
                     QName metaDataType = null;
                     if (documentInfoXto != null)
                     {
                        metaDataType = documentInfoXto.getMetaDataType();
                     }

                     checkSpecificDocumentSupport(document, metaDataType, dataId, model,
                           processDefinition);

                     // Imitating VfsDocumentAccessPathEvaluator.
                     // Inferring and storing DocumentType and directly saving data value.
                     ModelManager modelManager = ModelManagerFactory.getCurrent();
                     IModel imodel = modelManager.findModel(model.getModelOID());
                     IData idata = imodel.findData(DataUtils.getUnqualifiedProcessId(dataId));

                     DocumentTypeUtils.inferDocumentTypeAndStoreDocument(idata, document, sf.getDocumentManagementService());

                     ProcessInstanceBean iPi = ProcessInstanceBean.findByOID(pi.getOID());
                     iPi.setOutDataValue(idata, "", document);
                  }
                  else
                  {
                     processAttachments.add(document);
                  }

               }

               if ( !processAttachments.isEmpty())
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
            Throwable undeclaredThrowable = ((UndeclaredThrowableException) f).getUndeclaredThrowable();
            if (undeclaredThrowable instanceof InvocationTargetException)
            {
               Throwable targetException = ((InvocationTargetException) undeclaredThrowable).getTargetException();
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
      if ( !usedInProcess)
      {
         // data is not used by this process definition
         throw new ObjectNotFoundException(
               BpmRuntimeError.BPMRT_DATA_NOT_USED_BY_PROCESS.raise(dataId, processDefinition.getId()));
      }

      // check metaDataType
      if (metaDataType != null)
      {
         String typeDeclarationId = (String) data.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
         TypeDeclaration typeDeclaration = model.getTypeDeclaration(typeDeclarationId);
         if (null != typeDeclaration)
         {
            Set<TypedXPath> definedXPaths = StructuredTypeRtUtils.getAllXPaths(model,
                  typeDeclaration);
            Set<TypedXPath> requestedXPaths = XmlAdapterUtils.inferStructDefinition(
                  metaDataType, model);
            if (requestedXPaths != null && !requestedXPaths.equals(definedXPaths))
            {
               // has different schemas
               throw new InvalidValueException(
                     BpmRuntimeError.IPPWS_META_DATA_TYPE_INVALID.raise(metaDataType, dataId));
            }
         }
      }
   }
}
