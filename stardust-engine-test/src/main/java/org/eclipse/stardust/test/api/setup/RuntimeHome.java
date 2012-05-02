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
package org.eclipse.stardust.test.api.setup;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.stardust.engine.api.runtime.*;
import org.eclipse.stardust.engine.core.model.xpdl.XpdlUtils;
import org.eclipse.stardust.test.api.setup.TestRtEnvException.TestRtEnvAction;

/**
 * <p>
 * This class facilitates the task of
 * <ul>
 *    <li>reading a model from the file system, and</li>
 *    <li>deploying the read model to the Stardust Engine Runtime.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Furthermore, it's capable of cleaning up the Audit Trail runtime as well as the
 * DMS repository.
 * </p>
 * 
 * <p>
 * In addition, it caches read models so that a particular model will only
 * be read once from the file system.
 * </p>
 * 
 * @author Nicolas.Werlein
 * @version $Revision$
 */
public class RuntimeHome
{
   private static final Log LOG = LogFactory.getLog(RuntimeHome.class);
   
   private static final String MODEL_FOLDER = "models";
   
   private static final short CLEAN_UP_RUNTIME_RETRY_COUNT = 3;
   
   private static final String DMS_REPO_ROOT_FOLDER = "/";
   
   private static final Map<String, DeploymentElement> DEPLOYMENT_ELEMENTS = newHashMap();

   /**
    * <p>
    * Deploys the model with the given name to the Stardust Engine Runtime.
    * </p>
    * 
    * <p>
    * Models to be deployed with this method must reside in the folder
    * <code>models</code> on the classpath.
    * </p>
    * 
    * <p>
    * This method ensures that either all models or none at all will be deployed.
    * </p>
    * 
    * @param adminService an administration service of a user authorized to deploy models
    * @param modelNames the names of the models without extension (which will be assumed to be <code>xpdl</code>)
    * 
    * @throws ModelIOException if an exception occurs while reading the model from the file system
    * @throws DeploymentException if an exception occurs during model deployment
    */
   public static void deploy(final AdministrationService adminService, final String ... modelNames) throws ModelIOException, DeploymentException
   {
      if (adminService == null)
      {
         throw new NullPointerException("Administration Service must not be null.");
      }
      if (modelNames == null)
      {
         throw new NullPointerException("Name of Models must not be null.");
      }
      if (modelNames.length == 0)
      {
         throw new IllegalArgumentException("Name of Models must not be empty.");
      }

      final List<DeploymentElement> deploymentElements = newArrayList();
      for (final String m : modelNames)
      {
         final DeploymentElement deploymentElement = getDeploymentElement(m);
         deploymentElements.add(deploymentElement);
      }
      
      adminService.deployModel(deploymentElements, DeploymentOptions.DEFAULT);
   }
   
   /**
    * <p>
    * Cleans up the runtime and all models.
    * </p>
    * 
    * @param adminService an administration service of a user authorized to clean up the runtime
    * @throws TestRtEnvException if an exception occures during runtime and model cleanup
    */
   public static void cleanUpRuntimeAndModels(final AdministrationService adminService) throws TestRtEnvException
   {
      stopAllDaemons(adminService);
      
      LOG.debug("Trying to clean up the runtime and all models.");
      new RuntimeCleanerTemplate()
      {
         @Override
         protected void doCleanup()
         {
            adminService.cleanupRuntimeAndModels();
         }
      }.cleanUp();
   }

   /**
    * <p>
    * Cleans up the runtime (including user removal), but keeps the deployed models.
    * </p>
    * 
    * @param adminService an administration service of a user authorized to clean up the runtime
    * @throws TestRtEnvException if an exception occures during runtime and model cleanup
    */
   public static void cleanUpRuntime(final AdministrationService adminService) throws TestRtEnvException
   {
      stopAllDaemons(adminService);
      
      LOG.debug("Trying to clean up the runtime.");
      new RuntimeCleanerTemplate()
      {
         @Override
         protected void doCleanup()
         {
            adminService.cleanupRuntime(false);
         }
      }.cleanUp();
   }
   
   /**
    * <p>
    * Cleans up the DMS repository.
    * </p>
    * 
    * @throws TestRtEnvException if the DMS repository could not be cleaned up
    */
   public static void cleanUpDmsRepository(final DocumentManagementService docMgmtService) throws TestRtEnvException
   {
      LOG.debug("Trying to clean up the DMS repository.");
      
      try
      {
         docMgmtService.removeFolder(DMS_REPO_ROOT_FOLDER, true);
      }
      catch (final Exception e)
      {
         throw new TestRtEnvException("Could not clean up DMS repository.", e, TestRtEnvAction.CLEAN_UP_DMS_REPOSITORY);
      }
   }
   
   private static DeploymentElement getDeploymentElement(final String modelName)
   {
      if (modelName == null)
      {
         throw new NullPointerException("Name of Model must not be null.");
      }
      if (modelName.isEmpty())
      {
         throw new IllegalArgumentException("Name of Model must not be empty.");
      }
      
      DeploymentElement result = DEPLOYMENT_ELEMENTS.get(modelName);
      
      if (result == null)
      {
         try
         {
            result = createNewDeploymentElement(modelName);
         }
         catch (final IOException e)
         {
            throw new ModelIOException("Unable to read model file.", e);
         }
         
         DEPLOYMENT_ELEMENTS.put(modelName, result);
      }
      
      return result;
   }
   
   private static DeploymentElement createNewDeploymentElement(final String modelName) throws IOException
   {
      final String fqModelName = MODEL_FOLDER + "/" + modelName + "." + XpdlUtils.EXT_XPDL;
      final InputStream is = RuntimeHome.class.getClassLoader().getResourceAsStream(fqModelName);
      final Reader reader = new InputStreamReader(is);
      final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      int character;
      
      final byte[] model;
      try
      {
         while ((character = reader.read()) != -1)
         {
            outStream.write(character);
         }
         model = outStream.toByteArray();
      }
      finally
      {
         outStream.close();
      }
      
      final DeploymentElement result = new DeploymentElement(model);
      return result;
   }
   
   private static void stopAllDaemons(final AdministrationService adminService)
   {
      LOG.debug("Trying to stop all daemons.");
      
      final List<Daemon> allDaemons = adminService.getAllDaemons(false);
      for (final Daemon d : allDaemons)
      {
         final Daemon daemon = adminService.stopDaemon(d.getType(), true);
         
         final boolean isRunning = daemon.isRunning();
         final boolean isAck = daemon.getAcknowledgementState().equals(AcknowledgementState.RespondedOK);
         if (isRunning || !isAck)
         {
            throw new TestRtEnvException("Unable to stop daemon '" + daemon + "'.", TestRtEnvAction.DAEMON_TEARDOWN);
         }
      }
   }
   
   private static final class ModelIOException extends RuntimeException
   {
      private static final long serialVersionUID = 482439818461160624L;

      public ModelIOException(final String errorMsg, final IOException e)
      {
         super(errorMsg, e);
      }
   }
   
   private static abstract class RuntimeCleanerTemplate
   {
      public void cleanUp()
      {
         short retry = CLEAN_UP_RUNTIME_RETRY_COUNT;
         boolean success = false;

         while (!success && retry > 0)
         {
            try
            {
               doCleanup();
               success = true;
            }
            catch (final Exception e)
            {
               retry--;
               LOG.warn("Attempt to clean up runtime failed. Reattempting " + retry + " times:" + e.getMessage());
            }
         }
         
         if ( !success)
         {
            final String errorMsg = "Unable to clean up runtime.";
            LOG.error(errorMsg);
            throw new TestRtEnvException(errorMsg, TestRtEnvAction.CLEAN_UP_RUNTIME);
         }
      }
      
      protected abstract void doCleanup();
   }
   
   private RuntimeHome()
   {
      /* utility class; do not allow the creation of an instance */
   }
}
