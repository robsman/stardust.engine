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

import static org.eclipse.stardust.engine.api.spring.SpringConstants.REPORTING_TX_TIMEOUT;

import java.util.Collections;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.runtime.TxRollbackPolicy;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.interceptor.TransactionPolicyAdvisor;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class SpringTxInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 1L;

   private static final String REPORTING_SERVICE_CLASS_NAME = "org.eclipse.stardust.reporting.rt.service.ReportingService";

   private final AbstractSpringServiceBean serviceBean;

   private final TxRollbackPolicy txRollbackPolicy;

   public SpringTxInterceptor(AbstractSpringServiceBean serviceBean, TxRollbackPolicy txRollbackPolicy)
   {
      this.serviceBean = serviceBean;
      this.txRollbackPolicy = txRollbackPolicy;
   }

   public Object invoke(final MethodInvocation invocation) throws Throwable
   {
      TransactionTemplate txTemplate = new TransactionTemplate(
            serviceBean.getTransactionManager());
      txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
      //txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

      if (REPORTING_SERVICE_CLASS_NAME.equals(invocation.getMethod().getDeclaringClass().getName()))
      {
         int txTimeout = Parameters.instance().getInteger(REPORTING_TX_TIMEOUT, -1);
         if (txTimeout != -1)
         {
            txTemplate.setTimeout(txTimeout);
         }
      }

      return txTemplate.execute(new SpringTxCallback()
      {
         @Override
         public boolean mustRollback(MethodInvocation invocation, Throwable e)
         {
            return (null == txRollbackPolicy) || txRollbackPolicy.mustRollback(invocation, e);
         }

         @Override
         public Object doInTransaction(TransactionStatus status)
         {
            this.txStatus = status;

            final ITransactionStatus outerTxStatus = TransactionUtils.getCurrentTxStatus(invocation.getParameters());

            final PropertyLayer localProps = ParametersFacade.pushLayer(
                  invocation.getParameters(), Collections.EMPTY_MAP);

            try
            {
               TransactionUtils.registerTxStatus(localProps, this);

               return invocation.proceed();
            }
            catch (RuntimeException e)
            {
               if ( !status.isCompleted() && mustRollback(invocation, e))
               {
                  status.setRollbackOnly();
               }
               throw e;
            }
            catch (Error e)
            {
               if ( !status.isCompleted() && mustRollback(invocation, e))
               {
                  status.setRollbackOnly();
               }
               throw e;
            }
            catch (Throwable e)
            {
               if ( !status.isCompleted() && mustRollback(invocation, e))
               {
                  status.setRollbackOnly();
               }
               throw new PublicException("", e);
            }
            finally
            {
               // unregister txStatus
               ParametersFacade.popLayer(invocation.getParameters());

               if ((null != outerTxStatus)
                     && (outerTxStatus != TransactionUtils.NO_OP_TX_STATUS)
                     && status.isRollbackOnly())
               {
                  outerTxStatus.setRollbackOnly();
               }

               this.txStatus = null;
            }
         }

      });
   }

   public static abstract class SpringTxCallback
         implements TransactionCallback, ITransactionStatus, TransactionPolicyAdvisor
   {
      protected TransactionStatus txStatus;

      public boolean isRollbackOnly()
      {
         return (null != txStatus) ? txStatus.isRollbackOnly() : false;
      }

      public void setRollbackOnly()
      {
         if (null != txStatus)
         {
            txStatus.setRollbackOnly();
         }
      }

      public Object getTransaction()
      {
         return ((DefaultTransactionStatus)txStatus).getTransaction();
      }
   }
}
