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

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.RepositoryMigrationJobInfo;

import org.eclipse.stardust.vfs.IMigrationJobInfo;


public class DmsMigrationJobInfoBean implements RepositoryMigrationJobInfo, Serializable
{
   private static final long serialVersionUID = 1L;

   private String description;

   private int fromVersion;

   private int toVersion;

   private String name;

   public DmsMigrationJobInfoBean(IMigrationJobInfo currentMigrationJobInfo)
   {
      this.description = currentMigrationJobInfo.getDescription();
      this.fromVersion = currentMigrationJobInfo.getFromVersion();
      this.toVersion = currentMigrationJobInfo.getToVersion();
      this.name = currentMigrationJobInfo.getName();
   }

   public DmsMigrationJobInfoBean(String name, String description, int fromVersion,
         int toVersion)
   {
      super();
      this.name = name;
      this.description = description;
      this.fromVersion = fromVersion;
      this.toVersion = toVersion;
   }



   public String getDescription()
   {
      return description;
   }

   public int getFromVersion()
   {
      return fromVersion;
   }

   public int getToVersion()
   {
      return toVersion;
   }

   public String getName()
   {
      return name;
   }

}
