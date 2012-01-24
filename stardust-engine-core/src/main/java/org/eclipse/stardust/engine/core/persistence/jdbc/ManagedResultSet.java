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
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * Helper class allowing for safe usage of ad-hoc queries. The only purpose is to close
 * the statement producing the record set as soon as the record set itself is closed.
 *
 * @author fherinean
 * @version $Revision$
 */
public final class ManagedResultSet
{
   private static final Logger trace = LogManager.getLogger(ManagedResultSet.class);

   public static ResultSet createManager(final Statement statement, final ResultSet resultSet)
   {
      Method method = null;
      try
      {
         method = ResultSet.class.getMethod("close");
      }
      catch (Exception e)
      {
         trace.debug("No close method found in ResultSet.", e);
      }
      final Method closeMethod = method;
      return (ResultSet) Proxy.newProxyInstance(
            ResultSet.class.getClassLoader(),
            new Class[] {ResultSet.class},
            new InvocationHandler()
            {
               public Object invoke(Object proxy, Method method, Object[] args)
                     throws Throwable
               {
                  try
                  {
                     return method.invoke(resultSet, args);
                  }
                  finally
                  {
                     if (method.equals(closeMethod))
                     {
                        try
                        {
                           statement.close();
                        }
                        catch (SQLException e)
                        {
                           trace.debug("Ignoring error during statement close", e);
                        }
                     }
                  }
               }
            });
   }

   private ManagedResultSet()
   {
   }
}
