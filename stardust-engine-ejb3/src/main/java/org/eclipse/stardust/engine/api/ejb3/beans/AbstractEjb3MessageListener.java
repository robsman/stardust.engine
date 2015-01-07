package org.eclipse.stardust.engine.api.ejb3.beans;

import java.lang.reflect.Proxy;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.sql.DataSource;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.WorkflowException;
import org.eclipse.stardust.engine.core.runtime.beans.LoggedInUser;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.ejb.Ejb3ManagedService;
import org.eclipse.stardust.engine.core.runtime.ejb.ExecutorService;
import org.eclipse.stardust.engine.core.runtime.ejb.ForkingService;
import org.eclipse.stardust.engine.core.runtime.ejb.MDBInvocationManager;

public abstract class AbstractEjb3MessageListener implements javax.jms.MessageListener
{
   private final Kind kind;

   @EJB
   private ForkingService forkingService;

   @Resource
   private MessageDrivenContext context;

   protected AbstractEjb3MessageListener(Kind kind)
   {
      this.kind = kind;
   }

   @Override
   public void onMessage(Message message)
   {
      String failureMode = Parameters.instance().getString(
            kind.failureModeProperty,
            JmsProperties.PROCESSING_FAILURE_MODE_FORGET);
      boolean rollbackOnError = JmsProperties.PROCESSING_FAILURE_MODE_ROLLBACK.equalsIgnoreCase(failureMode);
      int nRetries = Parameters.instance().getInteger(
            kind.retryCountProperty, 20);
      int tPause = Parameters.instance().getInteger(
            kind.retryPauseProperty, 500);

      Action<?> action = (Action<?>) Proxy.newProxyInstance(
            Action.class.getClassLoader(),
            new Class[] {Action.class},
            new MDBInvocationManager(
                  kind.name(),
                  createAction(message, forkingService),
                  context, nRetries, tPause, rollbackOnError));
      action.execute();
   }

   protected abstract MDAction createAction(Message message, ForkingService forkingService);

   protected static enum Kind
   {
      MessageListener, DaemonListener, ResponseHandler;

      private final String failureModeProperty;
      private final String retryCountProperty;
      private final String retryPauseProperty;

      private Kind()
      {
         failureModeProperty = "JMS." + name() + ".ProcessingFailure.Mode";
         retryCountProperty = "JMS." + name() + ".ProcessingFailure.Retries";
         retryPauseProperty = "JMS." + name() + ".ProcessingFailure.Pause";
      }
   }

   protected static abstract class MDAction implements Action<Object>, Ejb3ManagedService
   {
      protected final Message message;
      protected final ForkingService forkingService;

      protected MDAction(Message message, ForkingService forkingService)
      {
         this.message = message;
         this.forkingService = forkingService;
      }

      protected void bootstrapModelManager() throws WorkflowException
      {
         forkingService.run(new Action<Object>()
         {
            public Object execute()
            {
               ModelManagerFactory.getCurrent().findActiveModel();
               return null;
            }
         }, forkingService);
      }

      @Override
      public void remove()
      {
      }

      @Override
      public LoggedInUser login(String username, String password, @SuppressWarnings("rawtypes") Map properties)
      {
         return null;
      }

      @Override
      public void logout()
      {
      }

      @Override
      public DataSource getDataSource()
      {
         return null;
      }

      @Override
      public Object getRepository()
      {
         return null;
      }

      @Override
      public ExecutorService getForkingService()
      {
         return forkingService;
      }
   }
}
