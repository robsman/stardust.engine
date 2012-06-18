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
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.EmbeddedServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.struct.spi.StructuredDataXPathEvaluator;


/**
 * Handles common tasks of property format for DmsResource
 */
public abstract class AbstractVfsResourceAccessPathEvaluator
{

   private final StructuredDataXPathEvaluator structEvaluator = new StructuredDataXPathEvaluator();

   public AbstractVfsResourceAccessPathEvaluator()
   {
      super();
   }
   
   protected void syncToRepository(Document document, Logger trace)
   {
      boolean isInternalDocumentSyncCall = Parameters.instance().getBoolean(
            DmsResourceSyncManager.IS_INTERNAL_DOCUMENT_SYNC_CALL, false);
      if ( !isInternalDocumentSyncCall && document != null && document.getId() != null)
      {
         try
         {
            ParametersFacade.pushLayer(null);

            IUser user = SecurityProperties.getUser();      
            
            if (user == null)
            {
               Parameters.instance().set(SecurityProperties.CURRENT_USER, TransientUser.getInstance());
            }
            
            DocumentManagementService dms = null;
            try
            {
               ServiceFactory sf = EmbeddedServiceFactory.CURRENT_TX();
               dms = sf.getDocumentManagementService();
            }
            catch (Throwable t)
            {
               trace.warn("Document data to repository synchronization failed.", t);
            }

            if (dms != null)
            {
               try
               {
                  Document existingDocument = dms.getDocument(document.getId());
                  if (existingDocument != null && !existingDocument.equals(document));
                  {
                     dms.updateDocument(document, false, null, null, false);                     
                  }
               }
               catch (Exception e)
               {
                  trace.error("Synchronization of document data to repository failed.", e);
                  Throwable cause = e.getCause();
                  for (int i = 0; i < 5; i++ )
                  {
                     if (cause != null)
                     {
                        if (cause instanceof AccessDeniedException)
                        {
                           throw new AccessForbiddenException(
                                 BpmRuntimeError.BPMRT_DMS_DOCUMENT_DATA_SYNC_FAILED.raise(document.getId()));
                        }
                        cause = cause.getCause();
                     }

                  }
                  throw new PublicException(
                        BpmRuntimeError.BPMRT_DMS_DOCUMENT_DATA_SYNC_FAILED.raise(document.getId()));
               }
            }
         }
         finally
         {
            ParametersFacade.popLayer();
         }
      }
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