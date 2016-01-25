/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Antje.Fuhrmann (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.test.api.setup;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;

import org.eclipse.stardust.engine.api.runtime.ServiceFactoryLocator;
import org.eclipse.stardust.engine.core.runtime.beans.SchemaHelper;
import org.eclipse.stardust.test.api.util.UsernamePasswordPair;

/**
 * Additionally to <code>TestClassSetup</code> this class setups a data cluster and
 * creates the cluster tables.
 *
 * @author Antje.Fuhrmann
 * @version $Revision: $
 */
public class DataClusterTestClassSetup extends TestClassSetup
{
   private String clusterConfigFile = "data-cluster.xml";

   private static final Log LOG = LogFactory.getLog(TestClassSetup.class);

   public DataClusterTestClassSetup(String clusterConfigFile, UsernamePasswordPair userPwdPair,
         ForkingServiceMode forkingServiceMode, final String... modelNames)
   {
      this(userPwdPair, forkingServiceMode, modelNames);
      this.clusterConfigFile = clusterConfigFile;
   }
   
   public DataClusterTestClassSetup(UsernamePasswordPair userPwdPair,
         ForkingServiceMode forkingServiceMode, final String... modelNames)
   {
      super(userPwdPair, forkingServiceMode, modelNames);
   }

   @Override
   protected void before() throws TestRtEnvException
   {
      if (locked)
      {
         return;
      }

      LOG.info("---> Setting up the test environment ...");

      dbms.init();
      dbms.start();
      dbms.createSchema();
      ClassPathResource resource = new ClassPathResource(clusterConfigFile);
      File configFile;
      try
      {
         configFile = resource.getFile();
         SchemaHelper.alterAuditTrailCreateDataClusterTables("sysop",
               configFile.getAbsolutePath(), false, true, null);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      springAppCtx.bootstrap(forkingServiceMode, testClass);

      sf = ServiceFactoryLocator.get(userPwdPair.username(), userPwdPair.password());
      if (modelNames.length > 0)
      {
         LOG.debug("Trying to deploy model(s) '" + Arrays.asList(modelNames) + "'.");
         RtEnvHome.deployModel(sf.getAdministrationService(), deploymentOptions, modelNames);
      }

      LOG.info("<--- ... setup of test environment done.");
   }
}
