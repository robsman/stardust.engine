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

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
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
 * Tests basic operations of the {@link RuntimeArtifact}.
 *
 * @author Roland.Stamm
 */
@FixMethodOrder(MethodSorters.JVM)
public class ArtifactSanityTest
{

   private static final String BENCHMARK_ARTIFACT_TYPE_ID = BenchmarkDefinitionArtifactType.TYPE_ID;

   private static final String ARTIFACT_NAME1 = "Benchmark One";

   private static final String ARTIFACT_ID1 = "bench1.benchmark";

   private static final String ARTIFACT_CONTENT1 = "benchmarkDefinition[]";

   private static final String ARTIFACT_NEW_CONTENT1 = "benchmarkDefinition[] updated.";

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
   {}

   @After
   public void cleanup() throws InterruptedException
   {}

   private RuntimeArtifact getRuntimeArtifact1(String artifactId)
   {
      return new RuntimeArtifact(BENCHMARK_ARTIFACT_TYPE_ID, artifactId, ARTIFACT_NAME1,
            ARTIFACT_CONTENT1.getBytes(), new Date(1));
   }

   @Test
   public void testDeployBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      DeployedRuntimeArtifact deployedRuntimeArtifact = as
            .deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID1));

      Assert.assertEquals(1, deployedRuntimeArtifact.getOid());
      Assert.assertEquals(ARTIFACT_ID1, deployedRuntimeArtifact.getArtifactId());
      Assert.assertEquals(ARTIFACT_NAME1, deployedRuntimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact.getArtifactTypeId());
   }

   @Test(expected = InvalidArgumentException.class)
   public void testDeployBenchmarkNullArguments()
   {
      AdministrationService as = sf.getAdministrationService();

      as.deployRuntimeArtifact(new RuntimeArtifact(BENCHMARK_ARTIFACT_TYPE_ID, null,
            null, null, null));

      Assert.fail("Should throw exception");
   }

   @Test(expected = PublicException.class)
   public void testDeployUnknownArtifactType()
   {
      AdministrationService as = sf.getAdministrationService();

      as.deployRuntimeArtifact(new RuntimeArtifact("unknown-type", null, null, null, null));

      Assert.fail("Should throw exception");
   }

   @Test
   public void testGetBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      RuntimeArtifact runtimeArtifact = as.getRuntimeArtifact(1);

      Assert.assertEquals(ARTIFACT_ID1, runtimeArtifact.getArtifactId());
      Assert.assertEquals(ARTIFACT_NAME1, runtimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID, runtimeArtifact.getArtifactTypeId());
      Assert.assertEquals(ARTIFACT_CONTENT1, new String(runtimeArtifact.getContent()));
   }

   @Test
   public void testQueryAllBenchmark()
   {
      QueryService qs = sf.getQueryService();

      DeployedRuntimeArtifacts runtimeArtifacts = qs
            .getRuntimeArtifacts(DeployedRuntimeArtifactQuery.findAll());
      Assert.assertEquals(1, runtimeArtifacts.getSize());

      DeployedRuntimeArtifact deployedRuntimeArtifact = runtimeArtifacts.get(0);

      Assert.assertEquals(1, deployedRuntimeArtifact.getOid());
      Assert.assertEquals(ARTIFACT_ID1, deployedRuntimeArtifact.getArtifactId());
      Assert.assertEquals(ARTIFACT_NAME1, deployedRuntimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact.getArtifactTypeId());
   }

   @Test
   public void testOverwriteBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      RuntimeArtifact runtimeArtifact = as.getRuntimeArtifact(1);

      runtimeArtifact.setContent(ARTIFACT_NEW_CONTENT1.getBytes());

      DeployedRuntimeArtifact deployedRuntimeArtifact = as.overwriteRuntimeArtifact(1,
            runtimeArtifact);

      Assert.assertEquals(1, deployedRuntimeArtifact.getOid());
      Assert.assertEquals(ARTIFACT_ID1, deployedRuntimeArtifact.getArtifactId());
      Assert.assertEquals(ARTIFACT_NAME1, deployedRuntimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact.getArtifactTypeId());

      RuntimeArtifact overwrittenRuntimeArtifact = as.getRuntimeArtifact(1);
      Assert.assertEquals(ARTIFACT_NEW_CONTENT1,
            new String(overwrittenRuntimeArtifact.getContent()));
      Assert.assertEquals(ARTIFACT_ID1, overwrittenRuntimeArtifact.getArtifactId());
      Assert.assertEquals(ARTIFACT_NAME1, overwrittenRuntimeArtifact.getArtifactName());
      Assert.assertEquals(BENCHMARK_ARTIFACT_TYPE_ID,
            deployedRuntimeArtifact.getArtifactTypeId());
   }

   @Test(expected = ObjectNotFoundException.class)
   public void testOverwriteNonExistingBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      RuntimeArtifact runtimeArtifact = as.getRuntimeArtifact(1);

      runtimeArtifact.setContent(ARTIFACT_NEW_CONTENT1.getBytes());

      as.overwriteRuntimeArtifact(0, runtimeArtifact);

      Assert.fail("Should throw exception");
   }

   @Test
   public void testDeleteBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      Assert.assertNotNull(as.getRuntimeArtifact(1));

      as.deleteRuntimeArtifact(1);

      Assert.assertNull(as.getRuntimeArtifact(1));
   }

   @Test(expected = ObjectNotFoundException.class)
   public void testDeleteNonExistingBenchmark()
   {
      AdministrationService as = sf.getAdministrationService();

      as.deleteRuntimeArtifact(0);
      Assert.fail("Should throw exception");
   }

   @Test
   public void testReferenceBroken()
   {
      AdministrationService as = sf.getAdministrationService();
      DocumentManagementService dms = sf.getDocumentManagementService();

      DeployedRuntimeArtifact ra = as
            .deployRuntimeArtifact(getRuntimeArtifact1(ARTIFACT_ID1));
      long oid = ra.getOid();

      // delete referenced content
      dms.removeFolder("/artifacts/runtime", true);

      // content is empty
      RuntimeArtifact ra2 = as.getRuntimeArtifact(oid);
      Assert.assertEquals(0, ra2.getContent().length);

      // repair content by overwrite
      ra2.setContent(ARTIFACT_NEW_CONTENT1.getBytes());
      as.overwriteRuntimeArtifact(oid, ra2);

      // new content should be delivered
      RuntimeArtifact ra3 = as.getRuntimeArtifact(oid);
      Assert.assertEquals(ARTIFACT_NEW_CONTENT1, new String(ra3.getContent()));

      // delete referenced content again
      dms.removeFolder("/artifacts/runtime", true);

      // cleanup should still work
      as.deleteRuntimeArtifact(oid);
      Assert.assertNull(as.getRuntimeArtifact(oid));
   }
}
