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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.TimeMeasure;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInterceptor;
import org.eclipse.stardust.engine.core.runtime.interceptor.MethodInvocation;
import org.eclipse.stardust.engine.core.runtime.logging.ISqlTimeRecorder;
import org.eclipse.stardust.engine.core.runtime.logging.RuntimeLog;


/**
 * @author ubirkemeyer
 * @version $Revision$
 */
public class DebugInterceptor implements MethodInterceptor
{
   private static final long serialVersionUID = 2L;
   
   public static final Logger trace = LogManager.getLogger(DebugInterceptor.class);
   private static final ThreadLocalSqlTimeRecorder sqlTimeRecorder = new ThreadLocalSqlTimeRecorder();

   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      final Method method = invocation.getMethod();
      final String methodName = method.getName();
      final String fqMethodName = method.getDeclaringClass().getName() + "." + methodName;

      trace.info("--> " + methodName);

      final TimeMeasure timer = new TimeMeasure();
      final Parameters parameters = Parameters.instance();
      parameters.set(ISqlTimeRecorder.PRP_SQL_TIME_RECORDER, sqlTimeRecorder);
      
      try
      {
         return invocation.proceed();
      }
      finally
      {
         final long diffTime = timer.stop().getDurationInMillis();

         final Parameters params = parameters;
         if (diffTime >= params.getLong(
               KernelTweakingProperties.SERVICE_CALL_TRACING_THRESHOLD, Long.MAX_VALUE))
         {
            final long cumulatedSqlTime = sqlTimeRecorder.getCumulatedSqlExecutionTime();
            final long fetchingTime = sqlTimeRecorder.getCumulatedFetchTime();
            final String uniqueIdentifier = sqlTimeRecorder.getUniqueIdentifier();
            
            RuntimeLog.PERFORMANCE.info("Service call: " + diffTime + " ms for '"
                  + fqMethodName + "'. (call ID: " + uniqueIdentifier + "; total SQL: "
                  + cumulatedSqlTime + " ms, total fetching time: " + fetchingTime
                  + " ms)");
            if ( !sqlTimeRecorder.getSqlExcecutionTimes().isEmpty())
            {
               RuntimeLog.PERFORMANCE
                     .info("List of recorded SQL statements for this service call:");
               for (Iterator iter = sqlTimeRecorder.getSqlExcecutionTimes().iterator(); iter
                     .hasNext();)
               {
                  Pair pair = (Pair) iter.next();
                  RuntimeLog.PERFORMANCE.info("\tcall ID: " + uniqueIdentifier + "; "
                        + pair.getSecond() + " ms: " + pair.getFirst());
               }
            }
            
         }
         
         parameters.set(ISqlTimeRecorder.PRP_SQL_TIME_RECORDER, null);
         sqlTimeRecorder.reset();

         trace.info("<-- " + methodName);
      }
   }
   
   public static void addSqlExecutionTime(String sql, long time)
   {
      sqlTimeRecorder.record(sql, time);
   }
   
   private static final class RecorderData
   {
      private List<Long> fetchTimes;
      private List<Pair<String, Long>> sqlExcecutionTimes;
      private String uniqueIdentifier;

      public RecorderData()
      {
         super();
         this.fetchTimes = CollectionUtils.newArrayList();
         this.sqlExcecutionTimes = CollectionUtils.newArrayList();
         this.uniqueIdentifier = UUID.randomUUID().toString();
      }
   }
   
   /**
    * This SQL recorder is thread local and will be used to store all executed SQL statements
    * with their execution duration during a service call.
    * 
    * @author born
    * @version $Revision$
    * 
    */
   private static final class ThreadLocalSqlTimeRecorder extends
         ThreadLocal<RecorderData> implements ISqlTimeRecorder
   {
      public ThreadLocalSqlTimeRecorder()
      {
         super();
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.engine.core.runtime.logging.ISqlTimeRecorder#record(java.lang.String, long)
       */
      public void record(String sql, long time)
      {
         final List<Pair<String, Long>> sqlList = getSqlExcecutionTimes();
         sqlList.add(new Pair(sql, new Long(time)));
      }
      
      public void record(long duration)
      {
         final List<Long> otherSqlList = getFetchTimes();
         otherSqlList.add(new Long(duration));
      }
      
      public String getUniqueIdentifier()
      {
         return get().uniqueIdentifier;
      }

      /* (non-Javadoc)
       * @see java.lang.ThreadLocal#initialValue()
       */
      protected RecorderData initialValue()
      {
         return new RecorderData();
      }
      
      /**
       * @return a correctly casted version of the stored object retrieved from {@link #get()}. 
       */
      private List<Pair<String, Long>> getSqlExcecutionTimes()
      {
         return get().sqlExcecutionTimes;
      }
      
      private List<Long> getFetchTimes()
      {
         return get().fetchTimes;
      }

      /**
       * @return the cumulated duration for all recorded SQL statements.
       */
      private long getCumulatedSqlExecutionTime()
      {
         long cumulatedTime = 0;
         for (Pair<String, Long> sqlExecutionTime : getSqlExcecutionTimes())
         {
            cumulatedTime += sqlExecutionTime.getSecond();
         }
         
         return cumulatedTime;
      }
      
      private long getCumulatedFetchTime()
      {
         long cumulatedTime = 0;
         for (Long fetchTime : getFetchTimes())
         {
            cumulatedTime += fetchTime;
         }
         
         return cumulatedTime;
      }

      /**
       * Reset the thread local instance. 
       */
      private void reset()
      {
         set(initialValue());
      }

   }

}
