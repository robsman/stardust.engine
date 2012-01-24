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
package org.eclipse.stardust.engine.core.struct;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * Constants for structured data type. Defined in this separate class
 * since it is planned to separate structured data classes into a 
 * plugin
 */
public final class StructuredDataConstants
{
   // attributes of structured data definition

   // data type id
   public static final String STRUCTURED_DATA = "struct";
   
   // prefix for ExternalReferences into model schemas
   public static final String URN_INTERNAL_PREFIX = "urn:internal:";   
   
   // attribute that maps an ExternalReference location to a local file. 
   public static final String RESOURCE_MAPPING_SCOPE = PredefinedConstants.ENGINE_SCOPE + "resource:mapping:";

   public static final String RESOURCE_MAPPING_LOCAL_FILE = RESOURCE_MAPPING_SCOPE + "localFile";

   public static final String TYPE_DECLARATION_ATT = PredefinedConstants.ENGINE_SCOPE + "dataType";
   
   public static final String TRANSFORMATION_ATT = PredefinedConstants.ENGINE_SCOPE + "transformation";

   public static final String EXTERNAL_ANNOTATIONS_ATT = RESOURCE_MAPPING_SCOPE + "annotations";

   public static final String ACCESS_PATH_SEGMENT_SEPARATOR = "/";

   
   // no instantiation alowed
   private StructuredDataConstants() {}

}
