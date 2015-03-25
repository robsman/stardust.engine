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

import org.eclipse.stardust.common.config.Version;


/**
 * The <code>RuntimeEnvironmentInfo</code> represents a snapshot about information for the runtime environment.
 *
 * @author stephan.born
 * @version $Revision: $
 */
public interface RuntimeEnvironmentInfo extends Serializable
{
   
   public static final String AUDITTRAIL_UUID = "stardust.audittrail.uuid";
   public static final String AUDITTRAIL_NAME = "stardust.audittrail.name";
   public static final String AUDITTRAIL_ARCHIVING_TIMESTAMP = "stardust.audittrail.archivingtimestamp";
   
   /**
    * The version of the kernel.
    *  
    * @return version of the kernel.
    */
   public Version getVersion();
   
   /**
    * Timestamp of last target archive time.
    * 
    * @return timestamp last target archive time 
    */
   public long getLastArchivingTime();
   
   /**
    * The UUID of the AuditTrail.
    * 
    * @return
    */
   public String getAuditTrailUUID();
   
   /**
    * The name of the AuditTrail.
    * 
    * @return
    */
   public String getAuditTrailName();
         
}
