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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.query.DataQuery;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.model.utils.ModelElementList;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.spi.dms.IDmsResourceSyncListener;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.struct.beans.StructuredDataValueBean;

/**
 * Responsible to sync existing AuditTrail document data and folder data.
 *
 * @author Roland.Stamm
 *
 */
public class DmsResourceSyncManager
      implements IDmsResourceSyncListener, IDmsResourceSyncListener.Factory
{

   private final static Logger trace = LogManager.getLogger(DmsResourceSyncManager.class);

   public IDmsResourceSyncListener getListener()
   {
      return new DmsResourceSyncManager();
   }

   public void documentChanged(Document oldDocument, Document newDocument)
   {
      ParametersFacade.pushLayer(Collections.singletonMap(
            AbstractVfsResourceAccessPathEvaluator.IS_INTERNAL_DOCUMENT_SYNC_CALL, true));
      try
      {
         Set<Long> allProcessInstances = getScopePisHavingDocument(oldDocument);

         for (Long processInstanceOid : allProcessInstances)
         {
            ProcessInstanceBean piBean = ProcessInstanceBean
                  .findByOID(processInstanceOid);

            IModel model = (IModel) piBean.getProcessDefinition().getModel();
            Iterator<IData> allData = model.getData().iterator();

            DataQuery dataQuery = DataQuery.findUsedInProcess(piBean.getModelOID(),
                  piBean.getProcessDefinition().getId());
            DataQueryEvaluator dataQueryEvaluator = new DataQueryEvaluator(dataQuery);
            FilteringIterator<IData> dataIterator = new FilteringIterator<IData>(allData,
                  dataQueryEvaluator);

            while (dataIterator.hasNext())
            {
               final IData iData = dataIterator.next();

               AccessPoint triggeringAccessPoint = Parameters
                     .instance()
                     .getObject(
                           AbstractVfsResourceAccessPathEvaluator.DMS_SYNC_CURRENT_ACCESS_POINT,
                           null);

               // The triggering accesspoint must not be synchronized, it is handled in
               // the call which has triggered the synchronization.
               if (!iData.equals(triggeringAccessPoint))
               {

                  String dataTypeId = iData.getType().getId();
                  if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
                  {
                     Object value = piBean.getInDataValue(iData, null);

                     if (value instanceof Document)
                     {
                        Document existingDocument = (Document) value;
                        if (RepositoryIdUtils.resourceIdEquals(existingDocument.getId(),
                              oldDocument.getId()))
                        {
                           // needs update
                           piBean.setOutDataValue(iData, "", newDocument);
                        }
                     }
                     else if (value == null)
                     {
                        // was null, data not initialized yet
                     }
                     else
                     {
                        trace.warn("Dms data value not of type Document: " + value);
                     }

                  }
                  else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
                  {
                     Object value = piBean.getInDataValue(iData, null);

                     if (value instanceof List)
                     {
                        List<Document> existingDocumentList = (List<Document>) value;
                        List<Document> documentList = CollectionUtils.newArrayList();
                        for (Document existingDocument : existingDocumentList)
                        {
                           if (RepositoryIdUtils.resourceIdEquals(existingDocument.getId(),
                                 oldDocument.getId()))
                           {
                              if (newDocument != null)
                              {
                                 documentList.add(newDocument);
                              }
                           }
                           else
                           {
                              documentList.add(existingDocument);
                           }
                        }

                        if (documentList.isEmpty())
                        {
                           // don't set empty list of no documents are contained.
                           documentList = null;
                        }

                        if (!existingDocumentList.equals(documentList))
                        {
                           piBean.setOutDataValue(iData, "", documentList);
                        }
                     }
                     else if (value == null)
                     {
                        // was null, data not initialized yet
                     }
                     else
                     {
                        trace.warn("Dms data value not of type List<Document>: " + value);
                     }
                  }
                  else if (DmsConstants.DATA_TYPE_DMS_FOLDER.equals(dataTypeId))
                  {
                     // TODO to be implemented in later version
                  }
                  else if (DmsConstants.DATA_TYPE_DMS_FOLDER_LIST.equals(dataTypeId))
                  {
                     // TODO to be implemented in later version
                  }
               }
            }

         }
      }
      finally
      {
         ParametersFacade.popLayer();
      }

   }

   private Set<Long> getScopePisHavingDocument(Document oldDocument)
   {
      final String documentsIdXPath = AuditTrailUtils.DOCS_DOCUMENTS + "/"
            + AuditTrailUtils.RES_ID;

      // Fetch XPath Oids from runtimeOid cache.
      Set<Long> xPathOids = CollectionUtils.newHashSet();
      ModelManager modelManager = ModelManagerFactory.getCurrent();
      List<IModel> models = modelManager.findActiveModels();
      for (IModel iModel : models)
      {
         ModelElementList<IData> allData = iModel.getData();
         for (IData iData : allData)
         {
            String dataTypeId = iData.getType().getId();
            if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
            {
               long runtimeOid = modelManager.getRuntimeOid(iData, AuditTrailUtils.RES_ID);
               if (runtimeOid > 0)
               {
                  xPathOids.add(runtimeOid);
               }
            }
            else if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(dataTypeId))
            {
               long runtimeOid = modelManager.getRuntimeOid(iData, documentsIdXPath);
               if (runtimeOid > 0)
               {
                  xPathOids.add(runtimeOid);
               }
            }
         }
      }

      Set<Long> scopePiOids = CollectionUtils.newHashSet();
      if (!xPathOids.isEmpty())
      {
         ResultSet rsScopePiOids = null;
         try
         {
            Session session = (Session) SessionFactory
                  .getSession(SessionFactory.AUDIT_TRAIL);
            String truncatedDocumentId = StringUtils.cutString(oldDocument.getId(),
                  StructuredDataValueBean.string_value_COLUMN_LENGTH);

            QueryDescriptor queryDescriptor = QueryDescriptor.from(
                  ProcessInstanceBean.class).select(ProcessInstanceBean.FIELD__OID);

            queryDescriptor.leftOuterJoin(StructuredDataValueBean.class, "PR_sdv1").on(
                  ProcessInstanceBean.FR__SCOPE_PROCESS_INSTANCE,
                  StructuredDataValueBean.FIELD__PROCESS_INSTANCE);

            queryDescriptor
                  .where(Predicates.andTerm(Predicates.inList(
                        StructuredDataValueBean.FR__XPATH, xPathOids.iterator()), //
                        Predicates.isEqual(StructuredDataValueBean.FR__TYPE_KEY,
                              BigData.STRING), //
                        Predicates.orTerm( //
                              Predicates.isEqual(
                                    StructuredDataValueBean.FR__STRING_VALUE,
                                    truncatedDocumentId), //
                              Predicates.isEqual(
                                    StructuredDataValueBean.FR__STRING_VALUE,
                                    RepositoryIdUtils
                                          .stripRepositoryId(truncatedDocumentId)))));

            rsScopePiOids = session.executeQuery(queryDescriptor);

            while (rsScopePiOids.next())
            {
               scopePiOids.add(rsScopePiOids.getLong(1));
            }
         }
         catch (SQLException sqle)
         {
            final String message = "Exeception on DmsResource Synchronization. ";
            trace.warn(message, sqle);
         }
         finally
         {
            QueryUtils.closeResultSet(rsScopePiOids);
         }
      }
      return scopePiOids;
   }

   public void folderChanged(Folder oldFolder, Folder newFolder)
   {
      // TODO to be implemented in later version
   }

}
