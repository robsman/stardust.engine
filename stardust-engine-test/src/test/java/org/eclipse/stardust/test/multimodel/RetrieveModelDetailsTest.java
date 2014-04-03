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
package org.eclipse.stardust.test.multimodel;

import static org.eclipse.stardust.test.multimodel.MultiModelConstants.CONSUMER_MODEL_NAME;
import static org.eclipse.stardust.test.multimodel.MultiModelConstants.PROVIDER_MODEL_NAME;
import static org.eclipse.stardust.test.util.TestConstants.MOTU;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.core.runtime.command.impl.RetrieveModelDetailsCommand;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup;
import org.eclipse.stardust.test.api.setup.LocalJcrH2TestSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * <p>
 * Verify ModelDetails can be retrieved with and without provider/consumer information.
 * </p>
 *
 * @author Robert Sauer
 * @version $Revision: 66675 $
 */
public class RetrieveModelDetailsTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory adminSf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final LocalJcrH2TestSetup testClassSetup = new LocalJcrH2TestSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING, PROVIDER_MODEL_NAME, CONSUMER_MODEL_NAME);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(adminSf);

   @Before
   public void setUp()
   {
   }

   @Test
   public void testProviderModelDetailsCanBeRetrievedWithFullDetails()
   {
      Models models = adminSf.getQueryService().getModels(DeployedModelQuery.findActiveForId(PROVIDER_MODEL_NAME));

      assertThat(models.size(), is(1));

      DeployedModelDescription provider = models.get(0);

      assertThat(provider.getConsumerModels().size(), is(1));
   }

   @Test
   public void testConsumerModelDetailsCanBeRetrievedWithFullDetails()
   {
      Models models = adminSf.getQueryService().getModels(DeployedModelQuery.findActiveForId(CONSUMER_MODEL_NAME));

      assertThat(models.size(), is(1));

      DeployedModelDescription consumer = models.get(0);

      assertThat(consumer.getProviderModels().size(), is(1));

      assertThat(consumer.getImplementationProcesses().size(), is(2));
   }

   @Test
   public void testProviderModelDetailsCanBeRetrievedWithReducedDetails()
   {
      DeployedModelDescription provider = (DeployedModelDescription) adminSf
            .getWorkflowService().execute(
                  RetrieveModelDetailsCommand
                        .retrieveActiveModelById(PROVIDER_MODEL_NAME));

      assertThat(provider.getConsumerModels().size(), is(0));
   }

   @Test
   public void testConsumerModelDetailsCanBeRetrievedWithReducedDetails()
   {
      DeployedModelDescription consumer = (DeployedModelDescription) adminSf
            .getWorkflowService().execute(
                  RetrieveModelDetailsCommand
                        .retrieveActiveModelById(CONSUMER_MODEL_NAME));

      assertThat(consumer, is(notNullValue()));

      assertThat(consumer.getProviderModels().size(), is(0));

      assertThat(consumer.getImplementationProcesses().size(), is(0));
   }
}
