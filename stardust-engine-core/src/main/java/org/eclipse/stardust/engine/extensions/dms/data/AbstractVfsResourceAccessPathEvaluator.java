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

import java.util.Map;

import javax.jcr.AccessDeniedException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryInstance;
import org.eclipse.stardust.engine.core.spi.dms.IRepositoryService;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryManager;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


/**
 * Handles common tasks of property format for DmsResource
 */
public abstract class AbstractVfsResourceAccessPathEvaluator
{

   /**
    * Parameter key used to prevent cyclic updates. It is checked in the workflow code
    * which handles AuditTrail persistence of document data. If it is true, the update
    * call synchronizing changes to the repository must not be triggered to prevent a call cycle.
    */
   public final static String IS_INTERNAL_DOCUMENT_SYNC_CALL = "org.eclipse.stardust.engine.extensions.dms.data.AbstractVfsResourceAccessPathEvaluator.isInternalDocumentSyncCall";

   /**
    * Holds the current accesspoint which triggered the update call if the sync was triggered via an access point.
    * This accesspoint has to be ignored while synchronizing because it is updated by the call itself and the data value bean is not be created yet.
    */
   public final static String DMS_SYNC_CURRENT_ACCESS_POINT = "org.eclipse.stardust.engine.extensions.dms.data.AbstractVfsResourceAccessPathEvaluator.currentAccessPoint";


   private final StructuredDataXPathEvaluator structEvaluator = new StructuredDataXPathEvaluator();

   public AbstractVfsResourceAccessPathEvaluator()
   {
      super();
   }

   protected void syncToRepository(Document document, Logger trace,
         AccessPoint accessPointDefinition)
   {
      boolean isInternalDocumentSyncCall = Parameters.instance().getBoolean(
            IS_INTERNAL_DOCUMENT_SYNC_CALL, false);
      String documentId = document.getId();
      if ( !isInternalDocumentSyncCall && document != null && documentId != null)
      {
         Map<String, Object> props = CollectionUtils.newHashMap();
         props.put(DMS_SYNC_CURRENT_ACCESS_POINT, accessPointDefinition);
         PropertyLayer layer = ParametersFacade.pushLayer(props);
         try
         {

            IUser user = SecurityProperties.getUser();

            if (user == null)
            {
               Parameters.instance().set(SecurityProperties.CURRENT_USER,
                     TransientUser.getInstance());
            }

            RepositoryManager repositoryProviderManager = RepositoryManager.getInstance();
            String repositoryId = RepositoryIdUtils.extractRepositoryId(documentId);
            Document prefixedDocument = document;
            // synchronization for legacy documents has to point to RepositoryProviderManager.DEFAULT_REPOSITORY_ID.
            if (repositoryId == null)
            {
               repositoryId = RepositoryManager.SYSTEM_REPOSITORY_ID;
               documentId = RepositoryIdUtils.addRepositoryId(documentId, repositoryId);
               prefixedDocument = RepositoryIdUtils.addRepositoryId(document, repositoryId);
            }

            IRepositoryInstance repositoryInstance = repositoryProviderManager
                  .getExplicitInstance(repositoryId);
            boolean synchronizationSupported = repositoryInstance.getRepositoryInstanceInfo()
                  .isWriteSupported();

            if (synchronizationSupported)
            {
               IRepositoryService dms = repositoryProviderManager.getImplicitService();
               try
               {
                  Document existingDocument = dms.getDocument(documentId);
                  if (existingDocument != null && !existingDocument.equals(prefixedDocument))
                  {
                     mergeNonUpdatableProperties(prefixedDocument, existingDocument);

                     dms.updateDocument(prefixedDocument, false, null, null, false);
                  }
               }
               catch (Exception e)
               {
                  Throwable cause = e.getCause();
                  for (int i = 0; i < 5; i++ )
                  {
                     if (cause != null)
                     {
                        if (cause instanceof AccessDeniedException)
                        {
                           trace.error("Synchronization of document data to repository failed.", e);
                           throw new AccessForbiddenException(
                                 BpmRuntimeError.BPMRT_DMS_DOCUMENT_DATA_SYNC_FAILED.raise(documentId));
                        }
                        cause = cause.getCause();
                     }

                  }
                  trace.error("Synchronization of document data to repository failed.", e);
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_DMS_DOCUMENT_DATA_SYNC_FAILED.raise(documentId));
               }
            }
         }
         finally
         {
            if (layer != null)
            {
               ParametersFacade.popLayer();
            }
         }
      }
   }

   private void mergeNonUpdatableProperties(Document document, Document existingDocument)
   {
      // DocumentAnnotations are not persisted in audit trail
      document.setDocumentAnnotations(existingDocument.getDocumentAnnotations());
   }

   protected Object readFromAuditTrail(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      Object legoResource = structEvaluator.evaluate(accessPointDefinition,
            accessPointInstance, inPath, accessPathEvaluationContext);
      if (StringUtils.isEmpty(inPath))
      {
         // convert list of name->value (generic properties) to map format, if needed
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(DmsPropertyFormatter.AS_MAP, null);
         propertyFormatter.visit((Map)legoResource, "");
      }
      return legoResource;
   }

   protected Object writeToAuditTrail(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath,
         AccessPathEvaluationContext accessPathEvaluationContext, Object value)
   {
      return writeToAuditTrail(accessPointDefinition, accessPointInstance, inPath,
            accessPathEvaluationContext, value, "");
   }

   protected Object writeToAuditTrail(AccessPoint accessPointDefinition, Object accessPointInstance, String inPath,
         AccessPathEvaluationContext accessPathEvaluationContext, Object value, String xPathPrefix)
   {
      if (StringUtils.isEmpty(inPath))
      {
         String excludeXPath = null;
         if ( !hasDefaultMetadataSchema(accessPointDefinition))
         {
            // do not convert properties of the document to a list form, since custom metadata is used
            excludeXPath = xPathPrefix+AuditTrailUtils.RES_PROPERTIES;
         }
         // (fh) we make a copy of the map since the property formatter is modifying the original map.
         // this is required for pojo scenarios where the value is the actual client object.
         if (value != null)
         {
            value = CollectionUtils.copyMap((Map) value);
         }
         DmsPropertyFormatter propertyFormatter = new DmsPropertyFormatter(DmsPropertyFormatter.AS_LIST, excludeXPath);
         propertyFormatter.visit((Map)value, "");
      }

      return structEvaluator.evaluate(accessPointDefinition,
            accessPointInstance, inPath, accessPathEvaluationContext,
            value);
   }

   private boolean hasDefaultMetadataSchema(AccessPoint accessPointDefinition)
   {
      String metadataComplexTypeName = (String)accessPointDefinition.getAttribute(DmsConstants.RESOURCE_METADATA_SCHEMA_ATT);
      if (StringUtils.isEmpty(metadataComplexTypeName))
      {
         return true;
      }
      else
      {
         return false;
      }
   }


}