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
import java.util.Map;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.QueryUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.Session;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.TypeDescriptor;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;


/**
 * Implementation of SequenceGenerator that retrieves sequence numbers in a batch
 * and caches the retrieved sequences separately for every entity type
 */
public class FastCachingSequenceGenerator extends ThreadLocal implements SequenceGenerator
{

   private static final Logger trace = LogManager.getLogger(CachingSequenceGenerator.class);

   private DBDescriptor dbDescriptor;

   private SqlUtils sqlUtils;
   
   private final int sequenceBatchSize;

   public FastCachingSequenceGenerator()
   {
      this.sequenceBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.SEQUENCE_BATCH_SIZE, 100);
   }

   protected Object initialValue()
   {
      // thread local type descriptor -> sequence cache mapping
      return CollectionUtils.newHashMap();
   }

   public void init(DBDescriptor dbDescriptor, SqlUtils sqlUtils)
   {
      if ( !dbDescriptor.supportsSequences())
      {
         throw new InternalException("Database Type '"
               + dbDescriptor.getClass().getName() + "' does not support sequences");
      }

      this.dbDescriptor = dbDescriptor;
      this.sqlUtils = sqlUtils;
   }

   public long getNextSequence(TypeDescriptor typeDescriptor, Session session)
   {
      // retrieve thread local sequence cache registry
      final Map sequenceCacheRegistry = (Map) get();
      
      SequenceCache sequenceCache = (SequenceCache) sequenceCacheRegistry.get(typeDescriptor);
      if (null == sequenceCache)
      {
         sequenceCache = new SequenceCache(sequenceBatchSize);
         
         sequenceCacheRegistry.put(typeDescriptor, sequenceCache);
      }

      // check if there is at least one id in the cache
      if (sequenceCache.isEmpty())
      {
         // obtain fresh set of IDs from the DB
         fillSequenceCache(typeDescriptor, session, sequenceCache);
         
         Assert.condition(0 == sequenceCache.idxNextVal);
      }

      final long sequenceValue = sequenceCache.getNext();

      if (trace.isDebugEnabled())
      {
         trace.debug("Returning unique ID: " + sequenceValue);
      }
      
      return sequenceValue;
   }

   private void fillSequenceCache(TypeDescriptor typeDescriptor, Session session,
         SequenceCache sequenceCache)
   {
      Field[] pkFields = typeDescriptor.getPkFields();

      Assert.condition(1 == pkFields.length,
            "Automatic PK values are only supported for types with a single PK field.");

      // pretend to only fetch one sequence value, but we will assume the sequence increment is at least sequenceBatchSize 
      String createPKStmt = dbDescriptor.getCreatePKStatement(sqlUtils.getSchemaName(),
            typeDescriptor.getPkSequence(), 1);

      Statement stmt = null;
      ResultSet rs = null;
      try
      {
         Connection connection = session.getConnection();

         if (session.isUsingPreparedStatements(typeDescriptor.getType()))
         {
            try
            {
               stmt = connection.prepareStatement(createPKStmt);
            }
            catch (SQLException x)
            {
               throw new InternalException(x);
            }
            rs = ((PreparedStatement) stmt).executeQuery();
         }
         else
         {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(createPKStmt);
         }

         if (rs.next())
         {
            final long newPk = rs.getLong(1);
            
            for (int i = 0; i < sequenceBatchSize; i++ )
            {
               sequenceCache.cachedValues[i] = (newPk + i);
            }
            sequenceCache.idxNextVal = 0;
         }
         else
         {
            throw new PublicException("Failed obtaining new sequence values, result set was empty.");
         }
      }
      catch (SQLException e)
      {
         throw new InternalException(e);
      }
      finally
      {
         QueryUtils.closeStatementAndResultSet(stmt, rs);
      }
   }

   private static class SequenceCache
   {
      private int idxNextVal;
      
      private final long[] cachedValues;
      
      public SequenceCache(int cacheSize)
      {
         this.cachedValues = new long[cacheSize];
         
         this.idxNextVal = cachedValues.length;
      }
      
      public boolean isEmpty()
      {
         return (idxNextVal == cachedValues.length);
      }
      
      public long getNext()
      {
         if (isEmpty())
         {
            throw new InternalException(
                  "Must not retrieve a value from empty sequence cache.");
         }
         
         return cachedValues[idxNextVal++];
      }
   }
   
}
