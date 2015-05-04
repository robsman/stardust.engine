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
package org.eclipse.stardust.test.admin;

import static org.eclipse.stardust.engine.api.model.PredefinedConstants.PREDEFINED_MODEL_ID;
import static org.eclipse.stardust.engine.api.query.DeployedModelQuery.findForId;
import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeploymentElement;
import org.eclipse.stardust.engine.api.runtime.DeploymentException;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.DeploymentOptions;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.test.api.setup.RtEnvHome;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.examples.MyConstants;
import org.eclipse.stardust.test.transientpi.TransientProcessInstanceModelConstants;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

/**
 * <p>
 * This class focuses on testing administration operations
 * exposed by {@link AdministrationService}.
 * </p>
 *
 * @author Nicolas.Werlein
 */
public class AdministrationOperationTest
{
   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(MOTU, MOTU);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(ADMIN_USER_PWD_PAIR, testClassSetup);
   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING);

   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup)
                                          .around(sf);

   @Test
   public void testExplicitDeploymentOfPredefinedModel()
   {
      final List<DeploymentInfo> deploymentInfos = sf.getAdministrationService().deployModel(Collections.<DeploymentElement>emptyList(), DeploymentOptions.DEFAULT);

      assertTrue(deploymentInfos.size() == 0);
      final Models models = sf.getQueryService().getModels(findForId(PREDEFINED_MODEL_ID));
      assertThat(models.size(), is(1));
   }

   @Test
   public void testDeployModel()
   {
      final List<DeploymentInfo> deploymentInfos = RtEnvHome.deployModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfos.size() == 1);
      assertTrue(deploymentInfos.get(0).getRevision() == 0);
      final Models models = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models.size(), is(1));
   }

   @Test
   public void testVersionModel()
   {
      final List<DeploymentInfo> deploymentInfos1 = RtEnvHome.deployModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfos1.size() == 1);
      assertTrue(deploymentInfos1.get(0).getRevision() == Integer.valueOf(0));
      final Models models1 = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models1.size(), is(1));

      final List<DeploymentInfo> deploymentInfos2 = RtEnvHome.deployModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfos2.size() == 1);
      assertTrue(deploymentInfos2.get(0).getRevision() == 0);
      final Models models2 = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models2.size(), is(2));
   }

   @Test
   public void overwriteModel()
   {
      RtEnvHome.deployModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);

      final DeploymentInfo deploymentInfo = RtEnvHome.overwriteModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfo.getRevision() == 1);
      final Models models = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models.size(), is(1));
   }

   @Test
   public void testOverwriteModelWithAllowOverwriteWithoutInitialModelHavingNoModelDeployed()
   {
      final DeploymentOptions deploymentOptions = DeploymentOptions.DEFAULT;
      deploymentOptions.setAllowOverwriteWithoutInitialModel(true);
      final DeploymentInfo deploymentInfo = RtEnvHome.overwriteModel(sf.getAdministrationService(), deploymentOptions, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfo.getRevision() == 0);
      final Models models = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models.size(), is(1));
   }

   @Test(expected = DeploymentException.class)
   public void testOverwriteModelHavingNoModelDeployed()
   {
      RtEnvHome.overwriteModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);
      fail();
   }

   @Test
   public void testOverwriteModelWithAllowOverwriteWithoutInitialModelHavingModelWithDifferentIdDeployed()
   {
      RtEnvHome.deployModel(sf.getAdministrationService(), null, TransientProcessInstanceModelConstants.MODEL_ID);

      final DeploymentOptions deploymentOptions = DeploymentOptions.DEFAULT;
      deploymentOptions.setAllowOverwriteWithoutInitialModel(true);
      final DeploymentInfo deploymentInfo = RtEnvHome.overwriteModel(sf.getAdministrationService(), deploymentOptions, MyConstants.MODEL_NAME);

      assertTrue(deploymentInfo.getRevision() == 0);
      final Models models = sf.getQueryService().getModels(findForId(MyConstants.MODEL_NAME));
      assertThat(models.size(), is(1));
   }

   @Test(expected = DeploymentException.class)
   public void testOverwriteModelHavingModelWithDifferentIdDeployed()
   {
      RtEnvHome.deployModel(sf.getAdministrationService(), null, TransientProcessInstanceModelConstants.MODEL_ID);

      RtEnvHome.overwriteModel(sf.getAdministrationService(), null, MyConstants.MODEL_NAME);
      fail();
   }

   @After
   public void tearDown()
   {
      RtEnvHome.cleanUpRuntimeAndModels(sf.getAdministrationService());
   }
}
