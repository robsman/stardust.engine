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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.eclipse.stardust.common.error.InternalException;


public class JdbcProxy extends SqlExceptionTransformingProxy implements InvocationHandler
{
   private static final Method[] EMPTY_METHODS_WHITE_LIST = new Method[]{};
   
   private static final Method[] CONNECTIONS_METHOD_WHITE_LIST;
   private static final Method[] STATEMENT_METHODS_WHITE_LIST;
   private static final Method[] PREPARED_STATEMENT_METHODS_WHITE_LIST;
   private static final Method[] RESULT_SET_METHODS_WHITE_LIST;
   
   static
   {
      try
      {
         CONNECTIONS_METHOD_WHITE_LIST = new Method[]{
               Connection.class.getMethod("close", new Class[] {}),
               Connection.class.getMethod("rollback", new Class[] {})};
         
         STATEMENT_METHODS_WHITE_LIST = new Method[]{
               Statement.class.getMethod("close", new Class[] {})};
         
         PREPARED_STATEMENT_METHODS_WHITE_LIST = new Method[]{
               PreparedStatement.class.getMethod("close", new Class[] {})};
         
         RESULT_SET_METHODS_WHITE_LIST = new Method[]{
               ResultSet.class.getMethod("close", new Class[] {})};
      }
      catch(NoSuchMethodException x)
      {
         throw new InternalException(x.getMessage(), x);
      }
   }
   
   public static Connection newInstance(Connection connection)
   {
      return (Connection) newInstance(connection, new Class[] { Connection.class },
            new TransactionFreezeInfo(), CONNECTIONS_METHOD_WHITE_LIST);
   }
   
   private static Object newInstance(Object object, Class[] interfaces,
         TransactionFreezeInfo txFreezeInfo, Method[] methodsWhiteList)
   {
      return Proxy.newProxyInstance(
            object.getClass().getClassLoader(), interfaces, 
            new JdbcProxy(object, txFreezeInfo, methodsWhiteList));
   }
   
   private JdbcProxy(Object object, TransactionFreezeInfo txFreezeInfo,
         Method[] methodsWhiteList)
   {
      super(txFreezeInfo, methodsWhiteList);
      this.object = object;
   }
   
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      Object result = super.invoke(proxy, method, args);

      Class[] interfaces = null;
      Method[] methodWhiteList = null;
      
      if (result instanceof PreparedStatement)
      {
         interfaces = new Class[] { PreparedStatement.class };
         methodWhiteList = PREPARED_STATEMENT_METHODS_WHITE_LIST;
      }
      else if (result instanceof Statement)
      {
         interfaces = new Class[] { Statement.class };
         methodWhiteList = STATEMENT_METHODS_WHITE_LIST;
      }
      else if (result instanceof ResultSet)
      {
         interfaces = new Class[] { ResultSet.class };
         methodWhiteList = RESULT_SET_METHODS_WHITE_LIST;
      }

      if (null != interfaces)
      {
         result = newInstance(result, interfaces, txFreezeInfo,
               null != methodWhiteList ?  methodWhiteList : EMPTY_METHODS_WHITE_LIST);
      }
      
      return result;
   }
}
