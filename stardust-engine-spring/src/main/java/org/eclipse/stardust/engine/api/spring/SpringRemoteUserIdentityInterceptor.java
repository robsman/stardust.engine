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
package org.eclipse.stardust.engine.api.spring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.IAuditTrailPartition;
import org.eclipse.stardust.engine.core.runtime.beans.IUser;
import org.eclipse.stardust.engine.core.runtime.beans.IUserDomain;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.SynchronizationService;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SpringRemoteUserIdentityInterceptor implements MethodInterceptor
{
   private static final ThreadLocal userIdHolder = new ThreadLocal();

   public static String getUserId()
   {
      return (String) userIdHolder.get();
   }
   
   public static void setUserId(String userId)
   {
      userIdHolder.set(userId);
   }
   
   public static void resetUserId()
   {
      // TODO Java 1.5++
      // settings.remove();
      userIdHolder.set(null);
   }

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      String userId = getUserId();
      
      boolean setUser = false;
      if ( !StringUtils.isEmpty(userId))
      {
         final PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();
         
         Map loginProperties = new HashMap();
         LoginUtils.mergeDefaultCredentials(loginProperties);
/*
         loginProperties.put(SecurityProperties.PARTITION, partitionId);
         loginProperties.put(SecurityProperties.REALM, realmId);
         loginProperties.put(SecurityProperties.DOMAIN, domainId);
*/
         IAuditTrailPartition partition = LoginUtils.findPartition(
               invocation.getParameters(), loginProperties);
         IUserDomain domain = LoginUtils.findUserDomain(invocation.getParameters(),
               partition, loginProperties);

         layer.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
         layer.setProperty(SecurityProperties.CURRENT_PARTITION_OID, new Short(
               partition.getOID()));
         layer.setProperty(SecurityProperties.CURRENT_DOMAIN, domain);
         layer.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, new Long(domain
               .getOID()));

         IModel model = ModelManagerFactory.getCurrent().findActiveModel();
         if (model == null)
         {
            model = ModelManagerFactory.getCurrent().findLastDeployedModel();
         }

         IUser user = SynchronizationService
               .synchronize(userId, model,
                     invocation.getParameters().getBoolean(
                           SecurityProperties.AUTHORIZATION_SYNC_LOGIN_PROPERTY,
                           true), loginProperties);

         layer.setProperty(SecurityProperties.CURRENT_USER, user);
         
         // clean thread, so nested calls won't reuse the user ID
         setUser = true;
         resetUserId();
      }

      try
      {
         return invocation.proceed();
      }
      finally
      {
         if (setUser)
         {
            // restore thread status
            setUserId(userId);
         }
      }
   }
}
