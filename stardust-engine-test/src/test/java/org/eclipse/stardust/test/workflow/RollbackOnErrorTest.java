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
package org.eclipse.stardust.test.workflow;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.DEFAULT_ROLE_ID;
import static org.eclipse.stardust.test.workflow.BasicWorkflowModelConstants.MODEL_NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.config.GlobalParameters;
import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.common.rt.TransactionUtils;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.KernelTweakingProperties;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class RollbackOnErrorTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private static final String DEFAULT_ROLE_USER_ID = "u1";

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final TestServiceFactory userSf = new TestServiceFactory(
         new UsernamePasswordPair(DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_USER_ID));

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(adminSf)
         .around(userSf);

   @Before
   public void setUp()
   {
      UserHome.create(adminSf, DEFAULT_ROLE_USER_ID, DEFAULT_ROLE_ID);
   }

   @Test
   public void testEmbeddedServiceFactoryAdheresToRollbackModeAlways()
   {
      verifyQueryServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_ALWAYS, true);
      verifyWorkflowServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_ALWAYS, true);
   }

   @Test
   public void testEmbeddedServiceFactoryAdheresToRollbackModeLenient()
   {
      verifyQueryServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_LENIENT, false);
      verifyWorkflowServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_LENIENT, true);
   }

   @Test
   public void testEmbeddedServiceFactoryAdheresToRollbackModeNever()
   {
      verifyQueryServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_NEVER, false);
      verifyWorkflowServiceAdheresToRollbackMode(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR_NEVER, false);
   }

   void verifyQueryServiceAdheresToRollbackMode(String mode, boolean mustRollbackQuery)
   {
      final AtomicBoolean wasRollbackOnlyAfterQuery = new AtomicBoolean();

      ParametersFacade.instance().flush();
      GlobalParameters.globals().set(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR, mode);

      ServiceCommand cmdFailingQuery = new ServiceCommand()
      {
         private static final long serialVersionUID = 1L;

         @Override
         public Serializable execute(ServiceFactory sf)
         {
            assertThat(TransactionUtils.getCurrentTxStatus().isRollbackOnly(), is(false));

            try
            {
               sf.getQueryService().findFirstActivityInstance(
                     ActivityInstanceQuery.findForProcessInstance(-42));
            }
            finally
            {
               wasRollbackOnlyAfterQuery.set(TransactionUtils.getCurrentTxStatus().isRollbackOnly());
            }
            return null;
         }
      };

      try
      {
         userSf.getWorkflowService().execute(cmdFailingQuery);
      }
      catch (ServiceCommandException sce)
      {
         // this is expected, just ignore
      }
      finally
      {
         assertThat(wasRollbackOnlyAfterQuery.get(), is(mustRollbackQuery));
      }
   }

   void verifyWorkflowServiceAdheresToRollbackMode(String mode, boolean mustRollbackStartProcess)
   {
      final AtomicBoolean wasRollbackOnlyAfterStartProcess = new AtomicBoolean();

      ParametersFacade.instance().flush();
      GlobalParameters.globals().set(KernelTweakingProperties.TX_ROLLBACK_ON_ERROR, mode);

      ServiceCommand cmdFailingProcessStart = new ServiceCommand()
      {
         private static final long serialVersionUID = 1L;

         @Override
         public Serializable execute(ServiceFactory sf)
         {
            assertThat(TransactionUtils.getCurrentTxStatus().isRollbackOnly(), is(false));

            try
            {
               sf.getWorkflowService().startProcess("There must be no process with this ID.", null, true);
            }
            finally
            {
               wasRollbackOnlyAfterStartProcess.set(TransactionUtils.getCurrentTxStatus().isRollbackOnly());
            }
            return null;
         }
      };

      try
      {
         userSf.getWorkflowService().execute(cmdFailingProcessStart);
      }
      catch (ServiceCommandException sce)
      {
         // this is expected, just ignore
      }
      finally
      {
         assertThat(wasRollbackOnlyAfterStartProcess.get(), is(mustRollbackStartProcess));
      }
   }
}
