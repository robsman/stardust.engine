/*******************************************************************************
 * Copyright (c) 2011, 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.engine.core.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.eclipse.stardust.common.Assert;
import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.persistence.FetchPredicate;
import org.eclipse.stardust.engine.core.persistence.Persistent;
import org.eclipse.stardust.engine.core.persistence.ResultIterator;
import org.eclipse.stardust.engine.core.runtime.logging.ISqlTimeRecorder;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLogUtils;
import org.eclipse.stardust.engine.runtime.utils.TimestampProviderUtils;



// @todo (ub): perverse implementation, copies the whole result in advance.


/**
 * A result set iterator is only valid for an active session.
 */
public class ResultSetIterator implements ResultIterator
{
   public static final Logger trace = LogManager.getLogger(ResultSetIterator.class);

   private final int startIndex;
   private final int maxSize;

   private Iterator result;
   private boolean hasMore;
   
   private Long totalCount;

   /**
    *
    */
   public ResultSetIterator(Session session, Class type, ResultSet resultSet)
   {
      this(session, type, resultSet, 0, Integer.MAX_VALUE);
   }

   /**
    *
    */
   public ResultSetIterator(Session session, Class type, ResultSet resultSet,
         int startIndex, int extent)
   {
      this(session, type, false, resultSet, startIndex, extent, null, false);
   }

   /**
    *
    */
   public ResultSetIterator(Session session, Class type, boolean distinct,
         ResultSet resultSet, int startIndex, int extent, FetchPredicate predicate,
         boolean countAll)
   {
      int index = 0;
      final TimeMeasure timer = new TimeMeasure().start();
      try
      {
         Assert.condition(startIndex >= 0, "Index has to be >= 0.");
         if (trace.isDebugEnabled())
         {
            trace.debug("-->ResultSetIterator");
         }
         if (extent < 0)
         {
            extent = Integer.MAX_VALUE;
         }
   
         this.startIndex = startIndex;
         this.maxSize = extent;
   
         this.hasMore = false;
         
         if (0 == this.maxSize && !countAll)
         {
            result = Collections.EMPTY_LIST.iterator();
            if (trace.isDebugEnabled())
            {
               trace.debug("<--ResultSetIterator size: 0");
            }
         }
         else
         {
            // fetch skipped items
            
            // TODO extract distinct rows
            TypeDescriptor typeDescriptor = session.getTypeDescriptor(type);
            Set fetchedPks = distinct ? new HashSet() : null;
   
            boolean mayHaveMore = true;
   
            List createdObjects = new ArrayList(10);
            
            Persistent firstObject = null;
            while (index <= startIndex)
            {
               if (resultSet.next())
               {
                  // TODO load object only in case of non-null predicate
                  firstObject = session.createObjectFromRow(type, resultSet,
                        createdObjects, predicate);
                  if (firstObject != null)
                  {
                     if (distinct)
                     {
                        final Object pk = typeDescriptor.getIdentityKey(firstObject);
                        if (fetchedPks.contains(pk))
                        {
                           continue;
                        }
                        else
                        {
                           fetchedPks.add(pk);
                        }
                     }
                     ++index;
                  }
               }
               else
               {
                  mayHaveMore = false;
                  break;
               }
            }
   
            final List content;
            if (mayHaveMore)
            {
               content = new ArrayList(Math.min(this.maxSize, 1000));
               content.add(firstObject);
            }
            else
            {
               content = Collections.EMPTY_LIST;
            }
   
            int nFetchedObjects = content.size();
   
            while (mayHaveMore && (nFetchedObjects < this.maxSize))
            {
               if (resultSet.next())
               {
                  Persistent nextObject = session.createObjectFromRow(type, resultSet,
                        createdObjects, predicate);
                  if (nextObject != null)
                  {
                     if (distinct)
                     {
                        final Object pk = typeDescriptor.getIdentityKey(nextObject);
                        if (fetchedPks.contains(pk))
                        {
                           continue;
                        }
                        else
                        {
                           fetchedPks.add(pk);
                        }
                     }
                     content.add(nextObject);
                     ++index;
                     ++nFetchedObjects;
                  }
               }
               else
               {
                  mayHaveMore = false;
                  break;
               }
            }
   
            while (mayHaveMore && resultSet.next())
            {
               final Persistent nextObject = session.createObjectFromRow(type, resultSet,
                     createdObjects, predicate);
               if (null != nextObject)
               {
                  if (distinct)
                  {
                     final Object pk = typeDescriptor.getIdentityKey(nextObject);
                     if (fetchedPks.contains(pk))
                     {
                        continue;
                     }
                     else
                     {
                        fetchedPks.add(pk);
                     }
                  }
                  if (!countAll)
                  {
                     // it's enough to find one more item
                     this.hasMore = true;
                     break;
                  }
                  else
                  {
                     ++index;
                  }
               }
            }
   
            if (typeDescriptor.getLoader() != null)
            {
               Loader loader = typeDescriptor.getLoader();
//               trace.info("--->>>--- start loading");
               for (int i = 0; i < createdObjects.size(); i++)
               {
//                  trace.info("Loading " + createdObjects.get(i));
                  loader.load((Persistent) createdObjects.get(i));
               }
//               trace.info("---<<<--- end loading");
            }
            this.result = content.iterator();
            this.totalCount = countAll ? new Long(index) : null;
         }
      }
      catch (SQLException x)
      {
         throw new InternalException(x);
      }
      finally
      {
         QueryUtils.closeResultSet(resultSet);
         if (trace.isDebugEnabled())
         {
            trace.debug("<--ResultSetIterator size: " + index);
         }
         long diffTime = timer.stop().getDurationInMillis();
         ISqlTimeRecorder recorder = RuntimeLogUtils.getSqlTimeRecorder(Parameters.instance());
         recorder.record(diffTime);
      }
   }

   /**
    *
    */
   public boolean hasNext()
   {
      return result.hasNext();
   }

   /**
    *
    */
   public Object next()
   {
      return result.next();
   }

   /**
    *
    */
   public void remove()
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Releases the result set explicitely.
    */
   public void close()
   {
   }

   public int getStartIndex()
   {
      return startIndex;
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public boolean hasMore()
   {
      return hasMore;
   }
   
   public boolean hasTotalCount()
   {
      return null != totalCount;
   }

   public long getTotalCount() throws UnsupportedOperationException
   {
      if (null == totalCount)
      {
         throw new UnsupportedOperationException("Total item count not available.");
      }
      return totalCount.longValue();
   }
   
   public long getTotalCountThreshold()
   {
      return Long.MAX_VALUE;
   }
}
