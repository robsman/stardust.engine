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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.FilteringIterator;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.query.DataQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.compatibility.el.SymbolTable;
import org.eclipse.stardust.engine.core.runtime.beans.DataQueryEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IDataValue;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.dms.IDmsResourceSyncListener;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.SpiUtils;

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

   /**
    * Parameter key used to prevent cyclic updates. It is checked in the workflow code
    * which handles AuditTrail persistence of document data. If it is true, the update
    * call synchronizing changes to the repository must not be triggered to prevent a call cycle.
    */
   public final static String IS_INTERNAL_DOCUMENT_SYNC_CALL = "org.eclipse.stardust.engine.extensions.dms.data.DmsResourceSyncManager.isInternalDocumentSyncCall";

   public IDmsResourceSyncListener getListener()
   {
      return new DmsResourceSyncManager();
   }

   public void documentChanged(Document oldDocument, Document newDocument)
   {
      ServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();

      ParametersFacade.pushLayer(Collections.singletonMap(IS_INTERNAL_DOCUMENT_SYNC_CALL,
            true));
      try
      {

         ProcessInstanceQuery piWithDocQuery = ProcessInstanceQuery.findHavingDocument(oldDocument);
         ProcessInstances allProcessInstances = sf.getQueryService()
               .getAllProcessInstances(piWithDocQuery);

         for (ProcessInstance processInstance : allProcessInstances)
         {
            ProcessInstanceBean piBean = ProcessInstanceBean.findByOID(processInstance.getOID());

            IModel model = (IModel) piBean.getProcessDefinition().getModel();
            Iterator<IData> allData = model.getData().iterator();

            DataQuery dataQuery = DataQuery.findUsedInProcess(
                  processInstance.getModelOID(), processInstance.getProcessID());
            DataQueryEvaluator dataQueryEvaluator = new DataQueryEvaluator(dataQuery);
            FilteringIterator<IData> dataIterator = new FilteringIterator<IData>(allData,
                  dataQueryEvaluator);

            while (dataIterator.hasNext())
            {
               final IData iData = dataIterator.next();

               String dataTypeId = iData.getType().getId();
               if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
               {
                  final IDataValue iDataValue = piBean.getDataValue(iData);

                  Object value = getEvaluatedDataValue(processInstance, iData, iDataValue);

                  if (value instanceof Document)
                  {
                     Document existingDocument = (Document) value;
                     if (existingDocument.getId().equals(oldDocument.getId()))
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
                  IDataValue iDataValue = piBean.getDataValue(iData);

                  Object value = getEvaluatedDataValue(processInstance, iData, iDataValue);

                  if (value instanceof List)
                  {
                     List<Document> existingDocumentList = (List<Document>) value;
                     List<Document> documentList = CollectionUtils.newArrayList();
                     for (Document existingDocument : existingDocumentList)
                     {
                        if (existingDocument.getId().equals(oldDocument.getId()))
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
                     
                     if ( !existingDocumentList.equals(documentList))
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
      finally
      {
         ParametersFacade.popLayer();
      }

   }

   public void folderChanged(Folder oldFolder, Folder newFolder)
   {
      // TODO to be implemented in later version
   }

   private Object getEvaluatedDataValue(ProcessInstance processInstance,
         final IData iData, final IDataValue iDataValue)
   {
      SymbolTable symbolTable = new SymbolTable()
      {
         public Object lookupSymbol(String name)
         {
            if (name.equals(iData.getId()))
            {
               return iDataValue;
            }
            return null;
         }
   
         public AccessPoint lookupSymbolType(String name)
         {
            if (name.equals(iData.getId()))
            {
               return iData;
            }
            return null;
         }
      };
   
      ExtendedAccessPathEvaluator evaluator = SpiUtils.createExtendedAccessPathEvaluator(iData.getType());
      AccessPathEvaluationContext evaluationContext = new AccessPathEvaluationContext(
            symbolTable, processInstance.getScopeProcessInstanceOID());
      Object value = evaluator.evaluate(iData, iDataValue.getValue(), null,
            evaluationContext);
      return value;
   }

}
