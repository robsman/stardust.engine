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

import org.eclipse.stardust.engine.api.model.PredefinedConstants;

/**
 * @author rsauer
 * @version $Revision$
 */
public class DmsConstants
{
   public static String DMS_SCOPE = PredefinedConstants.ENGINE_SCOPE + "dms:";

   public static String RESOURCE_METADATA_SCHEMA_ATT = DMS_SCOPE + "resourceMetadataSchema"; //$NON-NLS-1$

   public static String BY_REFERENCE_ATT = DMS_SCOPE + "byReference"; //$NON-NLS-1$

   public static final String SCOPE_VFS_OPERATION = DMS_SCOPE + "operation:";

   public static final String PRP_OPERATION_DMS_ID = SCOPE_VFS_OPERATION + "dmsId";

   public static final String PRP_OPERATION_NAME = SCOPE_VFS_OPERATION + "name";

   public static final String PRP_DMS_ID_SOURCE = SCOPE_VFS_OPERATION
      + "dmsIdSource";
   
   public static final String DMS_ID_SOURCE_DEFAULT = "default";
   
   public static final String DMS_ID_SOURCE_MODEL = "model";
   
   public static final String DMS_ID_SOURCE_RUNTIME = "runtime";

   public static final String PRP_RUNTIME_DEFINED_TARGET_FOLDER = SCOPE_VFS_OPERATION
      + "runtimeDefinedTargetFolder";

   public static final String PRP_RUNTIME_DEFINED_VERSIONING = SCOPE_VFS_OPERATION
      + "runtimeDefinedVersioning";
   
   public static final String DATA_TYPE_DMS_DOCUMENT = "dmsDocument";
   public static final String DATA_TYPE_DMS_FOLDER = "dmsFolder";
   public static final String DATA_TYPE_DMS_DOCUMENT_LIST = "dmsDocumentList";
   public static final String DATA_TYPE_DMS_FOLDER_LIST = "dmsFolderList";

   public static final String PATH_ID_ATTACHMENTS = "PROCESS_ATTACHMENTS";

   public static final String DATA_ID_ATTACHMENTS = "PROCESS_ATTACHMENTS";

   public static final String MONTAUK_SCHEMA_XSD = "org/eclipse/stardust/engine/extensions/dms/data/montauk-schema.xsd";

}
