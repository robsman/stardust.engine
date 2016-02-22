/**********************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 **********************************************************************************/
package org.eclipse.stardust.test.artifact;

import static org.eclipse.stardust.test.api.util.TestConstants.MOTU;

import java.util.Date;

import org.junit.*;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runners.MethodSorters;

import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifactQuery;
import org.eclipse.stardust.engine.api.query.DeployedRuntimeArtifacts;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.spi.artifact.impl.BenchmarkDefinitionArtifactType;
import org.eclipse.stardust.test.api.setup.TestClassSetup;
import org.eclipse.stardust.test.api.setup.TestClassSetup.ForkingServiceMode;
import org.eclipse.stardust.test.api.setup.TestMethodSetup;
import org.eclipse.stardust.test.api.setup.TestServiceFactory;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;
import org.eclipse.stardust.test.dms.DmsModelConstants;

/**
 * Tests query operations of the {@link RuntimeArtifact}.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class ArtifactQueryTest
{

   private static final String BENCHMARK_ARTIFACT_TYPE_ID = BenchmarkDefinitionArtifactType.TYPE_ID;

   private static final String ARTIFACT_ID_1 = "bench1.benchmark";
   private static final String ARTIFACT_ID_2 = "bench2.benchmark";

   private static final UsernamePasswordPair ADMIN_USER_PWD_PAIR = new UsernamePasswordPair(
         MOTU, MOTU);

   private final TestServiceFactory sf = new TestServiceFactory(ADMIN_USER_PWD_PAIR);

   private final TestMethodSetup testMethodSetup = new TestMethodSetup(
         ADMIN_USER_PWD_PAIR, testClassSetup);

   @ClassRule
   public static final TestClassSetup testClassSetup = new TestClassSetup(
         ADMIN_USER_PWD_PAIR, ForkingServiceMode.NATIVE_THREADING,
         DmsModelConstants.DMS_MODEL_NAME);


   @Rule
   public final TestRule chain = RuleChain.outerRule(testMethodSetup).around(sf);


   @Before
   public void setup() throws InterruptedException
   {
      AdministrationService as = sf.getAdministrationService();

      DeployedRuntimeArtifacts runtimeArtifacts = sf.getQueryService()
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findAll());
      for (DeployedRuntimeArtifact deployedRuntimeArtifact : runtimeArtifacts)
      {
         as.deleteRuntimeArtifact(deployedRuntimeArtifact.getOid());
      }

      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_1, 0, "1"));
      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_1, 10, "2"));

      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_2, 10, "3"));
      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_2, 12, "4"));

      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_1, 0, "5"));
      as.deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID_2, 5, "6"));

   }


   @After
   public void cleanup() throws InterruptedException
   {}


   private RuntimeArtifact getRuntimeArtifact1(String artifactId, long time, String name)
   {
      return new RuntimeArtifact(BENCHMARK_ARTIFACT_TYPE_ID,
            artifactId, name, "".getBytes(), new Date(time));
   }


   @Test
   public void testQueryAll()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findAll());
      Assert.assertEquals(6, runtimeArtifacts.getSize());

      DeployedRuntimeArtifact deployedRuntimeArtifact = runtimeArtifacts.get(0);

      Assert.assertTrue(0 < deployedRuntimeArtifact.getOid());
      Assert.assertEquals(ARTIFACT_ID_1, deployedRuntimeArtifact.getArtifactId());
      Assert.assertEquals("1", deployedRuntimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact.getArtifactTypeId());
   }


   @Test
   public void testQueryAllActive()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findAllActive(new Date()));

      Assert.assertEquals(2, runtimeArtifacts.getSize());

      DeployedRuntimeArtifact deployedRuntimeArtifact1 = runtimeArtifacts.get(0);
      Assert.assertTrue(0 < deployedRuntimeArtifact1.getOid());
      Assert.assertEquals("2", deployedRuntimeArtifact1.getArtifactName());
      Assert.assertEquals(ARTIFACT_ID_1, deployedRuntimeArtifact1.getArtifactId());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact1.getArtifactTypeId());

      DeployedRuntimeArtifact deployedRuntimeArtifact2 = runtimeArtifacts.get(1);
      Assert.assertTrue(0 < deployedRuntimeArtifact2.getOid());
      Assert.assertEquals("4", deployedRuntimeArtifact2.getArtifactName());
      Assert.assertEquals(ARTIFACT_ID_2, deployedRuntimeArtifact2.getArtifactId());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact2.getArtifactTypeId());
   }


   @Test
   public void testQueryActiveNow()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findActive(BENCHMARK_ARTIFACT_TYPE_ID, new Date()));
      Assert.assertEquals(2, runtimeArtifacts.getSize());

      DeployedRuntimeArtifact deployedRuntimeArtifact1 = runtimeArtifacts.get(0);
      Assert.assertTrue(0 < deployedRuntimeArtifact1.getOid());
      Assert.assertEquals("2", deployedRuntimeArtifact1.getArtifactName());
      Assert.assertEquals(ARTIFACT_ID_1, deployedRuntimeArtifact1.getArtifactId());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact1.getArtifactTypeId());

      DeployedRuntimeArtifact deployedRuntimeArtifact2 = runtimeArtifacts.get(1);
      Assert.assertTrue(0 < deployedRuntimeArtifact2.getOid());
      Assert.assertEquals("4", deployedRuntimeArtifact2.getArtifactName());
      Assert.assertEquals(ARTIFACT_ID_2, deployedRuntimeArtifact2.getArtifactId());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact2.getArtifactTypeId());
   }

   @Test
   public void testQueryActiveDayZero()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findActive(BENCHMARK_ARTIFACT_TYPE_ID, new Date(0)));
      Assert.assertEquals(1, runtimeArtifacts.getSize());

      DeployedRuntimeArtifact deployedRuntimeArtifact1 = runtimeArtifacts.get(0);
      Assert.assertTrue(0 < deployedRuntimeArtifact1.getOid());
      Assert.assertEquals("5", deployedRuntimeArtifact1.getArtifactName());
      Assert.assertEquals(ARTIFACT_ID_1, deployedRuntimeArtifact1.getArtifactId());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact1.getArtifactTypeId());
   }

   @Test
   public void testQueryNotActiveInPast()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findActive(ARTIFACT_ID_2,
                  BENCHMARK_ARTIFACT_TYPE_ID, new Date(0)));
      Assert.assertEquals(0, runtimeArtifacts.getSize());
   }
}
