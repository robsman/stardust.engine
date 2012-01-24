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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;


/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsFolderAccessPathEvaluator extends AbstractVfsResourceAccessPathEvaluator implements ExtendedAccessPathEvaluator
{

   private static final Logger trace = LogManager.getLogger(VfsFolderAccessPathEvaluator.class);
   
   public Object evaluate(AccessPoint accessPointDefinition, Object accessPointInstance,
         String inPath, AccessPathEvaluationContext accessPathEvaluationContext,
         Object value)
   {
      if (StringUtils.isEmpty(inPath))
      {
         // fully updating the folder
         if (value == null)
         {
            // special case - remove the value 
            Map auditTrailFolder = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, null, accessPathEvaluationContext);
            if (auditTrailFolder != null)
            {
               // update only if current value is not null
               accessPointInstance = writeToAuditTrail(accessPointDefinition, accessPointInstance, null,
                     accessPathEvaluationContext, null);
            }
         }
         else if (value instanceof Long)
         {
            // we have received the oid of an existing data, make a duplicate
            Map auditTrailFolder = (Map) readFromAuditTrail(accessPointDefinition,
                  value, null, accessPathEvaluationContext);
            accessPointInstance = writeToAuditTrail(accessPointDefinition,
                  null, null, accessPathEvaluationContext,
                  auditTrailFolder);
         }
         else if (value instanceof Folder)
         {
            // load snapshot from audit trail
            Map auditTrailFolder = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, null, accessPathEvaluationContext);
            if (null == auditTrailFolder)
            {
               auditTrailFolder = CollectionUtils.newMap();
            }
            
            Map newAuditTrailFolder = ((DmsFolderBean)value).vfsResource();
            if ( !auditTrailFolder.equals(newAuditTrailFolder))
            {
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext,
                     newAuditTrailFolder);
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
         // partially updating the folder

         // write value into audit trail
         accessPointInstance = writeToAuditTrail(accessPointDefinition,
               accessPointInstance, inPath, accessPathEvaluationContext, value);
         /*
         // read updated value
         Map auditTrailFolder = (Map) structEvaluator.evaluate(accessPointDefinition,
               accessPointInstance, null, accessPathEvaluationContext);
         
         // write document into VFS, yields a snapshot of the updated state
         final String folderId = (String) auditTrailFolder.get(AuditTrailUtils.RES_ID);
         
         if ( !StringUtils.isEmpty(folderId))
         {
            // update folder in vfs, retrieve snapshot of new state
            boolean snaphsotContainsUpdates = vfsMediator.writeFolderToVfs(auditTrailFolder, metadataComplexTypeName);
            if (snaphsotContainsUpdates)
            {
               // update audit trail with VFS snapshot
               
               accessPointInstance = structEvaluator.evaluate(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext,
                     auditTrailFolder);
            }
         }
         */
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
      
      if (accessPointDefinition instanceof DmsFolderAccessPoint)
      {
         if (StringUtils.isEmpty(outPath))
         {
            return (DmsFolderBean)accessPointInstance;
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
            final Map auditTrailFolder = (Map) readFromAuditTrail(accessPointDefinition,
                  accessPointInstance, outPath, accessPathEvaluationContext);

            if (auditTrailFolder == null)
            {
               return null;
            }

            return new DmsFolderBean(auditTrailFolder);
         }
         else
         {
            return readFromAuditTrail(accessPointDefinition, accessPointInstance,
                  outPath, accessPathEvaluationContext);
         }
      }
   }

}
