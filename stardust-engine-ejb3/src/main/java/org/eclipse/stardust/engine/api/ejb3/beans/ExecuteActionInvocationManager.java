package org.eclipse.stardust.engine.api.ejb3.beans;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.runtime.beans.ActionRunner;
import org.eclipse.stardust.engine.core.runtime.beans.InvocationManager;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.ForkingDebugInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.NonInteractiveSecurityContextInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.RuntimeExtensionsInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.CMTSessionInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.ContainerConfigurationInterceptor;
import org.eclipse.stardust.engine.core.runtime.ejb.interceptors.SessionBeanExceptionHandler;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;

class ExecuteActionInvocationManager extends InvocationManager
{
   private static final long serialVersionUID = 1L;

   public ExecuteActionInvocationManager(SessionContext sessionContext,
         ActionRunner actionRunner, ExecutorService service)
   {
      super(actionRunner, setupInterceptors(sessionContext, service));
   }

   private static List<MethodInterceptor> setupInterceptors(SessionContext sessionContext,
         ExecutorService service)
   {
      List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>(7);

      interceptors.add(new ForkingDebugInterceptor());
      interceptors.add(new PropertyLayerProviderInterceptor());
      interceptors.add(new ContainerConfigurationInterceptor("ForkingService", service));
      interceptors.add(new CMTSessionInterceptor(SessionProperties.DS_NAME_AUDIT_TRAIL,
            sessionContext, service));
      interceptors.add(new NonInteractiveSecurityContextInterceptor());
      interceptors.add(new RuntimeExtensionsInterceptor());
      interceptors.add(new SessionBeanExceptionHandler(sessionContext));
      interceptors.add(new CallingInterceptor());

      return interceptors;
   }
}