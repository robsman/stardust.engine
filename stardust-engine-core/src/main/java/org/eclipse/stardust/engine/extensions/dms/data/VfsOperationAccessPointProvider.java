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
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.core.pojo.data.JavaDataTypeUtils;
import org.eclipse.stardust.engine.core.spi.extensions.model.AccessPointProvider;
import org.eclipse.stardust.engine.core.spi.extensions.runtime.ModelAware;


/**
 * @author rsauer
 * @version $Revision$
 */
public class VfsOperationAccessPointProvider implements AccessPointProvider, ModelAware
{

   public static final String AP_ID_DMS_ID = "dmsId";
   
   public static final String AP_ID_TARGET_FOLDER = "targetFolder";
   
   public static final String AP_ID_FOLDER = "folder";
   
   public static final String AP_ID_FOLDER_INFO = "folderInfo";
   
   public static final String AP_ID_DOCUMENT = "document";
   
   public static final String AP_ID_DOCUMENT_INFO = "documentInfo";
   
   public static final String AP_ID_VERSIONING = "versioning";
   
   public static final String AP_ID_EXPRESSION = "expression";

   public static final String AP_ID_DOCUMENT_LIST = "documentList";

   public static final String AP_ID_FOLDER_LIST = "folderList";

   public static final String AP_ID_DOCUMENT_ID = "documentId";
   
   public static final String AP_ID_FOLDER_ID = "folderId";
   
   public static final String AP_ID_DOCUMENT_IDS = "documentIds";
   
   public static final String AP_ID_FOLDER_IDS = "folderIds";

   public static final String AP_ID_VERSION_LABEL = "versionLabel";

   public static final String AP_ID_RECURSIVE = "recursive";
   
   private IModel model;
   
   public void setModel(IModel model)
   {
      this.model = model;
   }

   public Iterator createIntrinsicAccessPoints(Map context, Map typeAttributes)
   {
      final String dmsId = (String) context.get(DmsConstants.PRP_OPERATION_DMS_ID);
      
      if ( StringUtils.isEmpty(dmsId))
      {
         // TODO (post 4.6) dynamically define DMS ID
      }

      final DmsOperation operation = DmsOperation.fromId((String) context.get(DmsConstants.PRP_OPERATION_NAME));
      final Object runtimeDefinedTargetFolder = context.get(DmsConstants.PRP_RUNTIME_DEFINED_TARGET_FOLDER);
      
      List result = CollectionUtils.newLinkedList();
      if (DmsOperation.OP_CREATE_FOLDER == operation)
      {
         if (Boolean.TRUE.equals(runtimeDefinedTargetFolder))
         {
            result.add(new DmsFolderAccessPoint(AP_ID_TARGET_FOLDER, Direction.IN, model));
         }
         result.add(new DmsFolderAccessPoint(AP_ID_FOLDER, Direction.IN_OUT, model));
      }
      else if (DmsOperation.OP_FIND_FOLDERS == operation)
      {
         result.add(new DmsFolderAccessPoint(AP_ID_FOLDER_INFO, Direction.IN, model));
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_EXPRESSION, null,
               String.class.getName(), Direction.IN, true, null));
         result.add(new DmsFolderListAccessPoint(AP_ID_FOLDER_LIST, Direction.OUT, model));

      }
      else if (DmsOperation.OP_REMOVE_FOLDER == operation)
      {
         result.add(new DmsFolderAccessPoint(AP_ID_FOLDER, Direction.IN, model));
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_RECURSIVE, null,
               Boolean.class.getName(), Direction.IN, true, null));
      }
      else if (DmsOperation.OP_ADD_DOCUMENT == operation)
      {
         if (Boolean.TRUE.equals(runtimeDefinedTargetFolder))
         {
            result.add(new DmsFolderAccessPoint(AP_ID_TARGET_FOLDER, Direction.IN, model));
         }
         result.add(new DmsVersioningAccessPoint(AP_ID_VERSIONING, Direction.IN, model));
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT, Direction.IN_OUT, model));
      }
      else if (DmsOperation.OP_FIND_DOCUMENTS == operation)
      {
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT_INFO, Direction.IN, model));
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_EXPRESSION, null,
               String.class.getName(), Direction.IN, true, null));
         result.add(new DmsDocumentListAccessPoint(AP_ID_DOCUMENT_LIST, Direction.OUT, model));
      }
      else if ((DmsOperation.OP_UPDATE_DOCUMENT == operation))
      {
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT, Direction.IN_OUT, model));
         result.add(new DmsVersioningAccessPoint(AP_ID_VERSIONING, Direction.IN, model));
      }
      else if ((DmsOperation.OP_UPDATE_FOLDER == operation))
      {
         result.add(new DmsFolderAccessPoint(AP_ID_FOLDER, Direction.IN_OUT, model));
      }
      else if ((DmsOperation.OP_REMOVE_DOCUMENT == operation))
      {
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT, Direction.IN, model));
      }
      else if (DmsOperation.OP_GET_DOCUMENT == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_DOCUMENT_ID, null,
               String.class.getName(), Direction.IN, true, null));
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT, Direction.OUT, model));
      }
      else if (DmsOperation.OP_GET_FOLDER == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_FOLDER_ID, null,
               String.class.getName(), Direction.IN, true, null));
         result.add(new DmsFolderAccessPoint(AP_ID_FOLDER, Direction.OUT, model));
      }
      else if (DmsOperation.OP_GET_DOCUMENTS == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_DOCUMENT_IDS, null,
               List.class.getName(), Direction.IN, true, null));
         result.add(new DmsDocumentListAccessPoint(AP_ID_DOCUMENT_LIST, Direction.OUT, model));
      }
      else if (DmsOperation.OP_GET_FOLDERS == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_FOLDER_IDS, null,
               List.class.getName(), Direction.IN, true, null));
         result.add(new DmsFolderListAccessPoint(AP_ID_FOLDER_LIST, Direction.OUT, model));
      }
      else if (DmsOperation.OP_LOCK_DOCUMENT == operation
            || DmsOperation.OP_UNLOCK_DOCUMENT == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_DOCUMENT_ID, null,
               String.class.getName(), Direction.IN, true, null));
      }
      else if (DmsOperation.OP_LOCK_FOLDER == operation
            || DmsOperation.OP_UNLOCK_FOLDER == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_FOLDER_ID, null,
               String.class.getName(), Direction.IN, true, null));
      }
      else if (DmsOperation.OP_VERSION_DOCUMENT == operation)
      {
         result.add(JavaDataTypeUtils.createIntrinsicAccessPoint(AP_ID_VERSION_LABEL, null,
               String.class.getName(), Direction.IN, true, null));
         result.add(new DmsDocumentAccessPoint(AP_ID_DOCUMENT, Direction.IN_OUT, model));
      }

      return result.iterator();
   }

}
