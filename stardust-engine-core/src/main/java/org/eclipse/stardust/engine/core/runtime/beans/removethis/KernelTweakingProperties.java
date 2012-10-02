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
package org.eclipse.stardust.engine.core.runtime.beans.removethis;

import org.eclipse.stardust.common.annotations.ConfigurationProperty;
import org.eclipse.stardust.common.annotations.PropertyValueType;
import org.eclipse.stardust.common.annotations.Status;
import org.eclipse.stardust.common.annotations.UseRestriction;
import org.eclipse.stardust.engine.core.spi.cluster.ClusterSafeObjectProvider;
import org.eclipse.stardust.engine.core.spi.jca.HazelcastJcaConnectionFactoryProvider;

/**
 * @author rsauer
 * @version $Revision$
 */
public final class KernelTweakingProperties
{
   public static final String XPDL_MODEL_DEPLOYMENT = "Carnot.Engine.Deployment.XPDL";
   
   public static final String RELOAD_MODEL_MANAGER_AFTER_MODEL_OPERATION =
      "Carnot.Engine.ReloadModelManagerAfterModelOperation";
   
   public static final String EJB_ROLLBACK_ON_ERROR =
      "Carnot.Engine.Ejb.RollbackOnError";
   
   public static final String EJB_ROLLBACK_ON_ERROR_ALWAYS =
      "always";
   public static final String EJB_ROLLBACK_ON_ERROR_LENIENT =
      "lenient";
   public static final String EJB_ROLLBACK_ON_ERROR_NEVER =
      "never";

   public static final String APPLICATION_EXCEPTION_PROPAGATION =
      "Carnot.Engine.ErrorHandling.ApplicationExceptionPropagation";
   
   public static final String APPLICATION_EXCEPTION_PROPAGATION_NEVER = "never";
   public static final String APPLICATION_EXCEPTION_PROPAGATION_ON_ROLLBACK = "onRollback";
   public static final String APPLICATION_EXCEPTION_PROPAGATION_ALWAYS = "always";
   
   /* boolean property */
   public static final String ASYNC_PROCESS_COMPLETION =
         "Carnot.Engine.Threading.AsyncProcessCompletion";
   
   public static final String AUTOMATIC_TOKEN_CLEANUP =
      "Carnot.Engine.Tuning.ProcessCompletionTokenCleanup";

   /**
    * TODO: describe
    */
   public static final String ACTIVITY_THREAD_RETRY_COUNT = "Carnot.Engine.Tuning.ActivityThread.Retries";
   
   /**
    * TODO: describe
    */
   public static final String ACTIVITY_THREAD_RETRY_PAUSE = "Carnot.Engine.Tuning.ActivityThread.Pause";

   public static final String PROCESS_COMPLETION_TOKEN_COUNT_TIMEOUT =
      "Carnot.Engine.Tuning.ProcessCompletionTokenCountTimeout";

   public static final String FIND_DAEMON_LOG_QUERY_TIMEOUT =
      "Carnot.Engine.Tuning.FindDaemonLogQueryTimeout";

   public static final String SLOW_STATEMENT_TRACING_THRESHOLD =
         "Carnot.Engine.Tuning.DB.slowStatementTracingThreshold";
   
   public static final String CLOB_READ_BUFFER_SIZE =
         "Carnot.Engine.Tuning.DB.clobReadBufferSize";

   public static final String SERVICE_CALL_TRACING_THRESHOLD =
         "Carnot.Engine.Tuning.ServiceCallTracingThreshold";
   
   public static final String DATA_FILTER_HINT =
         "Carnot.Engine.Tuning.DB.dataFilterHint";

   public static final String SINGLE_PARTITION = "Carnot.Engine.Tuning.DB.singlePartition";

   public static final String QUERY_EVALUATION_PROFILE =
         "Carnot.Engine.Tuning.Query.EvaluationProfile";

   public static final String QUERY_EVALUATION_PROFILE_DEFAULT = "default";
   public static final String QUERY_EVALUATION_PROFILE_INLINED = "inlined";
   public static final String QUERY_EVALUATION_PROFILE_CLUSTERED = "dataClusters";
   public static final String QUERY_EVALUATION_PROFILE_LEGACY = "legacy";

   public static final String PROCESS_PREFETCH_N_PARALLEL_INSTANCES =
      "Carnot.Engine.Tuning.Query.ProcessPrefetchNParallelInstances";
   public static final String USER_PREFETCH_N_PARALLEL_INSTANCES =
      "Carnot.Engine.Tuning.Query.UserPrefetchNParallelInstances";
   public static final String DESCRIPTOR_PREFETCH_N_PARALLEL_INSTANCES =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchNParallelInstances";
   public static final String DESCRIPTOR_PREFETCH_N_PARALLEL_DATA =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchNParallelData";
   public static final String DESCRIPTOR_PREFETCH_DATA_DISCRIMINATION_THRESHOLD =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchDataDiscriminationThreshold";

   public static final String DESCRIPTOR_PREFETCH_BATCH_SIZE =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchBatchSize";

   public static final String DESCRIPTOR_PREFETCH_STRUCT_XML =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchStructXml";
   public static final String DESCRIPTOR_PREFETCH_STRUCT_INDEX =
      "Carnot.Engine.Tuning.Query.DescriptorPrefetchStructIndex";

   /**
    * Perform prefetch of data values using existing data cluster tables. Default: false.
    */
   public static final String DESCRIPTOR_PREFETCH_USE_DATACLUSTER =
         "Infinity.Engine.Tuning.Query.DescriptorPrefetchUseDataCluster";

   /**
    * @deprecated
    */
   public static final String DESCRIPTOR_PREFETCH_REUSE_FILTER_JOINS =
         "Infinity.Engine.Tuning.Query.DescriptorPrefetchReuseFilterJoins";

   /**
    * Do NOT use in properties file, will be used internally only!
    * 
    * @deprecated
    */
   public static final String DESCRIPTOR_PREFETCHED_RTOID_SET =
      "Infinity.Engine.Tuning.Query.DescriptorPrefetched.RtOidList";

   public static final String INLINE_PROCESS_OID_THRESHOLD =
      "Carnot.Engine.Tuning.Query.InlineProcessOidThreshold";

   public static final String LAST_MODIFIED_TIMESTAMP_EPSILON =
      "Carnot.Engine.Tuning.Query.LastModifiedTimestampEpsilon";

   public static final String EVENT_TIME_OVERRIDABLE = "Carnot.Engine.OverridableTimeStamps";

   public static final String CACHE_PARTITIONS = "Carnot.Engine.Tuning.CachePartitions";

   public static final String CACHE_DOMAINS = "Carnot.Engine.Tuning.CacheDomains";

   public static final String SHARED_CONTEXT_CACHE = "Carnot.Engine.Tuning.SharedContextCache";
   
   public static final String SEQUENCE_BATCH_SIZE = "Carnot.Engine.Tuning.SequenceBatchSize";

   public static final String SINGLE_NODE_DEPLOYMENT = "Carnot.Engine.Tuning.SingleNodeDeployment";
   
   public static final String GET_COUNT_DEFAULT_TIMEOUT = "Carnot.Engine.Tuning.GetCountDefaultTimeout";
   
   public static final String RUNTIME_TIMESTAMP_PROVIDER = "Carnot.Engine.RuntimeTimestampProvider";
   
   public static final String USE_CONTEXT_CLASSLOADER = "Carnot.Engine.Classloading.UseContextClassloader";

   public static final String OPTIMIZE_COUNT_ONLY_SUBSET_POLICY =
      "Carnot.Engine.Tuning.Query.SubsetPolicy.OptimizeCountOnly";

   public static final String DELETE_PI_STMT_BATCH_SIZE = 
         "Carnot.Engine.Tuning.DeleteProcessInstances.StatementBatchSize";
   
   /**
    * With this property the column length for string columns can be configured. 
    * The property format is 
    * <code>Carnot.Db.ColumnLength.&lt;table_name&gt;.&lt;column_name&gt;=&lt;int value&gt;</code>.
    */
   public static final String STRING_COLUMN_LENGTH_PREFIX = 
      "Carnot.Db.ColumnLength.";
   
   public static final String CONTENT_STREAMING_THRESHOLD = "Carnot.Configuration.ContentStreamingThreshold";
   
   public static final String CONFIGURATION_CACHE_TTL = "Carnot.Configuration.CacheTTL";
   
   public static final String CONFIGURATION_MAXIMUM_CACHE_ITEMS = "Carnot.Configuration.MaximumCacheItems";
   
   // Only used for white box tests. Setting this to TRUE will set AIs/PIs to state ABORTING 
   // but will not run code to set it to ABORTED afterwards.
   public static final String PREVENT_ABORTING_TO_ABORTED_STATE_CHANGE = "Infinity.Engine.Threading.PreventAbortion";
   
   public static final String SORT_ACTIVITIES_IN_TRANSITION_ORDER = "Infinity.Engine.Process.SortActivitiesInTransitionOrder";
   
   public static final String EXTERNAL_CACHING = "Infinity.Engine.Caching";
   
   public static final String SHARED_CACHING = "Infinity.Engine.Caching.Shared";

   public static final String CACHE_FACTORY_OVERRIDE = "Infinity.Engine.Caching.CacheFactory";
   
   public static final String SUPPORT_TRANSIENT_PROCESSES = "Carnot.Engine.Tuning.SupportTransientProcesses";
   
   public static final String SUPPORT_TRANSIENT_PROCESSES_ON = "on";
   public static final String SUPPORT_TRANSIENT_PROCESSES_ALWAYS_TRANSIENT = "alwaysTransient";
   public static final String SUPPORT_TRANSIENT_PROCESSES_ALWAYS_DEFERRED = "alwaysDeferred";
   public static final String SUPPORT_TRANSIENT_PROCESSES_OFF = "off";
   
   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Internal)
   @PropertyValueType(ClusterSafeObjectProvider.class)
   public static final String CLUSTER_SAFE_OBJ_PROVIDER = "Carnot.Engine.Tuning.SupportTransientProcesses.ClusterSafeObjectProvider";
   
   @ConfigurationProperty(status = Status.Stable, useRestriction = UseRestriction.Public)
   @PropertyValueType(HazelcastJcaConnectionFactoryProvider.class)
   public static final String HZ_JCA_CONNECTION_FACTORY_PROVIDER = "Carnot.Engine.Hazelcast.JcaConnectionFactoryProvider";
   
   public static final String ASSIGN_TO_INVALID_USER = "Infinity.Engine.Activity.AssignToInvalidUser";   

   public static final String INFINITY_DMS_SHARED_DATA_EXIST = "Infinity.Dms.SharedDataExist";
}