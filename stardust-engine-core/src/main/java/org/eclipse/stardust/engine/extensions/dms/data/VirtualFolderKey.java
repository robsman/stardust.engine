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

public class VirtualFolderKey extends StringKey
{

   public static final VirtualFolderKey PARTITION_FOLDER = new VirtualFolderKey(
         "{uid:nonexistent-root-folder}", "partitionFolder");

   public static final VirtualFolderKey PARTITION_PREFRERENCES_FOLDER = new VirtualFolderKey(
         "{uid:nonexistent-partition-preferences-folder}", "partitionPreferencesFolder");

   public static final VirtualFolderKey PARTITION_PROCESS_INSTANCES_FOLDER = new VirtualFolderKey(
         "{uid:nonexistent-partition-process-instances-folder}",
         "partitionProcessInstancesFolder");

   public static final VirtualFolderKey PARTITION_DOCUMENTS_FOLDER = new VirtualFolderKey(
         "{uid:nonexistent-partition-documents-folder}", "partitionDocumentsFolder");

   public static final VirtualFolderKey USER_FOLDER = new VirtualFolderKey(
         "{uid:nonexistent-user-folder}", "userFolder");

   private static final long serialVersionUID = 1L;

   public VirtualFolderKey(String jcrUidString, String id)
   {
      super(id, jcrUidString);

   }

}
