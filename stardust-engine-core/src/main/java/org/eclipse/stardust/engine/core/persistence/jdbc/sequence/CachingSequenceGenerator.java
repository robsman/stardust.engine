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
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.jdbc.*;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;


/**
 * Implementation of SequenceGenerator that retrieves sequence numbers in a batch
 * and caches the retrieved sequences separately for every entity type
 */
public class CachingSequenceGenerator implements SequenceGenerator
{

   private static final Logger trace = LogManager.getLogger(CachingSequenceGenerator.class);

   private DBDescriptor dbDescriptor;

   private SqlUtils sqlUtils;

   private final ConcurrentHashMap typeDescriptorToIdCache;

   private final int sequenceBatchSize;

   public CachingSequenceGenerator()
   {
      this.typeDescriptorToIdCache = new ConcurrentHashMap();

      this.sequenceBatchSize = Parameters.instance().getInteger(
            KernelTweakingProperties.SEQUENCE_BATCH_SIZE, 100);
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

   public long getNextSequence(final TypeDescriptor typeDescriptor, final Session session)
   {
      // check if there is an id in the cache
      ConcurrentLinkedQueue cachedIds = (ConcurrentLinkedQueue) this.typeDescriptorToIdCache.get(typeDescriptor);

      if (null == cachedIds)
      {
         typeDescriptorToIdCache.putIfAbsent(typeDescriptor, new ConcurrentLinkedQueue());

         cachedIds = (ConcurrentLinkedQueue) this.typeDescriptorToIdCache.get(typeDescriptor);
      }

      Number cachedId = (Number) cachedIds.poll();
      if (cachedId == null)
      {
         synchronized (cachedIds)
         {
            cachedId = (Number) cachedIds.poll();
            if (cachedId == null)
            {
               List< ? > newIds = null;

               // MYSQL_SEQ needs isolated transaction to prevent sequence rollback.
               if (dbDescriptor instanceof MySqlSeqDbDescriptor)
               {
                  newIds = runIsolateAction(new Action<List< ? >>()
                  {

                     @Override
                     public List< ? > execute()
                     {
                        List newIds = getNextSequenceImpl(typeDescriptor, session);
                        return newIds;
                     }
                  });
               }
               else
               {
                  newIds =  getNextSequenceImpl(typeDescriptor, session);
               }
               Assert.condition(0 < newIds.size());

               // reserve first for own use
               cachedId = (Number) newIds.remove(0);
               cachedIds.addAll(newIds);
            }
         }
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("returning unique ID: " + cachedId.longValue());
      }

      return cachedId.longValue();
   }

   private List getNextSequenceImpl(TypeDescriptor typeDescriptor, Session session)
   {
      Field[] pkFields = typeDescriptor.getPkFields();

      Assert.condition(1 == pkFields.length,
            "Automatic PK values are only supported for types with a single PK field.");

      String createPKStmt = dbDescriptor.getCreatePKStatement(sqlUtils.getSchemaName(),
            typeDescriptor.getPkSequence(), this.sequenceBatchSize);

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
            List newIds = new LinkedList();
            while (pkQuery.next())
            {
               newIds.add(DmlManager.getJavaValue(field.getType(), typeDescriptor
                     .getPersistentField(field).getLength(), pkQuery, 1, true, false));
            }
            return newIds;
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
            List newIds = new LinkedList();
            while (pkQuery.next())
            {
               newIds.add(DmlManager.getJavaValue(field.getType(), typeDescriptor
                     .getPersistentField(field).getLength(), pkQuery, 1, true, false));
            }
            return newIds;
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

   private static <T extends Object> T runIsolateAction(Action<T> action)
   {
      ForkingServiceFactory factory = null;
      ForkingService service = null;
      try
      {
         factory = (ForkingServiceFactory) Parameters.instance().get(
               EngineProperties.FORKING_SERVICE_HOME);
         service = factory.get();
         return (T) service.isolate(action);
      }
      finally
      {
         if (null != factory)
         {
            factory.release(service);
         }
      }
   }

}
