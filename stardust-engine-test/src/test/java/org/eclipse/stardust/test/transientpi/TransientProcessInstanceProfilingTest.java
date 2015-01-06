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

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.MODEL_ID;

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
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.core.persistence.jdbc.DBDescriptor;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionFactory;
import org.eclipse.stardust.engine.core.persistence.jdbc.SessionProperties;
import org.eclipse.stardust.engine.core.persistence.jdbc.SqlUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.FastCachingSequenceGenerator;
import org.eclipse.stardust.engine.core.persistence.jdbc.sequence.SequenceGenerator;
import org.eclipse.stardust.engine.core.runtime.beans.Constants;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.engine.core.runtime.internal.SessionManager;
import org.eclipse.stardust.engine.spring.integration.jca.SpringAppContextHazelcastJcaConnectionFactoryProvider;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.api.util.WaitTimeout;
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
public class TransientProcessInstanceProfilingTest extends AbstractTransientProcessInstanceTest
{
   private static final Log LOG = LogFactory.getLog(TransientProcessInstanceProfilingTest.class);

   private static final String SEQUENCE_BATCH_SIZE_STRING = "5000";

   private static final Long SEQUENCE_BATCH_SIZE = Long.valueOf(SEQUENCE_BATCH_SIZE_STRING);

   private static final String PK_SEQUENCE_ANNOTATION = "PK_SEQUENCE";

   private static final String DEFER_JDBC_CONNECTION_RETRIEVAL_PROPERTY_KEY = "Carnot.Engine.Tuning.Spring.DeferJdbcConnectionRetrieval";

   private static final String ALL_USERS_WILDCARD_PROPERTY_VALUE = "*";


   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);


   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);

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
      System.setProperty(HAZELCAST_LOGGING_TYPE_KEY, HAZELCAST_LOGGING_TYPE_VALUE);

      final GlobalParameters params = GlobalParameters.globals();
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
      System.clearProperty(HAZELCAST_LOGGING_TYPE_KEY);
   }

   public TransientProcessInstanceProfilingTest()
   {
      super(testClassSetup);
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
      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = testClassSetup.dataSource().getConnection();
         stmt = connection.createStatement();

         for (final Class<?> clazz : Constants.PERSISTENT_RUNTIME_CLASSES)
         {
            final String sequenceName = (String) Reflect.getStaticFieldValue(clazz, PK_SEQUENCE_ANNOTATION);
            if (sequenceName != null)
            {
               stmt.addBatch("ALTER SEQUENCE " + sequenceName + " INCREMENT BY " + SEQUENCE_BATCH_SIZE_STRING);
            }
         }
         stmt.executeBatch();
      }
      finally
      {
         if (stmt != null)
         {
            stmt.close();
         }
         if (connection != null)
         {
            connection.close();
         }
      }
   }

   private static void injectSequenceGenerator(final GlobalParameters params)
   {
      params.set(KernelTweakingProperties.SEQUENCE_BATCH_SIZE, SEQUENCE_BATCH_SIZE);
      final SequenceGenerator sequenceGenerator = new FastCachingSequenceGenerator();
      final DBDescriptor dbDescriptor = DBDescriptor.create(SessionFactory.AUDIT_TRAIL);
      final SqlUtils sqlUtils = new SqlUtils((String) params.get(SessionProperties.DS_NAME_AUDIT_TRAIL + SessionProperties.DS_SCHEMA_SUFFIX), dbDescriptor);
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

      final Set<ProcessExecutor> processExecutors = initProcessExecutors(nThreads, sf.getWorkflowService());

      final List<Future<Long>> piOids = executeProcesses(nThreads, processExecutors);

      ProcessInstanceStateBarrier.instance().setTimeout(new WaitTimeout(1, TimeUnit.MINUTES));
      for (final Future<Long> f : piOids)
      {
         ProcessInstanceStateBarrier.instance().await(f.get(), ProcessInstanceState.Completed);
      }
      ProcessInstanceStateBarrier.instance().setTimeout(null);
   }
}
