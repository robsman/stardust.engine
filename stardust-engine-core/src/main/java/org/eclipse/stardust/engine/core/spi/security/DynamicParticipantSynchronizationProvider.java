/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
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

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;

/**
 * Contract for providing access to external user repositories.
 * 
 * @author rsauer
 * @version $Revision$
 */
public abstract class DynamicParticipantSynchronizationProvider
{
   public static final Logger trace = RuntimeLog.SPI;
   
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
    * wrapper to catch and log possible exceptions.
    * 
    * {@link provideUserConfiguration(String account)}
    */      
   public final ExternalUserConfiguration provideUserConfigurationLogAware(String account)
   {
      try
      {
         return provideUserConfiguration(account);
      }
      catch (Exception e)
      {
         String exceptionMessage = e.getMessage();
         trace.warn(exceptionMessage, e);
         
         throw new InternalException(e);
      }
   }   
   
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
      return provideUserConfigurationLogAware(account);
   }
   
   /**
    * Resolves a user in the external registry and provides its attributes.
    * A validity check is performed before the user configuration is returned.
    *
    * @param realm The security realm of the external user.
    * @param account The identity of the external user.
    * @param properties The login properties like partition, domain, ...
    * @return A (probably snapshot) of the users attributes.
    * 
    * @see #provideUserConfiguration(String account)
    */
   public ExternalUserConfiguration provideValidUserConfiguration(String realm,
         String account, Map properties)
   {
      ExternalUserConfiguration config = provideUserConfiguration(realm, account, properties);
      if (config != null)
      {
         if (StringUtils.isEmpty(config.getLastName()))
         {
            StringBuilder sb = new StringBuilder();
            sb.append(account);
            sb.append('@');
            sb.append(realm);
            sb.append(": ");
            sb.append(ExternalUserConfiguration.class.getSimpleName());
            sb.append(".lastName");
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise(sb.toString()));
         }
         if (StringUtils.isEmpty(config.getFirstName()))
         {
            StringBuilder sb = new StringBuilder();
            sb.append(account);
            sb.append('@');
            sb.append(realm);
            sb.append(": ");
            sb.append(ExternalUserConfiguration.class.getSimpleName());
            sb.append(".firstName");
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise(sb.toString()));
         }
      }
      return config;
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
    * Resolves a user group in the external registry for given partition and provides
    * its attributes. A validity check is performed before the user group configuration is returned.
    *
    * @param groupId The identity of the external user group.
    * @param properties The login properties like partition, domain, ... 
    * @return A (probably snapshot) of the user groups attributes.
    */
   public final ExternalUserGroupConfiguration provideValidUserGroupConfiguration(String groupId,
         Map properties)
   {
      ExternalUserGroupConfiguration config = provideUserGroupConfiguration(groupId, properties);
      if (config != null)
      {
         if (StringUtils.isEmpty(config.getName()))
         {
            StringBuilder sb = new StringBuilder();
            sb.append(groupId);
            sb.append(": ");
            sb.append(ExternalUserGroupConfiguration.class.getSimpleName());
            sb.append(".name");
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise(sb.toString()));
         }
      }
      return config;
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
   
   /**
    * Resolves a department in the external registry for given partition and provides
    * its attributes. A validity check is performed before the department configuration is returned.
    *
    * @param departmentKey The identity of the external department.
    * @param properties The login properties like partition, domain, ...
    * @return A (probably) snapshot of the department attributes.
    */
   public final ExternalDepartmentConfiguration provideValidDepartmentConfiguration(String participantId,
         List<String> departmentKey, Map<String, ?> properties)
   {
      ExternalDepartmentConfiguration config = provideDepartmentConfiguration(
            participantId, departmentKey, properties);
      if (config != null)
      {
         if (StringUtils.isEmpty(config.getName()))
         {
            StringBuilder sb = new StringBuilder();
            for (String key : departmentKey)
            {
               if (sb.length() > 0)
               {
                  sb.append('.');
               }
               sb.append(key);
            }
            sb.append('@');
            sb.append(participantId);
            sb.append(": ");
            sb.append(ExternalDepartmentConfiguration.class.getSimpleName());
            sb.append(".name");
            throw new InvalidArgumentException(BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise(sb.toString()));
         }
      }
      return config;
   }
}
