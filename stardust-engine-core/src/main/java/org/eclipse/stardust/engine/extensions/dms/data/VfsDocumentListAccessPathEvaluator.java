/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.extensions.dms.data;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.model.IProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.persistence.Functions;
import org.eclipse.stardust.engine.core.persistence.Join;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataBean;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * Access path evaluator for document lists.
 */
public class VfsDocumentListAccessPathEvaluator extends AbstractVfsResourceAccessPathEvaluator
      implements ExtendedAccessPathEvaluator, Stateless, IHandleGetDataValue
{
   private static final Logger trace = LogManager.getLogger(VfsDocumentListAccessPathEvaluator.class);

   private static final String XPATH_PREFIX = AuditTrailUtils.DOCS_DOCUMENTS+"/";

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
            ProcessAttachmentByRootProcessWrapper wrapper = new ProcessAttachmentByRootProcessWrapper(accessPathEvaluationContext, accessPointInstance, accessPointDefinition, false);
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
               legoDocuments.add(CollectionUtils.copyMap(dmsDocument.vfsResource()));
            }

            // infer document type.
            List<Document> toSyncDocuments = CollectionUtils.newLinkedList();
            if (accessPointDefinition instanceof IData)
            {
               List<Document> documentList = (List<Document>) value;
               IData data = (IData) accessPointDefinition;

               for (Document document : documentList)
               {
                  DocumentType documentType = DocumentTypeUtils.inferDocumentType(data);
                  if (documentType != null)
                  {
                     document.setDocumentType(documentType);
                  }
                  toSyncDocuments.add(document);
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
               for (Document document : toSyncDocuments)
               {
                  syncToRepository(document, trace, accessPointDefinition);
               }

            }
         }
         else
         {
            throw new PublicException(BpmRuntimeError.DMS_UNSUPPORTED_VALUE.raise(value));
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

   // IHandleGetDataValue implementation
   public Object evaluate(AccessPoint accessPointDefinition, String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return evaluate(accessPointDefinition, null, outPath, accessPathEvaluationContext, true);
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return evaluate(accessPointDefinition, accessPointInstance, outPath, accessPathEvaluationContext, false);
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance, String outPath, AccessPathEvaluationContext accessPathEvaluationContext, boolean isProcess)
   {
      ProcessAttachmentByRootProcessWrapper wrapper = new ProcessAttachmentByRootProcessWrapper(accessPathEvaluationContext, accessPointInstance, accessPointDefinition, isProcess);
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
         else if(outPath.equals(AuditTrailUtils.DOCS_COUNT))
         {
            // Execute Query to retrieve the document Count

            try
            {
               ResultSet rs = null;
               try
               {
                  Session session = (Session) SessionFactory.getSession(SessionFactory.AUDIT_TRAIL);

                  IProcessDefinition pd = accessPathEvaluationContext.getProcessInstance()
                        .getProcessDefinition();
                  IModel model = (IModel) pd.getModel();
                  Long modelOid = Long.valueOf(model.getOID());

                  QueryDescriptor query = QueryDescriptor.from(
                        StructuredDataValueBean.class).select(Functions.countDistinct(StructuredDataValueBean.FR__OID));

                  query.getQueryExtension()
                        .addJoin(
                              new Join(StructuredDataBean.class).on(
                                    StructuredDataValueBean.FR__XPATH,
                                    StructuredDataBean.FIELD__OID))
                        .addJoin(
                              new Join(AuditTrailDataBean.class).on(
                                    StructuredDataBean.FR__DATA,
                                    AuditTrailDataBean.FIELD__OID))
                        .setWhere(
                              Predicates.andTerm(
                                    Predicates.isEqual(
                                          StructuredDataValueBean.FR__PROCESS_INSTANCE,
                                          accessPathEvaluationContext.getScopeProcessInstanceOID()),
                                    Predicates.isEqual(AuditTrailDataBean.FR__ID,
                                          accessPointDefinition.getId()),
                                    Predicates.isEqual(StructuredDataBean.FR__XPATH,
                                          AuditTrailUtils.DOCS_DOCUMENTS)));



                  rs = session.executeQuery(query);

                  if (rs.next())
                  {
                     return rs.getInt(1);
                  }
               }
               finally
               {
                  QueryUtils.closeResultSet(rs);
               }
            }
            catch (Exception e)
            {
               throw new PublicException("Failed to retrieve data for XPATH "
                     + AuditTrailUtils.DOCS_COUNT, e);
            }

            return null;
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
      private IDataValue dataValue;
      private boolean isProcess = false;

      public ProcessAttachmentByRootProcessWrapper(AccessPathEvaluationContext accessPathEvaluationContext, Object accessPointInstance, AccessPoint accessPointDefinition, boolean isProcess)
      {
        this.isProcess = isProcess;
        this.isRootProcessAttachmentAttributeEnabled = handleRootPIProcessAttachmentsEvalContext(accessPointDefinition, accessPointInstance, accessPathEvaluationContext);
      }

      public ProcessAttachmentByRootProcessWrapper(AccessPathEvaluationContext accessPathEvaluationContext, AccessPoint accessPointDefinition, AbstractInitialDataValueProvider dataValueProvider)
      {
         this.isRootProcessAttachmentAttributeEnabled = handleRootPIProcessAttachmentsEvalContext(accessPointDefinition, dataValueProvider, accessPathEvaluationContext);
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
    	  return RootPIUtils.isRootProcessAttachmentAttributeEnabled(accessPointDefinition.getId(), accessPathEvaluationContext.getProcessInstance());
      }

      private boolean handleRootPIProcessAttachmentsEvalContext(AccessPoint accessPointDefinition, Object accessPointInstance, AccessPathEvaluationContext accessPathEvaluationContext)
      {
         IProcessInstance pi = accessPathEvaluationContext
               .getProcessInstance();

         if (isRootProcessAttachmentAttributeEnabled(accessPointDefinition, accessPathEvaluationContext))
         {
            IProcessInstance rootPI = pi.getRootProcessInstance();
            if (pi.getOID() != rootPI.getOID())
            {
               this.accessPathEvaluationContext = new AccessPathEvaluationContext(
                     rootPI,
                     accessPathEvaluationContext
                           .getTargetAccessPointDefinition(),
                     accessPathEvaluationContext.getTargetPath(),
                     accessPathEvaluationContext.getActivity());

               if (rootPI instanceof ProcessInstanceBean
                     && accessPointDefinition instanceof IData) {
                  this.accessPointInstance = rootPI.getDataValue(
                        (IData) accessPointDefinition).getValue();
               }
               return true;
            }
         }

         if(isProcess)
         {
            if (accessPointDefinition instanceof IData)
            {
               IDataValue dataValue = pi.getDataValue((IData) accessPointDefinition);
               this.accessPointInstance = dataValue.getValue();
            }
         }
         else
         {
            this.accessPointInstance = accessPointInstance;
         }

         this.accessPathEvaluationContext = accessPathEvaluationContext;

         return false;
      }


      private boolean handleRootPIProcessAttachmentsEvalContext(AccessPoint accessPointDefinition, AbstractInitialDataValueProvider dataValueProvider, AccessPathEvaluationContext accessPathEvaluationContext)
      {
         IProcessInstance pi = accessPathEvaluationContext
               .getProcessInstance();

         if (isRootProcessAttachmentAttributeEnabled(accessPointDefinition, accessPathEvaluationContext))
         {
            IProcessInstance rootPI = pi.getRootProcessInstance();
            if (pi.getOID() != rootPI.getOID())
            {

               if (rootPI instanceof ProcessInstanceBean
                     && accessPointDefinition instanceof IData) {
                     dataValue = rootPI.getDataValue(
                           (IData) accessPointDefinition, dataValueProvider);
               }
               return true;
            }
         }

         if (accessPointDefinition instanceof IData)
         {
            dataValue = pi.getDataValue((IData) accessPointDefinition, dataValueProvider);
         }

         return false;
      }

      public IDataValue getDataValue()
      {
         return dataValue;
      }

   }

   public boolean isStateless()
   {
      return true;
   }

   @Override
   public IDataValue getDataValue(AccessPoint accessPointDefinition,
         AbstractInitialDataValueProvider dataValueProvider,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      ProcessAttachmentByRootProcessWrapper wrapper = new ProcessAttachmentByRootProcessWrapper(accessPathEvaluationContext, accessPointDefinition, dataValueProvider);
      return wrapper.getDataValue();
   }
}