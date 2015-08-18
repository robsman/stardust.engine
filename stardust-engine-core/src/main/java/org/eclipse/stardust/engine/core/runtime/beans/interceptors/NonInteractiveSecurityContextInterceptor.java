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
package org.eclipse.stardust.engine.core.runtime.beans.interceptors;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.LoginUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.SecurityContextAwareAction.SecurityContextBoundAction;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;


public class NonInteractiveSecurityContextInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   private static final String METHOD_FORKING_SERVICE_ISOLATE = "isolate";
   
   private static final String METHOD_ACTION_RUNNER_EXECUTE = "execute";
   
   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      if (isSecurityContextAwareActionInvocation(invocation))
      {
         final Object[] args = invocation.getArguments();

         SecurityContextBoundAction secContextAction = (SecurityContextBoundAction) args[0];

         PropertyLayer layer = PropertyLayerProviderInterceptor.getCurrent();

         buildSecurityContext(secContextAction.getAction(), layer,
               invocation.getParameters());
      }

      return invocation.proceed();
   }
   
   public static boolean isSecurityContextAwareActionInvocation(MethodInvocation invocation)
   {
      final Object[] args = invocation.getArguments();

      final boolean isForkingServiceIsolate = ForkingService.class.isInstance(invocation.getTarget())
            && METHOD_FORKING_SERVICE_ISOLATE.equals(invocation.getMethod().getName());

      final boolean isActionRunnerExecuteAction = ActionRunner.class.isInstance(invocation.getTarget())
            && METHOD_ACTION_RUNNER_EXECUTE.equals(invocation.getMethod().getName());

      return (isForkingServiceIsolate || isActionRunnerExecuteAction) //
            && (null != args)
            && (1 == args.length)
            && (args[0] instanceof SecurityContextBoundAction);
   }
   
   public static void buildSecurityContext(SecurityContextAwareAction action,
         PropertyLayer props, Parameters params)
   {
      final short partitionOid = action.getPartitionOid();
      final long userDomainOid = action.getUserDomainOid();

      // bind partition and domain OIDs
      props.setProperty(SecurityProperties.CURRENT_PARTITION_OID, new Short(partitionOid));
      props.setProperty(SecurityProperties.CURRENT_DOMAIN_OID, new Long(userDomainOid));

      if (( -1 != partitionOid)
            && (null != SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)))
      {
         // bind real partition and domain
         IAuditTrailPartition partition = LoginUtils.findPartition(params, partitionOid);
         IUserDomain domain = (null != partition) //
               ? LoginUtils.findUserDomain(params, partition, userDomainOid)
               : UserDomainBean.findByOID(userDomainOid);

         props.setProperty(SecurityProperties.CURRENT_PARTITION, partition);
         props.setProperty(SecurityProperties.CURRENT_DOMAIN, domain);

         // bind system user
         UserRealmBean transientRealm = UserRealmBean.createTransientRealm(
               PredefinedConstants.SYSTEM_REALM, PredefinedConstants.SYSTEM_REALM,
               partition);
         IUser transientUser = UserBean.createTransientUser(PredefinedConstants.SYSTEM,
               PredefinedConstants.SYSTEM_FIRST_NAME, PredefinedConstants.SYSTEM_LAST_NAME,
               transientRealm);
         props.setProperty(SecurityProperties.CURRENT_USER, transientUser);
      }
   }

}
