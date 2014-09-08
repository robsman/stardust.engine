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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.IActionCarrier;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.core.persistence.Predicates;
import org.eclipse.stardust.engine.core.persistence.QueryExtension;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.runtime.beans.*;
import org.eclipse.stardust.engine.core.runtime.beans.daemons.*;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.CallingInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.ForkingDebugInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.MultipleTryInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.interceptors.PropertyLayerProviderInterceptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.spring.schedulers.DaemonScheduler;
import org.eclipse.stardust.engine.spring.schedulers.DefaultScheduler;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public abstract class AbstractSpringForkingServiceBean extends AbstractSpringServiceBean
      implements ForkingService, DaemonHandler
{
   private DaemonScheduler scheduler;

   public AbstractSpringForkingServiceBean()
   {
      super(ForkingService.class, TxIsolatedActionInvoker.class);
   }

   public void afterPropertiesSet() throws Exception
   {
      super.afterPropertiesSet();
      if (scheduler == null)
      {
         scheduler = new DefaultScheduler();
      }
      isolate(new Action()
      {
         public Object execute()
         {
            Iterator<DaemonLog> startLogs = SessionFactory.getSession(SessionFactory.AUDIT_TRAIL)
               .getIterator(DaemonLog.class,
                  QueryExtension.where(
                        Predicates.andTerm(
                              Predicates.isEqual(DaemonLog.FR__CODE, DaemonLog.START),
                              Predicates.greaterThan(DaemonLog.FR__STAMP, 0)
                              )
                        ));
            
            Map<AuditTrailPartitionBean, UserDomainBean> cache = CollectionUtils.newMap();
            Map<String, Object> layer = CollectionUtils.newMap();
            while (startLogs.hasNext())
            {
               DaemonLog log = startLogs.next();
               short partitionOid = log.getPartition();
               AuditTrailPartitionBean partition = AuditTrailPartitionBean.findByOID(partitionOid);
               UserDomainBean domain = cache.get(partition);
               if (domain == null)
               {
                  domain = UserDomainBean.findById(partition.getId(), partitionOid);
                  cache.put(partition, domain);
               }
               layer.put(SecurityProperties.CURRENT_PARTITION_OID, partitionOid);
               layer.put(SecurityProperties.CURRENT_DOMAIN_OID, domain.getOID());
               layer.put(SecurityProperties.CURRENT_PARTITION, partition);
               layer.put(SecurityProperties.CURRENT_DOMAIN, domain);
               try
               {
                  ParametersFacade.pushLayer(layer);
                  // force bootstrap the engine
                  ModelManagerFactory.getCurrent().findActiveModel();
                  startTimer(new DaemonCarrier(log.getType()));
               }
               finally
               {
                  ParametersFacade.popLayer();
               }
            }
            return null;
         }
      });
   }
   
   public DaemonScheduler getScheduler()
   {
      return scheduler;
   }

   public void setScheduler(DaemonScheduler scheduler)
   {
      this.scheduler = scheduler;
   }

   public Object isolate(Action action) throws PublicException
   {
      if (action instanceof DaemonOperation)
      {
         action = new DaemonOperationExecutor((DaemonOperation) action, this);
      }

      TransactionTemplate txTemplate = new TransactionTemplate(getTransactionManager());
      txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
      //txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

      final Action toExecute = action;
      return txTemplate.execute(new SpringTxInterceptor.SpringTxCallback()
      {
         @Override
         public boolean mustRollback(MethodInvocation invocation, Throwable Exception)
         {
            // always roll-back forking service invocations
            return true;
         }

         @Override
         public Object doInTransaction(final TransactionStatus status)
         {
            this.txStatus = status;

            final PropertyLayer localProps = ParametersFacade.pushLayer(Collections.EMPTY_MAP);
            TransactionUtils.registerTxStatus(localProps, this);

            try
            {
               return ((ForkingService) serviceProxy).isolate(toExecute);
            }
            finally
            {
               ParametersFacade.popLayer();
            }
         }
      });
   }

   public abstract void fork(IActionCarrier action, boolean transacted);

   public void startTimer(DaemonCarrier carrier)
   {
      final DaemonCarrier innerCarrier = carrier.copy();
      long period = Parameters.instance().getLong(
            innerCarrier.getType() + DaemonProperties.DAEMON_PERIODICITY_SUFFIX, 5) * 1000;
      final Runnable runnable = new Runnable()
      {
         public void run()
         {
            runDaemon(innerCarrier);
         }
      };
      scheduler.start(innerCarrier, period, runnable);
   }

   public void stopTimer(DaemonCarrier carrier)
   {
      scheduler.stop(carrier);
   }

   public boolean checkTimer(DaemonCarrier carrier)
   {
      return scheduler.isScheduled(carrier);
   }

   public void runDaemon(DaemonCarrier carrier)
   {
      fork(carrier.copy(), false);
   }

   protected static class ForkedActionInvocationManager extends InvocationManager
   {
      private static final long serialVersionUID = 1L;

      public ForkedActionInvocationManager(ActionRunner target)
      {
         super(target, setupInterceptors());
      }

      private static List setupInterceptors()
      {
         final Parameters parameters = Parameters.instance();

         List interceptors = CollectionUtils.newList(4);

         interceptors.add(new ForkingDebugInterceptor());
         interceptors.add(new PropertyLayerProviderInterceptor());
         interceptors.add(new MultipleTryInterceptor(//
               parameters.getInteger("POJO.Forking"
                     + JmsProperties.PROP_SUFFIX_MDB_RETRY_COUNT, 10),//
               parameters.getInteger("POJO.Forking"
                     + JmsProperties.PROP_SUFFIX_MDB_RETRY_PAUSE, 500)));
         interceptors.add(new CallingInterceptor());

         // other interceptors are inherited from the forking service Spring bean

         return interceptors;
      }
   }

   protected static class ForkedActionInvoker implements ActionRunner
   {
      private final ForkingServiceFactory serviceFactory;

      public ForkedActionInvoker(ForkingServiceFactory serviceFactory)
      {
         this.serviceFactory = serviceFactory;
      }

      public Object execute(Action action)
      {
         ForkingService service = serviceFactory.get();
         try
         {
            return service.isolate(action);
         }
         finally
         {
            serviceFactory.release(service);
         }
      }
   }

   protected static class TxIsolatedActionInvoker implements ForkingService
   {
      public Object isolate(Action action) throws PublicException
      {
         return action.execute();
      }

      public void fork(IActionCarrier action, boolean transacted)
      {
         throw new IllegalOperationException("This method must never be called directly.");
      }
   }
}
