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
package org.eclipse.stardust.engine.core.upgrade.jobs.m30;

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DataMapping extends ModelElement
{
   private String id;
   private String dataID;
   private String direction;
   private String context;
   private String applicationAccessPointId;
   private String dataPath;
   private String applicationPath;

   public DataMapping(String id, String dataID, String direction, String context,
         String applicationAccessPointId, String dataPath, String applicationPath, int oid, Model model)
   {
      this.id = id;
      this.dataID = dataID;
      this.direction = direction;
      this.context = context;
      this.applicationAccessPointId = applicationAccessPointId;
      this.dataPath = dataPath;
      this.applicationPath = applicationPath;
      model.register(this, oid);
   }

   public String getApplicationAccessPointId()
   {
      return applicationAccessPointId;
   }

   public String getApplicationPath()
   {
      return applicationPath;
   }

   public String getContext()
   {
      return context;
   }

   public String getDataID()
   {
      return dataID;
   }

   public String getDataPath()
   {
      return dataPath;
   }

   public String getDirection()
   {
      return direction;
   }

   public String getId()
   {
      return id;
   }
}
