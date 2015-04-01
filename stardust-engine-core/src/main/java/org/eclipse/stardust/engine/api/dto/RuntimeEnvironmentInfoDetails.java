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
package org.eclipse.stardust.engine.api.dto;

import org.eclipse.stardust.common.config.CurrentVersion;
import org.eclipse.stardust.common.config.Version;
import org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;

public class RuntimeEnvironmentInfoDetails implements RuntimeEnvironmentInfo
{
   private static final long serialVersionUID = 1L;

   private Long lastArchivingTime;

   private String auditTrailUUID;

   private String auditTrailName;

   private final Version version;

   public RuntimeEnvironmentInfoDetails()
   {
      super();
      this.version = CurrentVersion.getBuildVersion();

      this.auditTrailUUID = SchemaHelper.getAuditTrailProperty(RuntimeEnvironmentInfo.AUDITTRAIL_UUID);
      this.auditTrailName = SchemaHelper.getAuditTrailProperty(RuntimeEnvironmentInfo.AUDITTRAIL_NAME);

      if (SchemaHelper.getAuditTrailProperty(RuntimeEnvironmentInfo.AUDITTRAIL_ARCHIVING_TIMESTAMP) != null)
      {
         this.lastArchivingTime = Long.parseLong(SchemaHelper.getAuditTrailProperty(RuntimeEnvironmentInfo.AUDITTRAIL_ARCHIVING_TIMESTAMP));
      }
      else
      {
         this.lastArchivingTime = null;
      }

   }

   public Version getVersion()
   {
      return version;
   }

   public Long getLastArchivingTime()
   {
      return this.lastArchivingTime;
   }

   public String getAuditTrailUUID()
   {
      return this.auditTrailUUID;
   }

   public String getAuditTrailName()
   {
      // TODO Auto-generated method stub
      return this.auditTrailName;
   }

}
