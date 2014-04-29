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
import org.eclipse.stardust.common.config.Parameters;
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
import org.eclipse.stardust.engine.api.runtime.Resource;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.DataQueryEvaluator;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.spi.dms.IDmsResourceSyncListener;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryProviderManager;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;

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
      ServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();

      ParametersFacade.pushLayer(Collections.singletonMap(
            AbstractVfsResourceAccessPathEvaluator.IS_INTERNAL_DOCUMENT_SYNC_CALL, true));
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

               AccessPoint triggeringAccessPoint = Parameters.instance()
                     .getObject(
                           AbstractVfsResourceAccessPathEvaluator.DMS_SYNC_CURRENT_ACCESS_POINT,
                           null);

               // The triggering accesspoint must not be synchronized, it is handled in the call which has triggered the synchronization.
               if ( !iData.equals(triggeringAccessPoint))
               {

               String dataTypeId = iData.getType().getId();
               if (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataTypeId))
               {
                     Object value = piBean.getInDataValue(iData, null);

                  if (value instanceof Document)
                  {
                     Document existingDocument = (Document) value;
                     if (resourceIdEquals(existingDocument.getId(), oldDocument.getId()))
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
                        if (resourceIdEquals(existingDocument.getId(), oldDocument.getId()))
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
      }
      finally
      {
         ParametersFacade.popLayer();
      }

   }

   /**
    * Compares {@link Resource} Ids considering legacy Ids without repositoryId prefix.<br>
    * The prefix pointing to the repositoryId {@link RepositoryProviderManager#SYSTEM_REPOSITORY_ID} is optional.
    * <p>
    * For example: <br>
    * '{urn:repository:default}{jcr-uuid}ABC' == '{jcr-uuid}ABC'.<br>
    * However: <br>
    * '{urn:repository:newRepository}{jcr-uuid}ABC' != '{jcr-uuid}ABC'
    */
   private boolean resourceIdEquals(String id1, String id2)
   {
      String repositoryId1 = RepositoryIdUtils.extractRepositoryId(id1);
      String repositoryId2 = RepositoryIdUtils.extractRepositoryId(id2);
      if (repositoryId1 == null && repositoryId2 != null && RepositoryProviderManager.SYSTEM_REPOSITORY_ID.equals(repositoryId2))
      {
         return id1.equals(RepositoryIdUtils.stripRepositoryId(id2));
      }
      else if (repositoryId1 != null && repositoryId2 == null && RepositoryProviderManager.SYSTEM_REPOSITORY_ID.equals(repositoryId1))
      {
         return RepositoryIdUtils.stripRepositoryId(id1).equals(id2);
      }
      else
      {
         return id1.equals(id2);
      }
      
   }

   public void folderChanged(Folder oldFolder, Folder newFolder)
   {
      // TODO to be implemented in later version
   }

}
