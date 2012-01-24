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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPoint;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.AccessPathEvaluationContext;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ExtendedAccessPathEvaluator;


/**
 * Access path evaluator for folder lists. 
 */
public class VfsFolderListAccessPathEvaluator extends AbstractVfsResourceAccessPathEvaluator implements ExtendedAccessPathEvaluator
{

   private static final Logger trace = LogManager.getLogger(VfsFolderListAccessPathEvaluator.class);

   private static final String XPATH_PREFIX = AuditTrailUtils.FOLDERS_FOLDERS+"/";

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
            Map auditTrailFolderList = (Map) readFromAuditTrail(
                  accessPointDefinition, accessPointInstance, null,
                  accessPathEvaluationContext);
            if (null != auditTrailFolderList)
            {
               // update only if current value is not null
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext, null, XPATH_PREFIX);
            }
         }
         else if (value instanceof Long)
         {
            // we have received the oid of an existing data, make a duplicate
            Map auditTrailFolderList = (Map) readFromAuditTrail(accessPointDefinition,
                  value, null, accessPathEvaluationContext);
            accessPointInstance = writeToAuditTrail(accessPointDefinition,
                  null, null, accessPathEvaluationContext,
                  auditTrailFolderList, XPATH_PREFIX);
         }
         else if (value instanceof List)
         {
            // fully updating the document list

            // load snapshot from audit trail
            Map auditTrailFolderList = (Map) readFromAuditTrail(
                  accessPointDefinition, accessPointInstance, null,
                  accessPathEvaluationContext);
            if (null == auditTrailFolderList)
            {
               auditTrailFolderList = CollectionUtils.newMap();
            }

            // update audit trail, only if needed

            // build lego structure from List<IDmsFolder>
            Map newAuditTrailFolderList = CollectionUtils.newHashMap();
            List legoFolders = CollectionUtils.newLinkedList();
            newAuditTrailFolderList.put(AuditTrailUtils.FOLDERS_FOLDERS, legoFolders);
            for (Iterator i = ((List) value).iterator(); i.hasNext();)
            {
               DmsFolderBean dmsFolder = (DmsFolderBean) i.next();
               legoFolders.add(dmsFolder.vfsResource());
            }

            if ( !auditTrailFolderList.equals(newAuditTrailFolderList))
            {
               accessPointInstance = writeToAuditTrail(accessPointDefinition,
                     accessPointInstance, null, accessPathEvaluationContext,
                     newAuditTrailFolderList, XPATH_PREFIX);
            }
         }
         else
         {
            throw new PublicException("Unsupported value: " + value);
         }
      }
      else
      {
         // partially updating the folder

         // for the case of data mappings to the folders (e.g. folders[1])
         // convert Folder to Map
         if (value instanceof DmsFolderBean)
         {
            value = ((DmsFolderBean)value).vfsResource();
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
      if (null == accessPointInstance)
      {
         trace.debug("returning null for outPath '" + outPath + "'");
         return null;
      }

      if (accessPointDefinition instanceof DmsFolderListAccessPoint)
      {
         if (StringUtils.isEmpty(outPath))
         {
            return (List/*<IDmsFolder>*/) accessPointInstance;
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
            final Map auditTrailFolderList = (Map) readFromAuditTrail(
                  accessPointDefinition, accessPointInstance, outPath,
                  accessPathEvaluationContext);

            if (auditTrailFolderList == null
                  || !auditTrailFolderList.containsKey(AuditTrailUtils.FOLDERS_FOLDERS))
            {
               return null;
            }

            List /*<IDmsFolder>*/folderList = CollectionUtils.newLinkedList();
            List legoFolders = (List) auditTrailFolderList.get(AuditTrailUtils.FOLDERS_FOLDERS);
            for (Iterator i = legoFolders.iterator(); i.hasNext();)
            {
               Map legoFolder = (Map) i.next();
               folderList.add(new DmsFolderBean(legoFolder));
            }

            return folderList;
         }
         else
         {
            return readFromAuditTrail(accessPointDefinition, accessPointInstance,
                  outPath, accessPathEvaluationContext);
         }
      }
   }
}
