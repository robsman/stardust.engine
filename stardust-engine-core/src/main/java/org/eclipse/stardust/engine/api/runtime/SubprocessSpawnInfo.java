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
package org.eclipse.stardust.engine.api.runtime;

import java.io.Serializable;
import java.util.Map;

/**
 * Contains information needed to spawn a process instance.
 */
public class SubprocessSpawnInfo implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String processId;

   private boolean copyData;

   private Map<String, ?> data;

   /**
    * @param processId The process id.
    * @param copyData Specifies if data of the parent process instance should be copied.
    * @param data Allows specifying explicit data values which are set instead of copied.
    */
   public SubprocessSpawnInfo(String processId, boolean copyData, Map<String, ? > data)
   {
      super();
      this.processId = processId;
      this.copyData = copyData;
      this.data = data;
   }

   public String getProcessId()
   {
      return processId;
   }

   public boolean isCopyData()
   {
      return copyData;
   }

   public Map<String, ? > getData()
   {
      return data;
   }



}
