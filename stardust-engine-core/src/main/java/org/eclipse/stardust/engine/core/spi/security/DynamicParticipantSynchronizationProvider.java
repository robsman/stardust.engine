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
package org.eclipse.stardust.engine.core.spi.security;

import java.util.List;
import java.util.Map;

/**
 * Contract for providing access to external user repositories.
 * 
 * @author rsauer
 * @version $Revision$
 */
public abstract class DynamicParticipantSynchronizationProvider
{
   /**
    * Resolves a user in the external registry and provides its attributes.
    *
    * @param account The identity of the external user.
    * @return A (probably snapshot) of the users attributes.
    * 
    * @see #provideUserConfiguration(String realm, String account)
    */
   public abstract ExternalUserConfiguration provideUserConfiguration(String account);
   
   /**
    * Resolves a user in the external registry and provides its attributes.
    * <br />
    * The default implementation just calls {@link #provideUserConfiguration(String account)},
    * effectively ignoring the realm.
    *
    * @param realm The security realm of the external user.
    * @param account The identity of the external user.
    * @param properties The login properties like partition, domain, ...
    * @return A (probably snapshot) of the users attributes.
    * 
    * @see #provideUserConfiguration(String account)
    */
   public ExternalUserConfiguration provideUserConfiguration(String realm,
         String account, Map properties)
   {
      return provideUserConfiguration(account);
   }
   
   /**
    * Resolves a user group in the external registry for current partition and provides
    * its attributes.
    *
    * @param groupId The identity of the external user group.
    * @return A (probably snapshot) of the user groups attributes.
    */
   public ExternalUserGroupConfiguration provideUserGroupConfiguration(String groupId)
   {
      return null;
   }
   
   /**
    * Resolves a user group in the external registry for given partition and provides
    * its attributes.
    *
    * @param groupId The identity of the external user group.
    * @param properties The login properties like partition, domain, ... 
    * @return A (probably snapshot) of the user groups attributes.
    */
   public ExternalUserGroupConfiguration provideUserGroupConfiguration(String groupId,
         Map properties)
   {
      return provideUserGroupConfiguration(groupId);
   }
   
   /**
    * Resolves a department in the external registry for given partition and provides
    * its attributes.
    *
    * @param departmentKey The identity of the external department.
    * @return A (probably) snapshot of the department attributes.
    */
   public ExternalDepartmentConfiguration provideDepartmentConfiguration(String participantId,
         List<String> departmentKey)
   {
      return null;
   }
   
   /**
    * Resolves a department in the external registry for given partition and provides
    * its attributes.
    *
    * @param departmentKey The identity of the external department.
    * @param properties The login properties like partition, domain, ...
    * @return A (probably) snapshot of the department attributes.
    */
   public ExternalDepartmentConfiguration provideDepartmentConfiguration(String participantId,
         List<String> departmentKey, Map<String, ?> properties)
   {
      return provideDepartmentConfiguration(participantId, departmentKey);
   }
}
