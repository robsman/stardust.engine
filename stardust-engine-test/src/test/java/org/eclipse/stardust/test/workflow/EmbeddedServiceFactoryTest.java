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
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.ServiceCommandException;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.runtime.command.ServiceCommand;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UserHome;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

public class EmbeddedServiceFactoryTest
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
   public void testEmbeddedServiceFactoryUnwrapsExceptions()
   {
      final AtomicReference<Throwable> innerException = new AtomicReference<Throwable>();

      ServiceCommand cmdTriggerObjectNotFoundException = new ServiceCommand()
      {
         private static final long serialVersionUID = 1L;

         @Override
         public Serializable execute(ServiceFactory sf)
         {
            try
            {
               sf.getQueryService().findFirstActivityInstance(
                     ActivityInstanceQuery.findForProcessInstance(-42));
            }
            catch (RuntimeException e)
            {
               innerException.set(e);
               throw e;
            }
            return null;
         }
      };

      ServiceCommandException outerException = null;
      try
      {
         userSf.getWorkflowService().execute(cmdTriggerObjectNotFoundException);
      }
      catch (ServiceCommandException sce)
      {
         outerException = sce;
      }

      assertThat(innerException.get(), is(instanceOf(ObjectNotFoundException.class)));

      assertThat(outerException, is(instanceOf(ServiceCommandException.class)));
      assertThat(outerException.getCause(), is(sameInstance(innerException.get())));
   }
}
