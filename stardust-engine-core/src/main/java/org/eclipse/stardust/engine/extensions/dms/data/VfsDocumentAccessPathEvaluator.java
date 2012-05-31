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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Stateless;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.IData;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;



/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsDocumentAccessPathEvaluator
      extends AbstractVfsResourceAccessPathEvaluator
      implements ExtendedAccessPathEvaluator, Stateless
{

   private static final Logger trace = LogManager.getLogger(VfsDocumentAccessPathEvaluator.class);

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext,
         Object value)
   {
      // TODO test

      if (StringUtils.isEmpty(inPath))
      {
         if (value == null)
         {
            // special case - remove the value
            Map auditTrailDocument = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, null, accessPathEvaluationContext);
            if (auditTrailDocument != null)
            {
               // update only if current value is not null
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext, null);
            }
         }
         else if (value instanceof Long)
         {
            // we have received the oid of an existing data, make a duplicate
            Map auditTrailDoc = (Map) readFromAuditTrail(accessPointDefinition, value,
                  null, accessPathEvaluationContext);
            accessPointInstance = writeToAuditTrail(accessPointDefinition, null, null,
                  accessPathEvaluationContext, auditTrailDoc);
         }
         else if (value instanceof Document)
         {
            // fully updating the document

            Map auditTrailDoc = null;
            if (accessPointInstance instanceof DmsDocumentBean)
            {
               auditTrailDoc = ((DmsDocumentBean) accessPointInstance).vfsResource();
            }
            else if (accessPointInstance instanceof Long)
            {
               // load snapshot from audit trail
               auditTrailDoc = (Map) readFromAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext);
            }
            if (null == auditTrailDoc)
            {
               auditTrailDoc = CollectionUtils.newMap();
            }

            // update audit trail, only if needed
            Document document = (Document) value;
            // infer document type.
            if (accessPointDefinition instanceof IData)
            {
               IData data = (IData) accessPointDefinition;
               DocumentType documentType = DocumentTypeUtils.inferDocumentType(data);
               if (documentType != null)
               {
                  document.setDocumentType(documentType);                  
               }
            }
            
            syncToRepository(document, trace);

            Map newAuditTrailDocument = ((DmsDocumentBean) value).vfsResource();
            if ( !auditTrailDoc.equals(newAuditTrailDocument))
            {
               
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     null, null, accessPathEvaluationContext,
                     newAuditTrailDocument);
            }
         }
         else
         {
            throw new PublicException("Unsupported value: " + value);
         }

         return accessPointInstance;
      }
      else
      {
         // partially updating the document
         
         // write value into audit trail
         accessPointInstance = writeToAuditTrail(accessPointDefinition,
               accessPointInstance, inPath, accessPathEvaluationContext, value);

         // read and sync full document including updated value
         Map auditTrailDoc = (Map) readFromAuditTrail(accessPointDefinition,
               accessPointInstance, null, accessPathEvaluationContext);
         syncToRepository(new DmsDocumentBean(auditTrailDoc), trace);
         
      }

      return accessPointInstance;
   }

   public Object createDefaultValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }

   public Object createInitialValue(AccessPoint accessPointDefinition,
         AccessPathEvaluationContext accessPathEvaluationContext)
   {
      return null;
   }

   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String outPath, AccessPathEvaluationContext accessPathEvaluationContext)
   {
      if (null == accessPointInstance)
      {
         trace.debug("returning null for outPath '" + outPath + "'");
         return null;
      }

      if (accessPointDefinition instanceof DmsDocumentAccessPoint
            || (accessPointInstance != null && accessPointInstance instanceof DmsDocumentBean))
      {
         if (StringUtils.isEmpty(outPath))
         {
            return (DmsDocumentBean) accessPointInstance;
         }
         else
         {
            throw new InternalException(
                  "TODO: non-empty out access paths are not supported yet");
         }
      }
      else
      {
         if (StringUtils.isEmpty(outPath))
         {
            final Map auditTrailDoc = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, outPath, accessPathEvaluationContext);

            if (auditTrailDoc == null || auditTrailDoc.isEmpty())
            {
               return null;
            }

            return new DmsDocumentBean(auditTrailDoc);
         }
         else
         {
            return readFromAuditTrail(accessPointDefinition, accessPointInstance,
                  outPath, accessPathEvaluationContext);
         }
      }
   }

   public boolean isStateless()
   {
      return true;
   }
}
