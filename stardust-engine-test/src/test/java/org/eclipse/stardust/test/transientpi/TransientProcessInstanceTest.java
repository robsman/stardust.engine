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

import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.*;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.SystemException;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.spring.integration.cluster.SpringContainerClusteredEnvHazelcastObjectProvider;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.ProcessInstanceStateBarrier;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * <p>
 * Tests whether transient processes are <b>not</b> written to the database in case of
 * regular completion, but only in cases where an error during process execution occurs.
 * </p>
 * 
 * <p>
 * Plus, this class makes sure that non-transient processes are not affected by the
 * <i>Transient Process Instance</i> functionality.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class TransientProcessInstanceTest
{
   private static final String PI_BLOBS_HOLDER_FIELD_NAME = "piBlobsHolder";
   
   private static final String PI_BLOBS_FIELD_NAME = "piBlobs";
   
   private static final String PROCESS_EXECUTION_STATE = "Process.Execution.State";
   
   private static final String HAZELCAST_LOGGING_TYPE_KEY = "hazelcast.logging.type";
   private static final String HAZELCAST_LOGGING_TYPE_VALUE = "log4j";
   
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);
   
   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.JMS, MODEL_ID);
   
   @Rule
   public final TestRule chain = RuleChain.outerRule(sf)
                                          .around(testMethodSetup);
   
   @BeforeClass
   public static void setUpOnce()
   {
      System.setProperty(HAZELCAST_LOGGING_TYPE_KEY, HAZELCAST_LOGGING_TYPE_VALUE);
   }
   
   @AfterClass
   public static void tearDownOnce()
   {
      System.clearProperty(HAZELCAST_LOGGING_TYPE_KEY);
   }
   
   @Before
   public void setUp()
   {
      final Parameters params = Parameters.instance();
      params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.NOT_STARTED);
      params.set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 0);
      params.set(KernelTweakingProperties.CLUSTER_SAFE_OBJ_PROVIDER, SpringContainerClusteredEnvHazelcastObjectProvider.class.getName());
   }
   
   /**
    * <p>
    * <b>Transient Process Support is disabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of non-forked processes.
    * </p>
    */
   @Test
   public void testNonTransientProcessWithoutForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();
      
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasPiEntryInDb(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is disabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of forked processes.
    * </p>
    */
   @Test
   public void testNonTransientProcessWithForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasPiEntryInDb(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is disabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of non-forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testNonTransientProcessFailsWithoutForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();
      
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED_FAIL, null, true);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasPiEntryInDb(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is disabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether non-transient processes are written to the database
    * in case of forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testNonTransientProcessFailsWithForkOnTraversal() throws Exception
   {
      disableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED_FAIL, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasPiEntryInDb(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is disabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether non-transient processes can be executed successfully and
    * that they're written to the database.
    * </p>
    */
   @Test
   public void testNonTransientProcessSplitScenario() throws Exception
   {
      disableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      assertThat(hasPiEntryInDb(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether transient processes are <b>not</b> written to the database
    * in case of non-forked processes.
    * </p>
    */
   @Test
   public void testTransientProcessWithoutForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();
      
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED, null, true);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasPiEntryInDb(), is(false));
   }

   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether transient processes are <b>not</b> written to the database
    * in case of forked processes.
    * </p>
    */
   @Test
   public void testTransientProcessWithForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.COMPLETED));
      assertThat(hasPiEntryInDb(), is(false));
   }

   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether transient processes are written to the database
    * in case of non-forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testTransientProcessFailsWithoutForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();
      
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_NON_FORKED_FAIL, null, true);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasPiEntryInDb(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether transient processes are written to the database
    * in case of forked processes when an error occures during execution.
    * </p>
    */
   @Test
   public void testTransientProcessFailsWithForkOnTraversal() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED_FAIL, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Interrupted);
      
      final Parameters params = Parameters.instance();
      assertThat((ProcessExecutionState) params.get(PROCESS_EXECUTION_STATE), is(ProcessExecutionState.INTERRUPTED));
      assertThat(hasPiEntryInDb(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether transient processes can be executed successfully and
    * that they're <b>not</b> written to the database.
    * </p>
    */
   @Test
   public void testTransientProcessSplitScenario() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_SPLIT, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      assertThat(hasPiEntryInDb(), is(false));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests that the transient processes are <b>not</b> written to the
    * transient process in-memory storage in case of transaction rollback.
    * </p>
    */
   @Test
   public void testRollbackScenario() throws Exception
   {
      enableTransientProcessesSupport();
      
      try
      {
         sf.getWorkflowService().startProcess(PROCESS_DEF_ID_ROLLBACK, null, true);
         fail("Tx is marked for rollback and therefore cannot be committed.");
      }
      catch (final UnexpectedRollbackException ignored)
      {
         /* expected */
      }

      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests that the process instance data is removed from the in-memory storage
    * for transient process instances as soon as the process instance is completed.
    * </p>
    */
   @Test
   public void testCleanup() throws Exception
   {
      enableTransientProcessesSupport();
      
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_FORKED, null, true);
      
      ProcessInstanceStateBarrier.instance().await(pi.getOID(), ProcessInstanceState.Completed);
      
      assertThat(isTransientProcessInstanceStorageEmpty(), is(true));
   }

   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether a process instance started transiently is able to complete transiently,
    * if it only hits activities permitting transient execution, even though there's a route
    * in the model containing an interactive activity.
    * </p>
    */
   @Test
   public void testStartTransientlyCompleteTransiently() throws Exception
   {
      enableTransientProcessesSupport();
      
      sf.getWorkflowService().startProcess(PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE, null, true);
      
      assertThat(hasPiEntryInDb(), is(false));
   }
   
   /**
    * <p>
    * <b>Transient Process Support is enabled.</b>
    * </p>
    * 
    * <p>
    * Tests whether a process instance started transiently is able to complete non-transiently,
    * if it hits an activity that does not permit transient execution.
    * </p>
    */
   @Test
   public void testStartTransientlyCompleteNonTransiently() throws Exception
   {
      enableTransientProcessesSupport();
      
      final Map<String, ?> data = Collections.singletonMap(DATA_ID_TRANSIENT_ROUTE, Boolean.FALSE);
      final ProcessInstance pi = sf.getWorkflowService().startProcess(PROCESS_DEF_ID_TRANSIENT_NON_TRANSIENT_ROUTE, data, true);
      
      final ActivityInstance ai = sf.getQueryService().findFirstActivityInstance(ActivityInstanceQuery.findAlive(pi.getProcessID()));
      sf.getWorkflowService().activateAndComplete(ai.getOID(), null, null);
      
      assertThat(hasPiEntryInDb(), is(true));
   }
   
   private boolean hasPiEntryInDb() throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;
      
      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT * FROM PUBLIC.PROCESS_INSTANCE");
         result = rs.first();
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
      
      return result;
   }
   
   private void enableTransientProcessesSupport()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, true);
      
      dropTransientProcessInstanceStorage();
   }
   
   private void disableTransientProcessesSupport()
   {
      final Parameters params = Parameters.instance();
      params.set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, false);
   }
   
   private void dropTransientProcessInstanceStorage()
   {
      final Map<?, ?> piBlobs = getPiBlobsMap();
      piBlobs.clear();
   }
   
   private boolean isTransientProcessInstanceStorageEmpty()
   {
      final Map<?, ?> piBlobs = getPiBlobsMap();
      return piBlobs.isEmpty();
   }
   
   private Map<?, ?> getPiBlobsMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PI_BLOBS_FIELD_NAME);
   }
   
   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * in order to investigate the behavior in case of failures. In addition,
    * it sets a property so that one can determine that the process instance
    * is interrupted.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class FailingApp
   {
      public void fail()
      {
         final Parameters params = Parameters.instance();
         params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.INTERRUPTED);
         
         /* always throws an exception to test behavior in case of failures */
         throw new RuntimeException("expected");
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that simply succeeds. In addition, 
    * it sets a property so that one can determine that the process instance is completed.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SucceedingApp
   {
      public void success()
      {
         final Parameters params = Parameters.instance();
         params.set(PROCESS_EXECUTION_STATE, ProcessExecutionState.COMPLETED);
      }
   }
   
   /**
    * <p>
    * This is the application used in the test model that prevents the current transaction
    * from being committed.
    * </p>
    * 
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SettingRollbackOnlyApp
   {
      private static final String JTA_TX_MANAGER_SPRING_BEAN_ID = "jtaTxManager";
      
      public void setRollbackOnly() throws SystemException
      {
         final JtaTransactionManager txManager = SpringUtils.getApplicationContext().getBean(JTA_TX_MANAGER_SPRING_BEAN_ID, JtaTransactionManager.class);
         txManager.getUserTransaction().setRollbackOnly();
      }
   }
   
   private static enum ProcessExecutionState { NOT_STARTED, COMPLETED, INTERRUPTED }
}
