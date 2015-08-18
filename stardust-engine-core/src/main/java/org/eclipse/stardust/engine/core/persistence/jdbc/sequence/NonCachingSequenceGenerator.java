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
package org.eclipse.stardust.engine.core.persistence.jdbc.sequence;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.DmlManager;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;


/**
 * Implementation of SequenceGenerator that does NOT cache and does NOT 
 * support batch retrieval of sequence numbers, they are always retrieved 
 * one by one
 */
public class NonCachingSequenceGenerator implements SequenceGenerator
{

   private static final Logger trace = LogManager.getLogger(NonCachingSequenceGenerator.class);

   private DBDescriptor dbDescriptor;

   private SqlUtils sqlUtils;

   public NonCachingSequenceGenerator()
   {
   }

   public void init(DBDescriptor dbDescriptor, SqlUtils sqlUtils)
   {
      if ( !dbDescriptor.supportsSequences())
      {
         throw new InternalException("Database Type '"+dbDescriptor.getClass().getName()+"' does not support sequences");
      }
      this.dbDescriptor = dbDescriptor;
      this.sqlUtils = sqlUtils;
   }

   public long getNextSequence(TypeDescriptor typeDescriptor, Session session)
   {
      long uniqueId = this.getNextSequenceImpl(typeDescriptor, session);
      if (trace.isDebugEnabled())
      {
         trace.debug("returning unique ID: " + uniqueId);
      }
      return uniqueId;
   }

   private long getNextSequenceImpl(TypeDescriptor typeDescriptor, Session session)
   {
      Field[] pkFields = typeDescriptor.getPkFields();

      Assert.condition(1 == pkFields.length,
            "Automatic PK values are only supported for types with a single PK field.");

      String createPKStmt = dbDescriptor.getCreatePKStatement(sqlUtils.getSchemaName(),
            typeDescriptor.getPkSequence());

      Field field = pkFields[0];
      if (session.isUsingPreparedStatements(typeDescriptor.getType()))
      {
         PreparedStatement pkStatement = null;
         try
         {
            Connection connection = session.getConnection();
            PreparedStatement result;
            try
            {
               result = connection.prepareStatement(createPKStmt);
            }
            catch (SQLException x)
            {
               throw new InternalException(x);
            }
            pkStatement = result;
            ResultSet pkQuery = pkStatement.executeQuery();
            pkQuery.next();
            return ((Number) DmlManager.getJavaValue(field.getType(), typeDescriptor
                  .getPersistentField(field).getLength(), pkQuery, 1, true, false))
                  .longValue();
         }
         catch (SQLException e)
         {
            throw new InternalException(e);
         }
         finally
         {
            QueryUtils.closeStatement(pkStatement);
         }
      }
      else
      {
         Statement pkStmt = null;
         try
         {
            Connection connection = session.getConnection();
            pkStmt = connection.createStatement();
            ResultSet pkQuery = pkStmt.executeQuery(createPKStmt);
            pkQuery.next();
            return ((Number) DmlManager.getJavaValue(field.getType(), typeDescriptor
                  .getPersistentField(field).getLength(), pkQuery, 1, true, false))
                  .longValue();
         }
         catch (SQLException e)
         {
            throw new InternalException(e);
         }
         finally
         {
            QueryUtils.closeStatement(pkStmt);
         }
      }
   }

}
