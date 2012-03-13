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

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.config.PropertyLayer;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.rt.ITransactionStatus;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
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

   private final AbstractSpringServiceBean serviceBean;

   public SpringTxInterceptor(AbstractSpringServiceBean serviceBean)
   {
      this.serviceBean = serviceBean;
   }

   public Object invoke(final MethodInvocation invocation) throws Throwable
   {
      TransactionTemplate txTemplate = new TransactionTemplate(
            serviceBean.getTransactionManager());
      txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
      //txTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
      
      return txTemplate.execute(new SpringTxCallback()
      {
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
               if ( !status.isCompleted())
               {
                  status.setRollbackOnly();
               }
               throw e;
            }
            catch (Error e)
            {
               if ( !status.isCompleted())
               {
                  status.setRollbackOnly();
               }
               throw e;
            }
            catch (Throwable e)
            {
               if ( !status.isCompleted())
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
         implements TransactionCallback, ITransactionStatus
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
