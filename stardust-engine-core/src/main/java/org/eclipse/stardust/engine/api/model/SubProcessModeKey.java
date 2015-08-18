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
package org.eclipse.stardust.engine.api.model;

import org.eclipse.stardust.common.StringKey;

/**
 * Enumeration of possible subprocess execution modes.
 * 
 * @author rsauer
 * @version $Revision$
 */
public class SubProcessModeKey extends StringKey
{
   private static final long serialVersionUID = 3561957360505308402L;

   /**
    * Subprocesses will be executed synchronously, means the calling activity will wait
    * for subprocess completion. The subprocess will operate in the calling process's data
    * space.
    */
   public static final SubProcessModeKey SYNC_SHARED = new SubProcessModeKey(
         "sync_shared", "Synchronous / Shared Data");

   /**
    * Subprocesses will be executed synchronously, means the calling activity will wait
    * for subprocess completion. The subprocess will operate in its own
    * independent data space. Dependent to the flag "CopyAllData" this data space will 
    * initially contain a full copy of data available to the triggering activity or by
    * defined data mappings.
    */
   public static final SubProcessModeKey SYNC_SEPARATE = new SubProcessModeKey(
         "sync_separate", "Synchronous / Separate Data");
   
   /**
    * Subprocesses will be executed asynchronously, means the calling activity will just
    * trigger the subprocess and then continue. The subprocess will operate in its own
    * independent data space, which will initially contain a full copy of data available
    * to the triggering activity.
    */
   public static final SubProcessModeKey ASYNC_SEPARATE = new SubProcessModeKey(
         "async_separate", "Asynchronous / Separate Data");

   /**
    * Obtain a key instance from an ID.
    * 
    * @param id
    *           The ID of the key to be retrieved.
    * @return The appropriate key, or <code>null</code> if the ID was invalid.
    */
   public static SubProcessModeKey getKey(String id)
   {
      return (SubProcessModeKey) getKey(SubProcessModeKey.class, id);
   }

   private SubProcessModeKey(String id, String defaultName)
   {
      super(id, defaultName);
   }
}