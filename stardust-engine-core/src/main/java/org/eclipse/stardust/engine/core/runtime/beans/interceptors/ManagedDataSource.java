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

import java.sql.SQLException;

import javax.sql.DataSource;

import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;


/**
 * @author ubirkemeyer
 */
public class ManagedDataSource implements ManagedResource
{
   private static final Logger trace = LogManager.getLogger(ManagedDataSource.class);

   private final DataSource dataSource;

   public ManagedDataSource(DataSource dataSource)
   {
      this.dataSource= dataSource;
   }

   public DataSource getDataSource()
   {
      return dataSource;
   }

   public void commit()
   {
      try
      {
         dataSource.getConnection().commit();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
      finally
      {
         try
         {
            dataSource.getConnection().close();
         }
         catch (SQLException e)
         {
            trace.warn("", e);
         }
      }
   }

   public void rollback()
   {
      try
      {
         dataSource.getConnection().rollback();
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
      finally
      {
         try
         {
            dataSource.getConnection().close();
         }
         catch (SQLException e)
         {
            trace.warn("", e);
         }
      }
   }
}
