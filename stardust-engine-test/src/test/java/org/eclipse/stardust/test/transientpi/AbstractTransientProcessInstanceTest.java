/**********************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.transientpi;

import static org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants.PROCESS_DEF_ID_SPLIT_SPLIT;
import static org.eclipse.stardust.test.util.TestConstants.NL;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.sql.DataSource;
import javax.transaction.SystemException;

import org.eclipse.stardust.common.Action;
import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.dto.AuditTrailPersistence;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.api.spring.SpringUtils;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.ClusterSafeObjectProviderHolder;
import org.eclipse.stardust.engine.core.persistence.jdbc.transientpi.TransientProcessInstanceStorage;
import org.eclipse.stardust.engine.core.runtime.beans.AdministrationServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingService;
import org.eclipse.stardust.engine.core.runtime.beans.ForkingServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.LargeStringHolder;
import org.eclipse.stardust.engine.core.runtime.beans.ModelManagerFactory;
import org.eclipse.stardust.engine.core.runtime.beans.ProcessInstanceBean;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadData;
import org.eclipse.stardust.engine.core.runtime.beans.SerialActivityThreadWorkerCarrier;
import org.eclipse.stardust.engine.core.runtime.beans.WorkflowServiceImpl;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.JmsProperties;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.engine.core.runtime.removethis.EngineProperties;
import org.eclipse.stardust.engine.extensions.jms.app.DefaultMessageHelper;
import org.eclipse.stardust.test.api.monitoring.DatabaseOperationMonitoring;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.util.JmsConstants;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * <p>
 * Base class for transient process instance tests holding some
 * common methods and classes.
 * </p>
 *
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class AbstractTransientProcessInstanceTest
{
   protected static final String PI_BLOBS_HOLDER_FIELD_NAME = "piBlobsHolder";

   protected static final String PERSISTENT_TO_ROOT_PI_FIELD_NAME = "persistentToRootPi";

   protected static final String ROOT_PI_TO_PI_BLOB_FIELD_NAME = "rootPiToPiBlob";

   protected static final String CHUNK_SIZE_FIELD_NAME = "ATOM_SIZE";

   protected static final String HAZELCAST_LOGGING_TYPE_KEY = "hazelcast.logging.type";
   protected static final String HAZELCAST_LOGGING_TYPE_VALUE = "log4j";


   protected static volatile boolean appMayComplete;

   private final LocalJcrH2TestSetup testClassSetup;

   protected AbstractTransientProcessInstanceTest(final LocalJcrH2TestSetup testClassSetup)
   {
      this.testClassSetup = testClassSetup;
   }

   protected boolean hasEntryInDbForPi(final long oid) throws SQLException
   {
      final DataSource ds = testClassSetup.dataSource();
      final boolean result;

      Connection connection = null;
      Statement stmt = null;
      try
      {
         connection = ds.getConnection();
         stmt = connection.createStatement();
         final ResultSet rs = stmt.executeQuery("SELECT * FROM PUBLIC.PROCESS_INSTANCE WHERE OID = " + oid);
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

   protected boolean noSerialActivityThreadQueues()
   {
      final Map<Long, SerialActivityThreadData> map = ClusterSafeObjectProviderHolder.OBJ_PROVIDER.clusterSafeMap(SerialActivityThreadWorkerCarrier.SERIAL_ACTIVITY_THREAD_MAP_ID);
      return map.isEmpty();
   }

   protected void assertNoSerialActivityThreadQueuesBeforeTestStart(final String testMethodName)
   {
      final String errorMsg = NL + "Unable to start '" + testMethodName + "' due to existing serial activity thread queues. This is most likely caused by a failing test which ran prior to this one.";
      assertThat(errorMsg, noSerialActivityThreadQueues(), is(true));
   }

   protected void startProcessViaJms(final String processId)
   {
      final Queue queue = testClassSetup.queue(JmsProperties.APPLICATION_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setSessionTransacted(true);
      jmsTemplate.send(queue, new MessageCreator()
      {
         @Override
         public Message createMessage(final Session session) throws JMSException
         {
            final MapMessage msg = session.createMapMessage();
            msg.setStringProperty(DefaultMessageHelper.PROCESS_ID_HEADER, processId);

            return msg;
         }
      });
   }

   protected long receiveProcessInstanceCompletedMessage() throws JMSException
   {
      final Queue queue = testClassSetup.queue(JmsConstants.TEST_QUEUE_NAME_PROPERTY);
      final JmsTemplate jmsTemplate = new JmsTemplate();
      jmsTemplate.setConnectionFactory(testClassSetup.queueConnectionFactory());
      jmsTemplate.setReceiveTimeout(5000L);

      final Message message = jmsTemplate.receive(queue);
      if (message == null)
      {
         throw new JMSException("Timeout while receiving.");
      }
      return message.getLongProperty(DefaultMessageHelper.PROCESS_INSTANCE_OID_HEADER);
   }

   protected Set<ProcessExecutor> initProcessExecutors(final int nThreads, final WorkflowService wfService)
   {
      final String processId = PROCESS_DEF_ID_SPLIT_SPLIT;

      final Set<ProcessExecutor> processExecutors = new HashSet<ProcessExecutor>();
      for (int i=0; i<nThreads; i++)
      {
         final ProcessExecutor pe = new ProcessExecutor(wfService, processId);
         processExecutors.add(pe);
      }

      return processExecutors;
   }

   protected List<Future<Long>> executeProcesses(final int nThreads, final Set<ProcessExecutor> processExecutors) throws InterruptedException
   {
      final ExecutorService executor = Executors.newFixedThreadPool(nThreads);

      final List<Future<Long>> piOids = executor.invokeAll(processExecutors);
      executor.shutdown();
      boolean terminatedGracefully = executor.awaitTermination(10, TimeUnit.SECONDS);
      if ( !terminatedGracefully)
      {
         throw new IllegalStateException("Executor hasn't been terminated gracefully.");
      }

      return piOids;
   }

   protected void enableTransientProcessesSupport(final String testMethodName)
   {
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_ON);

      dropTransientProcessInstanceStorage();
      assertNoSerialActivityThreadQueuesBeforeTestStart(testMethodName);
   }

   protected void overrideTransientProcessesSupport(final String override, final String testMethodName)
   {
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, override);

      dropTransientProcessInstanceStorage();
      assertNoSerialActivityThreadQueuesBeforeTestStart(testMethodName);
   }

   protected void disableTransientProcessesSupport()
   {
      GlobalParameters.globals().set(KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES, KernelTweakingProperties.SUPPORT_TRANSIENT_PROCESSES_OFF);
   }

   protected void disableInMemStorageExposal()
   {
      GlobalParameters.globals().set(KernelTweakingProperties.TRANSIENT_PROCESSES_EXPOSE_IN_MEM_STORAGE, false);
   }

   protected void enableTxPropagation()
   {
      GlobalParameters.globals().set(KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION, KernelTweakingProperties.APPLICATION_EXCEPTION_PROPAGATION_ALWAYS);
   }

   protected void enableOneSystemQueueConsumerRetry()
   {
      GlobalParameters.globals().set(JmsProperties.MESSAGE_LISTENER_RETRY_COUNT_PROPERTY, 2);
   }

   protected void dropTransientProcessInstanceStorage()
   {
      getPersistentToRootPiMap().clear();
      getRootPiToBlobMap().clear();
   }

   protected boolean isTransientProcessInstanceStorageEmpty()
   {
      final Map<?, ?> persistentToRootPiMap = getPersistentToRootPiMap();
      final Map<?, ?> rootPiToBlobMap = getRootPiToBlobMap();

      return persistentToRootPiMap.isEmpty() && rootPiToBlobMap.isEmpty();
   }

   protected Map<?, ?> getPersistentToRootPiMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, PERSISTENT_TO_ROOT_PI_FIELD_NAME);
   }

   protected Map<?, ?> getRootPiToBlobMap()
   {
      final Object piBlobsHolder = Reflect.getFieldValue(TransientProcessInstanceStorage.instance(), PI_BLOBS_HOLDER_FIELD_NAME);
      return (Map<?, ?>) Reflect.getFieldValue(piBlobsHolder, ROOT_PI_TO_PI_BLOB_FIELD_NAME);
   }

   protected void assertPiInfoIsComplete(final ProcessInstance pi, final boolean considerTerminationTime)
   {
      assertThat(pi.getDetailsLevel(), notNullValue());
      assertThat(pi.getDetailsOptions(), notNullValue());
      assertThat(pi.getModelElementID(), notNullValue());
      assertThat(pi.getModelElementOID(), not(0));
      assertThat(pi.getModelOID(), not(0));
      assertThat(pi.getOID(), not(0L));
      assertThat(pi.getParentProcessInstanceOid(), not(0L));
      assertThat(pi.getPriority(), not(-1));
      assertThat(pi.getProcessID(), notNullValue());
      assertThat(pi.getProcessName(), notNullValue());
      assertThat(pi.getRootProcessInstanceOID(), not(0L));
      assertThat(pi.getScopeProcessInstance(), notNullValue());
      assertThat(pi.getScopeProcessInstanceOID(), not(0L));
      assertThat(pi.getStartingUser(), notNullValue());
      assertThat(pi.getStartTime(), notNullValue());
      assertThat(pi.getState(), notNullValue());
      if (considerTerminationTime)
      {
         assertThat(pi.getTerminationTime(), notNullValue());
      }
   }

   protected void assertAiInfoIsComplete(final ActivityInstance ai)
   {
      assertThat(ai.getActivity(), notNullValue());
      assertThat(ai.getLastModificationTime(), notNullValue());
      assertThat(ai.getModelElementID(), notNullValue());
      assertThat(ai.getModelElementOID(), not(0));
      assertThat(ai.getModelOID(), not(0));
      assertThat(ai.getOID(), not(0L));
      assertThat(ai.getProcessDefinitionId(), notNullValue());
      assertThat(ai.getProcessInstance(), notNullValue());
      assertThat(ai.getProcessInstanceOID(), not(0L));
      assertThat(ai.getStartTime(), notNullValue());
      assertThat(ai.getState(), notNullValue());
   }

   protected void initMonitoring(final ServiceFactory sf) throws SQLException
   {
      /* eager log-in: do login-in related DB operations before starting to monitor */
      /* since in a real-world scenario one can assume that he's already logged-in  */
      sf.getWorkflowService();

      /* eagerly initialize model manager: initialize model manager before starting to monitor */
      /* since in a real-world scenario one can assume that it's already initialized           */
      sf.getWorkflowService().execute(new InitializeModelManager());

      DatabaseOperationMonitoring.instance().createMonitoringTriggers(testClassSetup.dataSource());
   }

   protected void endMonitoring() throws SQLException
   {
      DatabaseOperationMonitoring.instance().dropMonitoringTriggers(testClassSetup.dataSource());
   }

   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * in order to investigate the behavior in case of failures.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class FailingApp
   {
      public void fail()
      {
         /* always throws an exception to test behavior in case of failures */
         throw new RuntimeException("expected");
      }
   }

   /**
    * <p>
    * This is the application used in the model that causes the process instance to fail
    * during the first attempt in order to investigate the behavior in these cases.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class FirstTryFailsApp
   {
      protected static volatile boolean firstTime = true;

      public void failTheFirstTime()
      {
         if (firstTime)
         {
            firstTime = false;
            throw new RuntimeException("expected");
         }

         /* succeed */
      }
   }

   /**
    * <p>
    * This is the application used in the test model that simply succeeds.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class SucceedingApp
   {
      public void success()
      {
         /* nothing to do */
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

   /**
    * <p>
    * This is the application used in the test model that aborts the process instance.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class AbortingApp
   {
      public void abort(final long piOid)
      {
         new AdministrationServiceImpl().abortProcessInstance(piOid);
         throw new RuntimeException("Aborting process instance ... (expected exception)");
      }
   }

   /**
    * <p>
    * This is the application used in the test model that queries for the current process
    * instance in a new transaction.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class IsolatedQueryApp
   {
      public void queryIsolated(final long piOid)
      {
         final ForkingServiceFactory factory = (ForkingServiceFactory) Parameters.instance().get(EngineProperties.FORKING_SERVICE_HOME);
         final ForkingService forkingService = factory.get();
         forkingService.isolate(new Action<Void>()
         {
            @Override
            public Void execute()
            {
               new WorkflowServiceImpl().getProcessInstance(piOid);
               return null;
            }
         });
      }
   }

   /**
    * <p>
    * This is the application used in the test model that waits for some time
    * in case it's not allowed to proceed.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class WaitingApp
   {
      public void doWait() throws InterruptedException, TimeoutException
      {
         int nRuns = 0;

         while ( !appMayComplete)
         {
            nRuns++;
            if (nRuns > 10)
            {
               /* something went terribly wrong: we need to cancel */
               throw new TimeoutException("We still may not complete: something went terribly wrong ...");
            }

            Thread.sleep(1000L);
         }
      }
   }

   /**
    * <p>
    * This is the application used in the test model that writes a large portion of
    * a string such that it needs to be split up in order to be stored in the db.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class BigDataWriter
   {
      private static final int CHUNK_SIZE = ((Integer) Reflect.getStaticFieldValue(LargeStringHolder.class, CHUNK_SIZE_FIELD_NAME)).intValue();

      public String writeData()
      {
         final StringBuilder sb = new StringBuilder();
         for (int i = 0; i < CHUNK_SIZE * 2; i++)
         {
            sb.append("x");
         }
         return sb.toString();
      }
   }

   /**
    * <p>
    * This is the application used in the test model that reades a large portion of
    * a string.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class BigDataReader
   {
      public void readData(final String string)
      {
         /* nothing to do */
      }
   }

   /**
    * <p>
    * This is the application used in the test model to change the <i>Audit Trail Persistence</i> mode.
    * </p>
    *
    * @author Nicolas.Werlein
    * @version $Revision$
    */
   public static final class ChangeAuditTrailPersistence
   {
      public void changeIt(final long rootPiOid, final String auditTrailPersistence)
      {
         final AuditTrailPersistence newValue = AuditTrailPersistence.valueOf(auditTrailPersistence);

         final ProcessInstanceBean rootPi = ProcessInstanceBean.findByOID(rootPiOid);
         rootPi.setAuditTrailPersistence(newValue);
      }
   }

   protected static final class ProcessExecutor implements Callable<Long>
   {
      private final WorkflowService wfService;
      private final String processId;

      public ProcessExecutor(final WorkflowService wfService, final String processId)
      {
         this.wfService = wfService;
         this.processId = processId;
      }

      @Override
      public Long call() throws Exception
      {
         final ProcessInstance pi = wfService.startProcess(processId, null, true);
         return pi.getOID();
      }
   }

   /**
    * <p>
    * This class is used to eagerly initialize the model manager.
    * </p>
    */
   private static final class InitializeModelManager implements ServiceCommand
   {
      private static final long serialVersionUID = -156306005992199032L;

      @Override
      public Serializable execute(final ServiceFactory sf)
      {
         /* initializes the model manager */
         ModelManagerFactory.getCurrent().getLastDeployment();

         return null;
      }
   }
}
