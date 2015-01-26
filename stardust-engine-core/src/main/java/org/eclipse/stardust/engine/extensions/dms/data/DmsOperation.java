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

import org.eclipse.stardust.common.StringKey;

public class DmsOperation extends StringKey
{
  
   private static final long serialVersionUID = 1L;

   public static final DmsOperation OP_CREATE_FOLDER = new DmsOperation("createFolder");

   public static final DmsOperation OP_FIND_FOLDERS = new DmsOperation("findFolders");

   public static final DmsOperation OP_REMOVE_FOLDER = new DmsOperation("removeFolder");

   public static final DmsOperation OP_ADD_DOCUMENT = new DmsOperation("addDocument");

   public static final DmsOperation OP_FIND_DOCUMENTS = new DmsOperation("findDocuments");

   public static final DmsOperation OP_UPDATE_DOCUMENT = new DmsOperation("updateDocument");

   public static final DmsOperation OP_REMOVE_DOCUMENT = new DmsOperation("removeDocument");

   public static final DmsOperation OP_GET_DOCUMENT = new DmsOperation("getDocument");

   public static final DmsOperation OP_GET_DOCUMENTS = new DmsOperation("getDocuments");

   public static final DmsOperation OP_GET_FOLDER = new DmsOperation("getFolder");
   
   public static final DmsOperation OP_GET_FOLDERS = new DmsOperation("getFolders");

   public static final DmsOperation OP_VERSION_DOCUMENT = new DmsOperation("versionDocument");
   
   public static final DmsOperation OP_LOCK_DOCUMENT = new DmsOperation("lockDocument");
   
   public static final DmsOperation OP_UNLOCK_DOCUMENT = new DmsOperation("unlockDocument");

   public static final DmsOperation OP_UPDATE_FOLDER = new DmsOperation("updateFolder");

   public static final DmsOperation OP_UNLOCK_FOLDER = new DmsOperation("unlockFolder");
   
   public static final DmsOperation OP_LOCK_FOLDER = new DmsOperation("lockFolder");

   public DmsOperation(String id)
   {
      super(id, id);
   }

   public static DmsOperation fromId(String id)
   {
      return (DmsOperation) StringKey.getKey(DmsOperation.class, id);
   }
}

