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
package org.eclipse.stardust.engine.core.persistence.jdbc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

import org.eclipse.stardust.common.error.ApplicationException;
import org.eclipse.stardust.common.error.TransactionFreezedException;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.ExceptionUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;


public abstract class SqlExceptionTransformingProxy implements InvocationHandler
{
   private Method[] methodsWhiteList;
   protected Object object;
   protected TransactionFreezeInfo txFreezeInfo;
   
   private boolean acceptMethodInFreezedTx(Method method)
   {
      boolean monitorMethods = ((Session) SessionFactory
            .getSession(SessionFactory.AUDIT_TRAIL)).isMonitoringOnJdbcInterception();

      if ( !monitorMethods)
      {
         return true;
      }
         
      for (int i = 0; i < methodsWhiteList.length; ++i)
      {
         if (methodsWhiteList[i].equals(method))
         {
            return true;
         }
      }
      
      return false;
   }
   
   protected SqlExceptionTransformingProxy(TransactionFreezeInfo txFreezeInfo, Method[] methodsWhiteList)
   {
      this.methodsWhiteList = methodsWhiteList;
      this.txFreezeInfo = txFreezeInfo;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Object result;
      
      try
      {
         if ( !txFreezeInfo.isFreezed() || acceptMethodInFreezedTx(method))
         {
            result = method.invoke(object, args);
         }
         else
         {
            throw new TransactionFreezedException("Transaction already in freezed state.",
                  txFreezeInfo.getTxFreezeException().isTxRolledBackOnFreeze(),
                  txFreezeInfo.getTxFreezeException().getCause());
         }
      }
      catch(TransactionFreezedException x)
      {
         throw x;
      }
      catch (InvocationTargetException e)
      {
         Throwable throwable = e.getTargetException();
         
         if (throwable instanceof SQLException)
         {
            DBDescriptor dbDescriptor = ((Session) SessionFactory
                  .getSession(SessionFactory.AUDIT_TRAIL)).getDBDescriptor();
            
            ApplicationException appException = ExceptionUtils.transformException(
                  dbDescriptor, (SQLException) throwable);
            
            if (null != appException)
            {
               if (appException instanceof TransactionFreezedException)
               {
                  txFreezeInfo
                        .setTxFreezeException((TransactionFreezedException) appException);
               }

               throw appException;
            }
         }
         
         throw throwable;
      }
      catch (Exception e)
      {
         throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
      }

      return result;
   }
}
