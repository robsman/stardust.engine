/**********************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.transientpi;

import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.FastCachingSequenceGenerator;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.SequenceGenerator;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.api.util.WaitTimeout;
import org.eclipse.stardust.test.transientpi.TransientProcessInstanceTest.ProcessExecutor;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class is not part of the common sanity test suite (i.e. is not executed automatically on a regular basis),
 * but helps to profile transient process instance execution.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceProfilingTest
{
   private static final Log LOG = LogFactory.getLog(TransientProcessInstanceProfilingTest.class);
   
   private static final String SEQUENCE_BATCH_SIZE_STRING = "5000";
   
   private static final Long SEQUENCE_BATCH_SIZE = Long.valueOf(SEQUENCE_BATCH_SIZE_STRING);
   
   private static final String DEFER_JDBC_CONNECTION_RETRIEVAL_PROPERTY_KEY = "Carnot.Engine.Tuning.Spring.DeferJdbcConnectionRetrieval";
   
   private static final String ALL_USERS_WILDCARD_PROPERTY_VALUE = "*";
   
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);

   static
   {
      Logger.getRootLogger().setLevel(Level.WARN);      
   }
   
   @BeforeClass
   public static void setUpOnce() throws SQLException
   {
      System.setProperty(TransientProcessInstanceTest.HAZELCAST_LOGGING_TYPE_KEY, TransientProcessInstanceTest.HAZELCAST_LOGGING_TYPE_VALUE);
      
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ON);
      params.set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, false);
      params.set(KernelTweakingProperties.HZ_JCA_CONNECTION_FACTORY_PROVIDER, SpringAppContextHazelcastJcaConnectionFactoryProvider.class.getName());
      params.set(DEFER_JDBC_CONNECTION_RETRIEVAL_PROPERTY_KEY, true);
      params.set(SessionManager.PRP_SESSION_NO_TRACKING, ALL_USERS_WILDCARD_PROPERTY_VALUE);
      params.set(SecurityProperties.LOGIN_USERS_WITHOUT_LOGIN_LOGGING, ALL_USERS_WILDCARD_PROPERTY_VALUE);

      incrementDbSequenceSize();
      injectSequenceGenerator(params);
   }

   @AfterClass
   public static void tearDownOnce()
   {
      System.clearProperty(TransientProcessInstanceTest.HAZELCAST_LOGGING_TYPE_KEY);
   }
   
   @Test
   public void profileIt() throws Exception
   {
      warmUp();

      final long startTime = System.currentTimeMillis();
      doTest();
      final long endTime = System.currentTimeMillis();
      final long duration = endTime - startTime;
      
      LOG.warn("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX " + duration + " XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
   }
   
   private static void incrementDbSequenceSize() throws SQLException
   {
      final Connection connection = testClassSetup.dataSource().getConnection();
      final Statement stmt = connection.createStatement();
      stmt.addBatch("ALTER SEQUENCE activity_instance_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE activity_instance_log_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE act_inst_property_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE trans_inst_seq  INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE trans_token_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE daemon_log_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE data_value_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE department_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE event_binding_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE log_entry_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE property_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE timer_log_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE usergroup_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE usergroup_property_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE user_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE user_property_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE wfuser_session_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE user_participant_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE user_usergroup_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE process_instance_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE proc_inst_property_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE structured_data_value_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE domain_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE domain_hierarchy_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE wfuser_domain_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE wfuser_realm_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE clob_data_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE model_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE STRING_DATA_SEQ INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE partition_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.addBatch("ALTER SEQUENCE link_type_seq INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
      stmt.executeBatch();
   }
   
   private static void injectSequenceGenerator(final Parameters params)
   {
      params.set(KernelTweakingProperties.SEQUENCE_BATCH_SIZE, SEQUENCE_BATCH_SIZE);
      final SequenceGenerator sequenceGenerator = new FastCachingSequenceGenerator();
      final DBDescriptor dbDescriptor = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      final SqlUtils sqlUtils = new SqlUtils(params.getString(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX), dbDescriptor);
      sequenceGenerator.init(dbDescriptor, sqlUtils);
      
      params.set(SequenceGenerator.UNIQUE_GENERATOR_PARAMETERS_KEY, sequenceGenerator);
   }
   
   private void warmUp() throws Exception
   {
      doTest();
   }
   
   private void doTest() throws InterruptedException, TimeoutException, ExecutionException, SQLException
   {
      final int nThreads = 100;

      final Set<ProcessExecutor> processExecutors = TransientProcessInstanceTest.initProcessExecutors(nThreads, sf.getWorkflowService());

      final List<Future<Long>> piOids = TransientProcessInstanceTest.executeProcesses(nThreads, processExecutors);
      
      ProcessInstanceStateBarrier.setTimeout(new WaitTimeout(1, TimeUnit.MINUTES));
      for (final Future<Long> f : piOids)
      {
         ProcessInstanceStateBarrier.instance().await(f.get(), ProcessInstanceState.Completed);
      }
   }
}
