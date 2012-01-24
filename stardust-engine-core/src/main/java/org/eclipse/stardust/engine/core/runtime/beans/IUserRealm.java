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
package org.eclipse.stardust.engine.core.runtime.beans;

/**
 *
 */
public interface IUserRealm extends AttributedIdentifiablePersistent
{
   String LAST_LOGIN_TIMESTAMP_PROPERTY = "LastLoginTimestamp";
   String LAST_FAILED_LOGIN_TIMESTAMP_PROPERTY = "LastFailedLoginTimestamp";
   String FAILED_LOGIN_RETRIES_COUNT_PROPERTY = "FailedLoginRetriesCount";

   /**
    *
    */
   String getId();

   /**
    *
    */
   void setId(String id);

   /**
    *
    */
   String getName();

   /**
    *
    */
   void setName(String name);

   /**
    *
    */
   String getDescription();

   /**
    *
    */
   void setDescription(String description);
   
   public IAuditTrailPartition getPartition();
}
