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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.error.LoginFailedException;
import org.eclipse.stardust.engine.api.model.IModel;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.LogCode;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.LoginServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.core.security.InvokerPrincipal;
import org.eclipse.stardust.engine.core.security.InvokerPrincipalUtils;
import org.eclipse.stardust.engine.core.spi.security.ExternalLoginResult;
import org.eclipse.stardust.engine.core.spi.security.PrincipalProvider;
import org.eclipse.stardust.engine.core.spi.security.PrincipalWithProperties;
import org.eclipse.stardust.engine.extensions.ejb.utils.J2EEUtils;



// @todo (france, ub): should be three interceptors (?!)
// - login interceptor for internal login
// - login interceptor for principal login
// - synchronization interceptor

/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class AbstractLoginInterceptor implements MethodInterceptor
{
   private static final Logger trace = LogManager.getLogger(AbstractLoginInterceptor.class);
   
   public static final String METHODNAME_LOGIN = "login";
   public static final String METHODNAME_LOGOUT = "logout";

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      Object result = null;
      
      Object principalProvider = invocation.getParameters().get(
            SecurityProperties.AUTHENTICATION_PRINCIPAL_PROVIDER_PROPERTY);
      if (principalProvider instanceof PrincipalProvider)
      {
         Principal principal = ((PrincipalProvider) principalProvider).getPrincipal();

         if (null != principal)
         {
            String principalName = J2EEUtils.getPrincipalName(principal);
            if (trace.isDebugEnabled())
            {
               trace.debug("Performing implicit login for user " + principalName);
            }
            
            Map mergedProperties = new HashMap();
            if (principal instanceof PrincipalWithProperties)
            {
               mergedProperties.putAll(((PrincipalWithProperties) principal).getProperties());
            }
            else
            {
               InvokerPrincipal invokerPrincipal = InvokerPrincipalUtils.getCurrent();
               if (null != invokerPrincipal)
               {
                  if (invokerPrincipal.getName().equals(principalName))
                  {
                     mergedProperties.putAll(invokerPrincipal.getProperties());
                  }
                  else
                  {
                     throw new InternalException("No invoker principal does not match principal. "
                           + "Implicit login attempt aborted.");
                  }
               }
            }
            LoginUtils.mergeDefaultCredentials(invocation.getParameters(),
                  mergedProperties);

            LoggedInUser loggedInUser = new LoggedInUser(principalName, mergedProperties);

            result = performCall(invocation, loggedInUser);
         }
         else
         {
            throw new InternalException("No caller principal available. "
                  + "Implicit login attempt aborted.");
         }
      }
      else if (invocation.getMethod().getDeclaringClass().getName().equals(
            ManagedService.class.getName()))
      {
         if (isLoginCall(invocation.getMethod()))
         {
            result = performLoginCall(invocation);
         }
         else if (isLogoutCall(invocation.getMethod()))
         {
            performLogoutCall();
            
            result = null;
         }
      }
      else
      {
         result = performCall(invocation, null);
      }
      
      return result;
   }

   protected Object performCall(MethodInvocation invocation, LoggedInUser loggedInUser)
         throws Throwable
   {
      if ((null != loggedInUser) && !StringUtils.isEmpty(loggedInUser.getUserId()))
      {
         final PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();
   
         // need to set partition or else model manager will not bootstrap
         setCurrentPartitionAndDomain(invocation.getParameters(), layer,
               loggedInUser.getProperties());
         
         IModel model = ModelManagerFactory.getCurrent().findActiveModel();
         if (model == null)
         {
            model = ModelManagerFactory.getCurrent().findLastDeployedModel();
         }
   
         IUser user = SynchronizationService.synchronize(loggedInUser.getUserId(),
               model, invocation.getParameters().getBoolean(
                     SecurityProperties.AUTHORIZATION_SYNC_LOGIN_PROPERTY, true),
               loggedInUser.getProperties());
         
         if (null != user && LoginUtils.isUserExpired(user))
         {
            throw LoginUtils.createAccountExpiredException(user);
         }
                     
         UserUtils.updateDeputyGrants((UserBean) user);
                                   
         setCurrentUser(layer, user);
         
         SessionManager.instance().updateLastModificationTime(user);
      }
      
      return invocation.proceed();
   }

   protected LoggedInUser performLoginCall(MethodInvocation invocation)
   {
      final PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();

      Object[] args = invocation.getArguments();
      
      final Map loginProperties = (2 < args.length) ? (Map) args[2] : Collections.EMPTY_MAP; 
      
      // need to set partition or else model manager will not bootstrap
      setCurrentPartitionAndDomain(invocation.getParameters(), layer, loginProperties);      
      
      LoggedInUser loggedInUser = doLogin(invocation);      
      
      IUser user = getUser(invocation, loggedInUser.getUserId(), loggedInUser.getProperties());
      
      setCurrentUser(layer, user);

      SessionManager.instance().updateLastModificationTime(user);
      
      if ( !LoginUtils.isLoginLoggingDisabled(user))
      {
         AuditTrailLogger.getInstance(LogCode.SECURITY).info("Logged in.");
      }
      
      return loggedInUser;
   }
   
   protected void performLogoutCall()
   {
   }

   public static boolean isLoginCall(Method method)
   {
      return method.getDeclaringClass().getName().equals(ManagedService.class.getName())
            && METHODNAME_LOGIN.equals(method.getName());
   }
   
   public static boolean isLogoutCall(Method method)
   {
      return method.getDeclaringClass().getName().equals(ManagedService.class.getName())
            && METHODNAME_LOGOUT.equals(method.getName());
   }
   
   public static LoggedInUser doLogin(MethodInvocation invocation)
         throws LoginFailedException
   {
      ExternalLoginResult result;
      ForkingService service = null;
      try
      {
         ForkingServiceFactory factory = (ForkingServiceFactory) invocation.getParameters()
               .get(EngineProperties.FORKING_SERVICE_HOME);
         service = factory.get();

         Object[] args = invocation.getArguments();
         
         final String userId;
         final String originalUserId = (String) args[0];
         final String password = (String) args[1];
         final Map loginProperties = (2 < args.length)
               ? (Map) args[2]
               : Collections.EMPTY_MAP; 
         result = (ExternalLoginResult) service.isolate(new LoginAction(originalUserId, password,
               loginProperties));
         //give the login provider the possebility to modify the user id
         if(StringUtils.isNotEmpty(result.getUserId()))
         {
            userId = result.getUserId();
         }
         else
         {
            userId = originalUserId;
         }
         
         if (!result.wasSuccessful())
         {
            if (result.getLoginFailedReason().getReason() == LoginFailedException.PASSWORD_EXPIRED
               || result.getLoginFailedReason().getReason() == LoginFailedException.DISABLED_USER)               
            {               
               final PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();
               // need to set partition or else model manager will not bootstrap
               setCurrentPartitionAndDomain(invocation.getParameters(), layer,
                     loginProperties);      
               
               IUser user = getUser(invocation, userId, loginProperties);
               
               if (result.getLoginFailedReason().getReason() == LoginFailedException.PASSWORD_EXPIRED)
               {
                  if(user != null)
                  {
                     user.setPasswordExpired(true);                  
                  }               
               }
               else if (result.getLoginFailedReason().getReason() == LoginFailedException.DISABLED_USER)
               {                  
                  try
                  {
                     service.isolate(new DisableUserAction(user.getOID()));
                  }
                  catch (LoginFailedException e)
                  {
                     throw e;
                  }                  
                  throw result.getLoginFailedReason();
               }               
            }
            else
            {
               throw result.getLoginFailedReason();
            }
         }
         
         if (result.isOverridingProperties())
         {
            loginProperties.putAll(result.getProperties());
         }
         
         Map mergedProperties = new HashMap(loginProperties);
         LoginUtils.mergeDefaultCredentials(mergedProperties);
         
         return new LoggedInUser(userId, mergedProperties);
      }
      finally
      {
         // @todo (france, ub): ForkingServiceLocator.release(service);
      }
   }

   private static IUser getUser(MethodInvocation invocation, final String userId,
         final Map loginProperties)
   {            
      IModel model = ModelManagerFactory.getCurrent().findActiveModel();
      if (model == null)
      {
         model = ModelManagerFactory.getCurrent().findLastDeployedModel();
      }
      
      IUser user;
      try
      {
         user = SynchronizationService.synchronize(userId,
               model, invocation.getParameters().getBoolean(
                     SecurityProperties.AUTHORIZATION_SYNC_LOGIN_PROPERTY, true),
               loginProperties);
      }
      catch (ObjectNotFoundException e)
      {
         throw new LoginFailedException(e.getError(), LoginFailedException.UNKNOWN_REASON);
      }
      
      return user;
   }

   public static void setCurrentPartitionAndDomain(Parameters params, PropertyLayer layer,
         Map loginProperties)
   {
      IAuditTrailPartition partition = LoginUtils.findPartition(params, loginProperties);
      IUserDomain domain = LoginUtils.findUserDomain(params, partition, loginProperties);

      layer.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
      layer.setProperty(SecurityProperties.CURRENT_PARTITION_OID, new Short(
            partition.getOID()));
      layer.setProperty(SecurityProperties.CURRENT_DOMAIN, domain);
      layer.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, new Long(domain.getOID()));
   }
   
   public static void setCurrentUser(PropertyLayer layer, IUser user)
   {
      layer.setProperty(SecurityProperties.CURRENT_USER, user);
   }

   private static class DisableUserAction implements Action
   {
      long userOid;
      
      public DisableUserAction(long oid)
      {
         userOid = oid;
      }

      public Object execute()
      {
         UserBean user = UserBean.findByOid(userOid);
         if (user == null)
         {
            throw new LoginFailedException(
                  BpmRuntimeError.ATDB_UNKNOWN_USER_OID.raise(userOid),
                  LoginFailedException.SYSTEM_ERROR);
         }
         user.setPasswordExpired(true);                  
         user.setValidTo(new Date());                  
         
         return null;
      }
   }
   
   private static class LoginAction implements Action
   {
      private String username;
      private String password;     
      private Map properties;

      public LoginAction(String user, String password, Map properties)
      {
         this.password = password;
         this.username = user;
         this.properties = properties;
      }

      public Object execute()
      {
         Map mergedProps = new HashMap(properties);

         LoginUtils.mergeDefaultCredentials(mergedProps);
         
         ExternalLoginResult login = LoginServiceFactory.getService().login(username, password, mergedProps);
         if(login == null)
         {
            StringBuilder sb = new StringBuilder();
            sb.append("ExternalLoginProvider.login(String id, String password, Map properties) returned null.");
            return ExternalLoginResult.testifyFailure(new LoginFailedException(sb.toString(), LoginFailedException.SYSTEM_ERROR));
         }
         
         return login;
      }

      public String toString()
      {
         return "logging in: " + username;
      }
   }
}