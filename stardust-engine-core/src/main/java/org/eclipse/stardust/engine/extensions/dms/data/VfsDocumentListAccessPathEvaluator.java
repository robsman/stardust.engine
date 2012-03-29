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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IProcessInstance;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;



/**
 * Access path evaluator for document lists.
 */
public class VfsDocumentListAccessPathEvaluator extends AbstractVfsResourceAccessPathEvaluator
      implements ExtendedAccessPathEvaluator, Stateless
{

   private static final Logger trace = LogManager.getLogger(VfsDocumentListAccessPathEvaluator.class);

   private static final String XPATH_PREFIX = AuditTrailUtils.DOCS_DOCUMENTS+"/";

   private static final String PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS = "PROCESS_DEFINITIONS_WITH_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS";

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext,
         Object value)
   {
      // since documents with different metadata can reside in a document list,
      // unknown metadata schema elements should be ignored and not written to
      // audit trail
      accessPathEvaluationContext.setIgnoreUnknownValueParts(true);

      if (StringUtils.isEmpty(inPath))
      {
         if (value == null)
         {
            // write null to structured data
            Map auditTrailDocList = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, null, accessPathEvaluationContext);
            if (null != auditTrailDocList)
            {
               // update only if current value is not null
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext,
                     null, XPATH_PREFIX);
            }
         }
         else if (value instanceof Long)
         {
            // we have received the oid of an existing data, make a duplicate
            Map auditTrailDocList = (Map) readFromAuditTrail(accessPointDefinition,
                  value, null, accessPathEvaluationContext);
            accessPointInstance = writeToAuditTrail(accessPointDefinition,
                  null, null, accessPathEvaluationContext,
                  auditTrailDocList, XPATH_PREFIX);
         }
         else if (value instanceof List)
         {
            // handle rootProcessAttachments
            ProcessAttachmentByRootProcessWrapper wrapper = new ProcessAttachmentByRootProcessWrapper(accessPathEvaluationContext, accessPointInstance, accessPointDefinition);
            AccessPathEvaluationContext accessPathEvaluationContext2 = wrapper.getAccessPathEvaluationContext();
            Object accessPointInstance2 = wrapper.getAccessPointInstance();
            // fully updating the document list

            // load snapshot from audit trail
            Map auditTrailDocList = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance2, null, accessPathEvaluationContext2);
            if (null == auditTrailDocList)
            {
               auditTrailDocList = CollectionUtils.newMap();
            }

            // build lego structure from List<IDmsDocument>
            Map newAuditTrailDocList = CollectionUtils.newHashMap();
            List<Map> legoDocuments = CollectionUtils.newLinkedList();
            newAuditTrailDocList.put(AuditTrailUtils.DOCS_DOCUMENTS, legoDocuments);
            for (Iterator i = ((List)value).iterator(); i.hasNext(); )
            {
               DmsDocumentBean dmsDocument = (DmsDocumentBean)i.next();
               legoDocuments.add(dmsDocument.vfsResource());
            }

            // infer document type.
            List<Map> toSyncDocuments = CollectionUtils.newLinkedList();
            if (accessPointDefinition instanceof IData)
            {
               List<Document> documentList = (List<Document>) value;
               IData data = (IData) accessPointDefinition;

               for (Document document : documentList)
               {
                  DocumentType inferredDocumentType = DocumentTypeUtils.inferDocumentType(
                        data, document);
                  if (inferredDocumentType != null)
                  {
                     DocumentType inputDocumentType = document.getDocumentType();
                     if (inputDocumentType == null)
                     {
                        document.setDocumentType(inferredDocumentType);
                        toSyncDocuments.add(((DmsDocumentBean)document).vfsResource());
                        if (trace.isInfoEnabled())
                        {
                           trace.info("Inferred document type of document '"
                                 + document.getName() + "' as '"
                                 + inferredDocumentType.getDocumentTypeId()
                                 + "' based on data '" + data.getId() + "'.");
                        }
                     }
                     else if ( !inferredDocumentType.equals(inputDocumentType))
                     {
                        throw new InvalidValueException(
                              BpmRuntimeError.DMS_DOCUMENT_TYPE_INVALID.raise(inputDocumentType.getDocumentTypeId()));
                     }
                  }
               }
            }

            // update audit trail, only if needed

            if ( !auditTrailDocList.equals(newAuditTrailDocList))
            {
               if (wrapper.isRootProcessAttachmentAttributeEnabled())
               {
                  accessPathEvaluationContext2.getProcessInstance().lock();
               }
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance2, null, accessPathEvaluationContext2,
                     newAuditTrailDocList, XPATH_PREFIX);
               // sync documents having document type changed to repository
               for (Map legoDocument : toSyncDocuments)
               {
                  new VfsMediator().writeDocumentToVfs(legoDocument,
                        false, null, false);
               }

            }
         }
         else
         {
            throw new PublicException("Unsupported value: " + value);
         }
      }
      else
      {
         // partially updating the document list

         // for the case of data mappings to the documents (e.g. document[1])
         // convert Document to Map
         if (value instanceof DmsDocumentBean)
         {
            value = ((DmsDocumentBean)value).vfsResource();
            if (AuditTrailUtils.hasDefaultMetadataSchema(accessPointDefinition))
            {
               List propertyList = AuditTrailUtils.convertToPropertyList((Map) ((Map)value).get(AuditTrailUtils.RES_PROPERTIES));
               ((Map)value).put(AuditTrailUtils.RES_PROPERTIES, propertyList);
            }
         }

         // write value into audit trail
         accessPointInstance = writeToAuditTrail(accessPointDefinition,
               accessPointInstance, inPath, accessPathEvaluationContext, value, XPATH_PREFIX);
      }


      return accessPointInstance;
   }

   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      // TODO Auto-generated method stub
      return null;
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {

      ProcessAttachmentByRootProcessWrapper wrapper = new ProcessAttachmentByRootProcessWrapper(accessPathEvaluationContext, accessPointInstance, accessPointDefinition);
      AccessPathEvaluationContext accessPathEvaluationContext2 = wrapper.getAccessPathEvaluationContext();
      Object accessPointInstance2 = wrapper.getAccessPointInstance();


      if (null == accessPointInstance2)
      {
         trace.debug("returning null for outPath '" + outPath + "'");
         return null;
      }

      if (accessPointDefinition instanceof DmsDocumentListAccessPoint)
      {
         if (StringUtils.isEmpty(outPath))
         {
            return (List/*<IDmsDocument>*/)accessPointInstance;
         }
         else
         {
            throw new InternalException("TODO: non-empty out access paths are not supported yet");
         }
      }
      else
      {
         if (StringUtils.isEmpty(outPath))
         {
            final Map auditTrailDocList = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance2, outPath, accessPathEvaluationContext2);

            if (auditTrailDocList == null || !auditTrailDocList.containsKey(AuditTrailUtils.DOCS_DOCUMENTS))
            {
               return null;
            }

            List /*<IDmsDocument>*/ documentList = CollectionUtils.newLinkedList();
            List legoDocuments = (List)auditTrailDocList.get(AuditTrailUtils.DOCS_DOCUMENTS);
            for (Iterator i = legoDocuments.iterator(); i.hasNext(); )
            {
               Map legoDocument = (Map) i.next();
               documentList.add(new DmsDocumentBean(legoDocument));
            }

            return documentList;
         }
         else
         {
            return readFromAuditTrail(accessPointDefinition, accessPointInstance2,
                  outPath, accessPathEvaluationContext2);
         }
      }
   }

   private class ProcessAttachmentByRootProcessWrapper
   {

      private AccessPathEvaluationContext accessPathEvaluationContext;
      private Object accessPointInstance;
      private boolean isRootProcessAttachmentAttributeEnabled;

      public ProcessAttachmentByRootProcessWrapper(AccessPathEvaluationContext accessPathEvaluationContext, Object accessPointInstance, AccessPoint accessPointDefinition)
      {
        this.isRootProcessAttachmentAttributeEnabled = handleRootPIProcessAttachmentsEvalContext(accessPointDefinition, accessPointInstance, accessPathEvaluationContext);
      }

      public boolean isRootProcessAttachmentAttributeEnabled()
      {
         return isRootProcessAttachmentAttributeEnabled;
      }

      public Object getAccessPointInstance()
      {
        return accessPointInstance;
      }

      public AccessPathEvaluationContext getAccessPathEvaluationContext()
      {
         return accessPathEvaluationContext;
      }


      private boolean isRootProcessAttachmentAttributeEnabled(
            AccessPoint accessPointDefinition,
            AccessPathEvaluationContext accessPathEvaluationContext)
      {
         if (DmsConstants.DATA_ID_ATTACHMENTS.equals(accessPointDefinition.getId()))
         {
            Map<Long, Boolean> byRefAttributeCache = (Map<Long, Boolean>) Parameters.instance()
                  .get(PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS);
            if (byRefAttributeCache == null)
            {
               byRefAttributeCache = CollectionUtils.newHashMap();
            }

            IProcessDefinition pd = accessPathEvaluationContext.getProcessInstance()
                  .getProcessDefinition();
            IModel model = (IModel) pd.getModel();
            Long modelOid = Long.valueOf(model.getOID());

            Boolean byRef = byRefAttributeCache.get(modelOid);
            if (byRef == null)
            {
               ModelElementList processDefinitions = model.getProcessDefinitions();
               byRef = Boolean.FALSE;
               for (int i = 0; i < processDefinitions.size(); i++ )
               {
                  IProcessDefinition innerPd = (IProcessDefinition) processDefinitions.get(i);
                  if (Boolean.TRUE.equals(innerPd.getAttribute(DmsConstants.BY_REFERENCE_ATT)))
                  {
                     byRef = Boolean.TRUE;
                     break;
                  }
               }

               byRefAttributeCache.put(modelOid, byRef);
               Parameters.instance().set(PROP_MODEL_PROCESS_ATTACHMENTS_BY_ROOT_PROCESS,
                     byRefAttributeCache);
            }

            if (byRef)
            {
               IProcessInstance rootPI = accessPathEvaluationContext.getProcessInstance()
                     .getRootProcessInstance();
               if (Boolean.TRUE.equals(rootPI.getProcessDefinition().getAttribute(
                     DmsConstants.BY_REFERENCE_ATT)))
               {
                  return true;
               }
            }
         }
         return false;
      }

      private boolean handleRootPIProcessAttachmentsEvalContext(AccessPoint accessPointDefinition, Object accessPointInstance, AccessPathEvaluationContext accessPathEvaluationContext)
      {
         boolean modified;
         if (isRootProcessAttachmentAttributeEnabled(accessPointDefinition, accessPathEvaluationContext))
         {
            IProcessInstance rootPI = accessPathEvaluationContext.getProcessInstance()
                  .getRootProcessInstance();
            this.accessPathEvaluationContext = new AccessPathEvaluationContext(rootPI,
                  accessPathEvaluationContext.getTargetAccessPointDefinition(),
                  accessPathEvaluationContext.getTargetPath(),
                  accessPathEvaluationContext.getActivity());

            if (rootPI instanceof ProcessInstanceBean && accessPointDefinition instanceof IData)
            {
               this.accessPointInstance = rootPI.getDataValue(
                     (IData) accessPointDefinition).getValue();
            }
            modified = true;
         }
         else
         {
            this.accessPathEvaluationContext = accessPathEvaluationContext;
            this.accessPointInstance = accessPointInstance;
            modified = false;
         }

         return modified;
      }

   }

   public boolean isStateless()
   {
      return true;
   }
}
