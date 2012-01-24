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

import org.eclipse.stardust.common.IntKey;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;


/**
 * Represents the profile scope. Used in 
 * ({@link AdministrationService#getProfile(ProfileScope)} and
 * ({@link AdministrationService#setProfile(ProfileScope, java.util.Map)}.
 *
 * @author sborn
 * @version $Revision: $
 */
public class ProfileScope extends IntKey
{
   private static final long serialVersionUID = 1L;
   
   private static final int GLOBAL_SCOPE = 1;
   private static final int PARTITION_SCOPE = 2;
   private static final int DOMAIN_SCOPE = 3;
   private static final int USER_SCOPE = 4;

   /**
    * Global scope.
    */
   public static final ProfileScope GlobalScope = new ProfileScope(GLOBAL_SCOPE,
         "Global Scope");
   /**
    * Partition scope.
    */
   public static final ProfileScope PartitionScope = new ProfileScope(PARTITION_SCOPE,
         "Partition Scope");
   /**
    * Domain scope.
    */
   public static final ProfileScope DomainScope = new ProfileScope(DOMAIN_SCOPE,
         "Domain Scope");
   /**
    * User scope.
    */
   public static final ProfileScope UserScope = new ProfileScope(USER_SCOPE, "User Scope");

   private ProfileScope(int id, String name)
   {
      super(id, name);
   }
}