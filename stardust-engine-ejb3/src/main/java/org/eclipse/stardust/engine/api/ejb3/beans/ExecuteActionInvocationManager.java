package org.eclipse.stardust.engine.api.ejb3.beans;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.eclipse.stardust.common.utils.ejb.J2eeContainerType;
import org.eclipse.stardust.engine.api.ejb3.interceptors.CMTSessionInterceptor;
import org.eclipse.stardust.engine.api.ejb3.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.api.ejb3.interceptors.SessionBeanExceptionHandler;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.ForkingDebugInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.NonInteractiveSecurityContextInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.RuntimeExtensionsInterceptor;

class ExecuteActionInvocationManager extends InvocationManager
   {
      private static final long serialVersionUID = 1L;

      public ExecuteActionInvocationManager(SessionContext sessionContext,
            ActionRunner actionRunner, org.eclipse.stardust.engine.api.ejb3.ForkingService service)
      {
         super(actionRunner, setupInterceptors(sessionContext, service));
      }

      private static List setupInterceptors(SessionContext sessionContext, org.eclipse.stardust.engine.api.ejb3.ForkingService service)
      {
    	  
    	  
         List interceptors = new ArrayList(7);

         interceptors.add(new ForkingDebugInterceptor());
         interceptors.add(new PropertyLayerProviderInterceptor());
         interceptors.add(new ContainerConfigurationInterceptor("ForkingService",
               J2eeContainerType.EJB, service));
         interceptors.add(new CMTSessionInterceptor(
               SessionProperties.DS_NAME_AUDIT_TRAIL, sessionContext, service));
         interceptors.add(new NonInteractiveSecurityContextInterceptor());
         interceptors.add(new RuntimeExtensionsInterceptor());
         interceptors.add(new SessionBeanExceptionHandler(sessionContext));
         interceptors.add(new CallingInterceptor());

         return interceptors;
      }
   }