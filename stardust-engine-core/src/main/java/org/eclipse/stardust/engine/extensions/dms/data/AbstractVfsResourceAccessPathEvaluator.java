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

import org.eclipse.stardust.common.StringUtils;
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